package com.birkann.jwt;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.birkann.service.IJWTService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JWTService implements IJWTService {
	
	public static final String SECRET_KEY = "7UiEQwqHpPeuR9HEyd2NcEgluhuJ9Ctdn8/8lMyvUrY=";//h256 secret key generator

	@Override
	public String generateToken(UserDetails userDetails) {
		return Jwts.builder()
		.setSubject(userDetails.getUsername())
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
}
