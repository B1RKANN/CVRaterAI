package com.birkann.jwt;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.birkann.model.User;
import com.birkann.oauth2.UserPrincipal;
import com.birkann.service.IJWTService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class JWTService implements IJWTService {
	
	private static final String JWT_COOKIE_NAME = "jwt";
	public static final String SECRET_KEY = "7UiEQwqHpPeuR9HEyd2NcEgluhuJ9Ctdn8/8lMyvUrY=";//h256 secret key generator

	@Override
	public String generateToken(UserDetails userDetails) {
		if (userDetails instanceof UserPrincipal) {
			return generateToken((UserPrincipal) userDetails);
		} else if (userDetails instanceof User) {
			// UserDetails'i User sınıfına cast ediyoruz
			User user = (User) userDetails;
			
			return Jwts.builder()
			.setSubject(userDetails.getUsername())
			.claim("userId", user.getId()) // Kullanıcı ID'sini claim olarak ekliyoruz
			.claim("role", user.getRole().name()) // Kullanıcı rolünü de ekleyebiliriz
			.setIssuedAt(new Date())
			.setExpiration(new Date(System.currentTimeMillis()+1000*60*60*2))
			.signWith(getKey(), SignatureAlgorithm.HS256)
			.compact();
		}
		
		// Fallback for other UserDetails implementations
		return Jwts.builder()
		.setSubject(userDetails.getUsername())
		.setIssuedAt(new Date())
		.setExpiration(new Date(System.currentTimeMillis()+1000*60*60*2))
		.signWith(getKey(), SignatureAlgorithm.HS256)
		.compact();
	}
	
	public String generateToken(UserPrincipal userPrincipal) {
		return Jwts.builder()
		.setSubject(userPrincipal.getUsername())
		.claim("userId", userPrincipal.getId()) // Kullanıcı ID'sini claim olarak ekliyoruz
		.claim("name", userPrincipal.getName())
		.claim("role", userPrincipal.getAuthorities().iterator().next().getAuthority()) // İlk yetkiyi rol olarak kullan
		.setIssuedAt(new Date())
		.setExpiration(new Date(System.currentTimeMillis()+1000*60*60*2))
		.signWith(getKey(), SignatureAlgorithm.HS256)
		.compact();
	}
	
	
	public Claims getClaims(String token) {
		Claims claims = Jwts.parserBuilder()
		.setSigningKey(getKey())
		.build()
		.parseClaimsJws(token).getBody();
		
		return claims;
	}
	
	
	public <T> T exportToken(String token,Function<Claims, T> claimsFunc) {
		Claims claims = getClaims(token);
		return claimsFunc.apply(claims);
	}
	
	@Override
	public String extractUsername(String token) {
		return exportToken(token, Claims::getSubject);
	}
	
	public String getUsernameByToken(String token) {
		return extractUsername(token);
	}
	
	@Override
	public boolean isTokenValid(String token, UserDetails userDetails) {
		final String username = extractUsername(token);
		return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
	}
	
	public boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}
	
	private Date extractExpiration(String token) {
		return exportToken(token, Claims::getExpiration);
	}
	
	public Boolean isTokenValid(String token) {
		Date expiredDate = exportToken(token, Claims::getExpiration);
		return new Date().before(expiredDate);
	}

	public Key getKey() {
		byte[] bytes = Decoders.BASE64.decode(SECRET_KEY);
		return Keys.hmacShaKeyFor(bytes);
	}
	
	@Override
	public void addTokenToCookie(HttpServletResponse response, String token) {
		Cookie cookie = new Cookie(JWT_COOKIE_NAME, token);
		cookie.setHttpOnly(true);
		cookie.setSecure(false); // true olarak değiştirin (HTTPS için)
		cookie.setPath("/");
		cookie.setMaxAge(2 * 60 * 60); // 2 saat
		response.addCookie(cookie);
	}

	@Override
	public String getTokenFromCookie(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (JWT_COOKIE_NAME.equals(cookie.getName())) {
					return cookie.getValue();
				}
			}
		}
		return null;
	}

	@Override
	public void clearTokenCookie(HttpServletResponse response) {
		Cookie cookie = new Cookie(JWT_COOKIE_NAME, null);
		cookie.setHttpOnly(true);
		cookie.setSecure(false); // true olarak değiştirin (HTTPS için)
		cookie.setPath("/");
		cookie.setMaxAge(0); // Cookie'yi hemen sil
		response.addCookie(cookie);
	}

	// DefaultOidcUser'dan JWT token oluşturma metodu
	public String generateTokenFromOidcUser(org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser oidcUser) {
		return Jwts.builder()
		.setSubject(oidcUser.getEmail())
		.claim("userId", oidcUser.getSubject()) // Subject değerini kullanıcı ID'si olarak kullan (Google'ın unique ID'si)
		.claim("name", oidcUser.getFullName())
		.claim("email", oidcUser.getEmail())
		.claim("picture", oidcUser.getPicture())
		.claim("role", "USER") // Varsayılan rol
		.setIssuedAt(new Date())
		.setExpiration(new Date(System.currentTimeMillis()+1000*60*60*2)) // 2 saat
		.signWith(getKey(), SignatureAlgorithm.HS256)
		.compact();
	}
}
