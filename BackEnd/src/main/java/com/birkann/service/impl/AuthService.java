package com.birkann.service.impl;

import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.birkann.dto.AuthRequest;
import com.birkann.dto.AuthResponse;
import com.birkann.dto.RefreshTokenRequest;
import com.birkann.dto.RegisterRequest;
import com.birkann.enums.PlanType;
import com.birkann.model.Credit;
import com.birkann.model.RefreshToken;
import com.birkann.model.Role;
import com.birkann.model.User;
import com.birkann.repository.CreditRepository;
import com.birkann.repository.RefreshTokenRepository;
import com.birkann.repository.UserRepository;
import com.birkann.service.IAuthService;
import com.birkann.service.IJWTService;

import jakarta.servlet.http.HttpServletResponse;


@Service
public class AuthService implements IAuthService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private CreditRepository creditRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private IJWTService jwtService;

	@Autowired
	private RefreshTokenRepository refreshTokenRepository;
	
	@Value("${jwt.refreshExpirationDateInMs}")
    private int refreshExpirationDateInMs;
	
	@Override
	public AuthResponse signup(RegisterRequest request) {
		
		if(userRepository.findByEmail(request.getEmail()).isPresent()) {
			throw new RuntimeException("User already exist");
		}
		
		// Kaydolurken kullanıcıya krediler ekle
		Credit credit = new Credit();
		credit.setUserCredit(20); // Varsayılan olarak 20 kredi
		credit.setPlanType(PlanType.FREE); // Plan tipini FREE olarak ayarla
		
		// Başlangıç tarihi olarak şu anki zamanı ayarla
		Date startDate = new Date();
		credit.setStartDate(startDate);
		
		// Bitiş tarihi olarak 7 gün sonrasını ayarla
		Date expiredDate = new Date(startDate.getTime() + (7 * 24 * 60 * 60 * 1000L)); // 7 gün
		credit.setExpiredDate(expiredDate);
		
		credit = creditRepository.save(credit);
		
		User user = new User();
		user.setName(request.getName());
		user.setEmail(request.getEmail());
		user.setPassword(passwordEncoder.encode(request.getPassword()));
		user.setCredit(credit);
		user.setRole(Role.USER); // Varsayılan olarak USER rolü
		
		user = userRepository.save(user);
		
		// JWT token oluştur
		String jwt = jwtService.generateToken(user);
		
		// Refresh token oluştur
		RefreshToken refreshToken = createRefreshToken(user);
		
		AuthResponse authResponse = new AuthResponse();
		authResponse.setToken(jwt);
		authResponse.setRefreshToken(refreshToken.getToken());
		authResponse.setRole(user.getRole().toString()); // Kullanıcı rolünü ayarla
		
		// Kullanıcı bilgilerini response'a ekle
		authResponse.setUserId(user.getId());
		authResponse.setEmail(user.getEmail());
		authResponse.setName(user.getName());
		
		return authResponse;
	}
	
	@Override
	public AuthResponse signupWithCookie(RegisterRequest request, HttpServletResponse response) {
		// Normal kayıt işlemini gerçekleştir
		AuthResponse authResponse = signup(request);
		
		// JWT token'ı cookie olarak ayarla
		jwtService.addTokenToCookie(response, authResponse.getToken());
		
		// Kullanıcı bilgilerini response'a ekle
		User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new RuntimeException("User not found"));
		authResponse.setUserId(user.getId());
		authResponse.setEmail(user.getEmail());
		authResponse.setName(user.getName());
		
		return authResponse;
	}

	@Override
	public AuthResponse signin(AuthRequest request) {
		
		authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
		
		var user = userRepository.findByEmail(request.getEmail()).orElseThrow(()-> new IllegalArgumentException("Invalid email or password."));
			
		var jwt = jwtService.generateToken(user);
		
		// Önceki refresh token varsa sil
		refreshTokenRepository.deleteByUser(user);
		
		// Yeni refresh token oluştur
		RefreshToken refreshToken = createRefreshToken(user);
		
		AuthResponse authResponse = new AuthResponse();
		authResponse.setToken(jwt);
		authResponse.setRefreshToken(refreshToken.getToken());
		authResponse.setRole(user.getRole().toString()); // Kullanıcı rolünü ekle
		
		// Kullanıcı bilgilerini response'a ekle
		authResponse.setUserId(user.getId());
		authResponse.setEmail(user.getEmail());
		authResponse.setName(user.getName());
		
		return authResponse;
	}
	
	@Override
	public AuthResponse signinWithCookie(AuthRequest request, HttpServletResponse response) {
		// Normal giriş işlemini gerçekleştir
		AuthResponse authResponse = signin(request);
		
		// JWT token'ı cookie olarak ayarla
		jwtService.addTokenToCookie(response, authResponse.getToken());
		
		// Kullanıcı bilgilerini response'a ekle
		User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new RuntimeException("User not found"));
		authResponse.setUserId(user.getId());
		authResponse.setEmail(user.getEmail());
		authResponse.setName(user.getName());
		
		return authResponse;
	}
	
	@Override
	public AuthResponse refreshToken(RefreshTokenRequest request) {
		String requestRefreshToken = request.getRefreshToken();

		Optional<RefreshToken> refreshToken = refreshTokenRepository.findByToken(requestRefreshToken);
		
		if(refreshToken.isPresent()) {
			RefreshToken token = refreshToken.get();
			
			if(token.getExpiryDate().before(new Date())) {
				refreshTokenRepository.delete(token);
				throw new RuntimeException("Refresh token was expired. Please make a new signin request");
			}
			
			User user = (User) token.getUser();
			var jwt = jwtService.generateToken(user);
			
			AuthResponse authResponse = new AuthResponse();
			authResponse.setToken(jwt);
			authResponse.setRefreshToken(requestRefreshToken);
			authResponse.setRole(user.getRole().toString()); // Kullanıcı rolünü ekle
			
			// Kullanıcı bilgilerini response'a ekle
			authResponse.setUserId(user.getId());
			authResponse.setEmail(user.getEmail());
			authResponse.setName(user.getName());
			
			return authResponse;
		}
		
		throw new RuntimeException("Refresh token is not in database!");
	}
	
	@Override
	public AuthResponse refreshTokenWithCookie(RefreshTokenRequest request, HttpServletResponse response) {
		// Normal token yenileme işlemini gerçekleştir
		AuthResponse authResponse = refreshToken(request);
		
		// JWT token'ı cookie olarak ayarla
		jwtService.addTokenToCookie(response, authResponse.getToken());
		
		// Refresh token'dan kullanıcı bilgilerini al
		Optional<RefreshToken> refreshTokenOpt = refreshTokenRepository.findByToken(request.getRefreshToken());
		if (refreshTokenOpt.isPresent()) {
			User user = (User) refreshTokenOpt.get().getUser();
			authResponse.setUserId(user.getId());
			authResponse.setEmail(user.getEmail());
			authResponse.setName(user.getName());
		}
		
		return authResponse;
	}
	
	@Override
	public void logout(HttpServletResponse response) {
		// JWT token'ı temizleyen cookie ayarla
		jwtService.clearTokenCookie(response);
	}
	
    public RefreshToken createRefreshToken(User user) {
    	RefreshToken refreshToken = new RefreshToken();
    	refreshToken.setUser(user);
    	refreshToken.setExpiryDate(new Date(System.currentTimeMillis() + refreshExpirationDateInMs));
    	refreshToken.setToken(java.util.UUID.randomUUID().toString());
    	refreshToken = refreshTokenRepository.save(refreshToken);
    	return refreshToken;
    }

	@Override
	public User createAdminUser(RegisterRequest request) {
		// Admin kullanıcı oluşturma işlemi - sadece sistem yöneticisi tarafından çağrılmalı
		
		if(userRepository.findByEmail(request.getEmail()).isPresent()) {
			throw new RuntimeException("User already exist");
		}
		
		// Admin kullanıcının yüksek miktarda kredisi olsun
		Credit credit = new Credit();
		credit.setUserCredit(100); // Admin için 100 kredi
		credit.setPlanType(PlanType.PRO); // Admin için PRO plan
		
		// Başlangıç tarihi olarak şu anki zamanı ayarla
		Date startDate = new Date();
		credit.setStartDate(startDate);
		
		// Bitiş tarihi olarak 365 gün sonrasını ayarla (1 yıl)
		Date expiredDate = new Date(startDate.getTime() + (365L * 24 * 60 * 60 * 1000L));
		credit.setExpiredDate(expiredDate);
		
		credit = creditRepository.save(credit);
		
		User user = new User();
		user.setName(request.getName());
		user.setEmail(request.getEmail());
		user.setPassword(passwordEncoder.encode(request.getPassword()));
		user.setCredit(credit);
		user.setRole(Role.ADMIN); // ADMIN rolü atanıyor
		
		return userRepository.save(user);
	}
} 