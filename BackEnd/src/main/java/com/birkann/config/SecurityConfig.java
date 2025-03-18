package com.birkann.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.core.annotation.Order;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.birkann.handler.AuthEntryPoint;
import com.birkann.jwt.JWTAuthenticaterFilter;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

	public static final String[] AUTH_V2_PATHS = {
	    "/auth/v2/register",
	    "/auth/v2/authenticate",
	    "/auth/v2/refreshToken",
	    "/auth/v2/register-with-cookie",
	    "/auth/v2/authenticate-with-cookie",
	    "/auth/v2/refreshToken-with-cookie",
	    "/auth/v2/logout"
	};
	public static final String[] SWAGGER_PATH = {
			"/swagger-ui/**",
			"/v3/api-docs/**",
			"/swagger-ui/swagger-ui.css",
			"/swagger-ui.html"
	};
	
	@Autowired
	private AuthenticationProvider authenticationProvider;
	
	@Autowired
	private JWTAuthenticaterFilter jwtAuthenticationFilter;
	
	@Autowired
	private AuthEntryPoint authEntryPoint;
	
	static {
		// SecurityContextHolder'ın debug modunu etkinleştir
		SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
	}
	
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(Arrays.asList("http://127.0.0.1:5500"));
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(Arrays.asList("*"));
		configuration.setAllowCredentials(true);
		
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
	
	@Bean
	@Order(1)
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			.cors(cors -> cors.configurationSource(corsConfigurationSource()))
			.csrf(csrf -> csrf.disable())
			.authorizeHttpRequests(request-> 
				request
				// Public endpoints - herkese açık
				.requestMatchers(AUTH_V2_PATHS).permitAll()  // Auth v2 yollarını herkese açık yap
				.requestMatchers(SWAGGER_PATH).permitAll()
				// OPTIONS isteklerine izin ver
				.requestMatchers("OPTIONS", "/**").permitAll()
				// Admin endpoints - sadece ADMIN rolüne sahip kullanıcılar için
				.requestMatchers("/api/v1/credit/**").hasRole("ADMIN")
				.requestMatchers("/rest/api/credit/**").hasRole("ADMIN")
				// Diğer tüm endpoint'ler için kimlik doğrulama gerekli
				.anyRequest()
				.authenticated())
			.exceptionHandling(exceptionHandling -> 
				exceptionHandling.authenticationEntryPoint(authEntryPoint)
			)
			.sessionManagement(session-> 
				session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			)
			.authenticationProvider(authenticationProvider)
			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
		
		return http.build();
	}
	
}
