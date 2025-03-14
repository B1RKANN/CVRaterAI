package com.birkann.service.impl;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.birkann.dto.AuthRequest;
import com.birkann.dto.AuthResponse;
import com.birkann.dto.DtoCredit;
import com.birkann.dto.DtoUser;
import com.birkann.dto.RefreshTokenRequest;
import com.birkann.dto.RegisterRequest;
import com.birkann.enums.PlanType;
import com.birkann.exception.BaseException;
import com.birkann.exception.ErrorMessage;
import com.birkann.exception.MessageType;
import com.birkann.jwt.JWTService;
import com.birkann.model.Credit;
import com.birkann.model.RefreshToken;
import com.birkann.model.User;
import com.birkann.repository.CreditRepository;
import com.birkann.repository.RefreshTokenRepository;
import com.birkann.repository.UserRepository;
import com.birkann.service.IAuthenticationService;

@Service
public class AuthenticationService implements IAuthenticationService{
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private AuthenticationProvider authenticationProvider;
	
	@Autowired
	private JWTService jwtService;
	
	@Autowired
	private RefreshTokenRepository refreshTokenRepository;

	@Autowired
	private CreditRepository creditRepository;
	
	private User createUser(RegisterRequest input) {
		User user = new User();
		user.setName(input.getName());
		user.setEmail(input.getEmail());
		user.setPassword(passwordEncoder.encode(input.getPassword()));
		user.setCredit(saveCredit());
		return user;
	}

	private Credit saveCredit() {
		Credit credit = new Credit();
		credit.setPlanType(PlanType.FREE);
		credit.setUserCredit(20);
		Date startDate = new Date();
		credit.setStartDate(startDate);
		Date expiredDate = new Date(startDate.getTime() + (7 * 24 * 60 * 60 * 1000L));
		credit.setExpiredDate(expiredDate);
		return creditRepository.save(credit);
	}
	
	private RefreshToken createRefreshToken(User user) {
		RefreshToken refreshToken = new RefreshToken();
		refreshToken.setCreateTime(new Date());
		refreshToken.setExpiredTime(new Date(System.currentTimeMillis()+1000*60*60*4));
		refreshToken.setRefreshToken(UUID.randomUUID().toString());
		refreshToken.setUser(user);
		return refreshToken;
	}
	
	
	@Override
	public DtoUser register(RegisterRequest input) {
		DtoUser dtoUser = new DtoUser();
		DtoCredit dtoCredit = new DtoCredit();
		User savedUser = userRepository.save(createUser(input));
		BeanUtils.copyProperties(savedUser, dtoUser);
		BeanUtils.copyProperties(savedUser.getCredit(), dtoCredit);
		dtoUser.setCredit(dtoCredit);
		return dtoUser;
	}

	@Override
	public AuthResponse authenticate(AuthRequest input) {
		try {
			UsernamePasswordAuthenticationToken authenticationToken =
					new UsernamePasswordAuthenticationToken(input.getEmail(), input.getPassword());
			
			authenticationProvider.authenticate(authenticationToken);
			
			Optional<User> optUser = userRepository.findByEmail(input.getEmail());
	
			String accesToken = jwtService.generateToken(optUser.get());
			
			RefreshToken savedRefreshToken = refreshTokenRepository.save(createRefreshToken(optUser.get()));
			
			return new AuthResponse(accesToken, savedRefreshToken.getRefreshToken());
			
		} catch (Exception e) {
			throw new BaseException(new ErrorMessage(MessageType.USERNAME_OR_PASSWORD_INVALID, e.getMessage()));
		}
	}
	
	
	public boolean isValidRefreshToken(Date expiredDate) {
		return new Date().before(expiredDate);
	}
	
	
	@Override
	public AuthResponse refreshToken(RefreshTokenRequest input) {
		Optional<RefreshToken> optRefreshToken = refreshTokenRepository.findByRefreshToken(input.getRefreshToken());
		
		if (optRefreshToken.isEmpty()) {
			throw new BaseException(new ErrorMessage(MessageType.REFRESH_TOKEN_NOT_FOUND, input.getRefreshToken()));
		}
		if(!isValidRefreshToken(optRefreshToken.get().getExpiredTime())) {
			throw new BaseException(new ErrorMessage(MessageType.REFRESH_TOKEN_IS_EXPIRED, input.getRefreshToken()));
		}
		
		User user = optRefreshToken.get().getUser();
		
		String accesToken = jwtService.generateToken(user);
		RefreshToken savedRefreshToken = refreshTokenRepository.save(createRefreshToken(user));
		
		
		
		return new AuthResponse(accesToken, savedRefreshToken.getRefreshToken());
	}
	
}
