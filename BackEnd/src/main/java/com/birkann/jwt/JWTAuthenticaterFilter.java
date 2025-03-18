package com.birkann.jwt;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.birkann.service.IJWTService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JWTAuthenticaterFilter extends OncePerRequestFilter {
	
	private static final Logger logger = LoggerFactory.getLogger(JWTAuthenticaterFilter.class);
	
	private static final List<String> PUBLIC_PATHS = Arrays.asList(
		"/auth/v2/register",
		"/auth/v2/authenticate",
		"/auth/v2/refreshToken",
		"/auth/v2/register-with-cookie",
		"/auth/v2/authenticate-with-cookie",
		"/auth/v2/refreshToken-with-cookie",
		"/auth/v2/logout"
	);
	
	@Autowired
	private IJWTService jwtService;
	
	@Autowired
	private UserDetailsService userDetailsService;
	
	/**
	 * Bu metot filtre uygulanmaması gereken URL'leri belirler
	 * Kayıt, giriş ve token yenileme işlemleri için filtre uygulanmamalıdır
	 */
	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
		String path = request.getServletPath();
		String method = request.getMethod();
		logger.debug("shouldNotFilter kontrol ediliyor, yol: {}, metod: {}", path, method);
		
		// OPTIONS isteklerini her zaman geçir
		if ("OPTIONS".equalsIgnoreCase(method)) {
			logger.info("JWT Filter - OPTIONS isteği geçiriliyor: {}", path);
			return true;
		}
		
		// Public endpoint kontrolü
		boolean isPublicEndpoint = PUBLIC_PATHS.stream()
			.anyMatch(publicPath -> path.equals(publicPath) || path.equals("/auth" + publicPath));
		
		logger.info("JWT Filter - URL: {}, Public Endpoint: {}", path, isPublicEndpoint);
		return isPublicEndpoint;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		
		String requestURI = request.getRequestURI();
		logger.debug("JWT Filter işleniyor. URI: {}, Metod: {}", requestURI, request.getMethod());
		
		// Önce cookie'den token'ı almayı dene
		String token = jwtService.getTokenFromCookie(request);
		
		// Cookie'de token yoksa Authorization header'dan almayı dene
		if (token == null) {
			String header = request.getHeader("Authorization");
			if(header == null || !header.startsWith("Bearer ")) {
				logger.debug("JWT token bulunamadı (cookie veya header). URI: {}", requestURI);
				filterChain.doFilter(request, response);
				return;
			}
			token = header.substring(7);
		} else {
			logger.debug("JWT token cookie'den alındı. URI: {}", requestURI);
		}
		
		String username = null;
		
		try {
			username = jwtService.extractUsername(token);
			logger.debug("JWT'den çıkarılan kullanıcı adı: {}, URI: {}", username, requestURI);
		} catch (Exception e) {
			logger.error("JWT işleme hatası: {}, URI: {}", e.getMessage(), requestURI);
			filterChain.doFilter(request, response);
			return;
		}
		
		if(username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
			logger.debug("SecurityContext boş, kullanıcı doğrulanıyor: {}, URI: {}", username, requestURI);
			
			UserDetails userDetails = userDetailsService.loadUserByUsername(username);
			if(userDetails == null) {
				logger.warn("Kullanıcı bulunamadı: {}, URI: {}", username, requestURI);
				filterChain.doFilter(request, response);
				return;
			}
			
			logger.debug("Kullanıcı detayları yüklendi: {}, yetkileri: {}, URI: {}", 
					username, userDetails.getAuthorities(), requestURI);
			
			if(jwtService.isTokenValid(token, userDetails)) {
				logger.debug("JWT geçerli, kimlik doğrulama oluşturuluyor: {}, URI: {}", username, requestURI);
				
				UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
						userDetails,
						null,
						userDetails.getAuthorities());
				
				authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(authToken);
				
				logger.debug("Kimlik doğrulama başarılı, SecurityContext güncellendi: {}, yetkileri: {}, URI: {}", 
						username, userDetails.getAuthorities(), requestURI);
			} else {
				logger.warn("JWT geçersiz: {}, URI: {}", username, requestURI);
			}
		} else if (username != null) {
			logger.debug("SecurityContext zaten dolu. Mevcut kimlik: {}, URI: {}", 
					SecurityContextHolder.getContext().getAuthentication().getName(), requestURI);
		}
		
		filterChain.doFilter(request, response);
	}

}
