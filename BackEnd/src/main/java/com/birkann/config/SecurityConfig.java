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

import com.birkann.handler.AuthEntryPoint;
import com.birkann.jwt.JWTAuthenticaterFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

	public static final String REGISTER = "/register";
	public static final String AUTHENTICATE = "/authenticate";
	public static final String REFRESH_TOKEN = "/refreshToken";
	public static final String[] AUTH_V2_PATHS = {
	    "/auth/v2/register",
	    "/auth/v2/authenticate",
	    "/auth/v2/refreshToken"
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
	@Order(1)
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.csrf().disable()
		.authorizeHttpRequests(request-> 
		request.requestMatchers(AUTHENTICATE, REGISTER, REFRESH_TOKEN).permitAll()
		.requestMatchers(AUTH_V2_PATHS).permitAll()  // Auth v2 yollarını herkese açık yap
		.requestMatchers(SWAGGER_PATH).permitAll()
		// Credit controller endpointlerini sadece ADMIN rolüne sahip kullanıcılara aç
		.requestMatchers("/api/v1/credit/**").hasRole("ADMIN")
		.requestMatchers("/rest/api/credit/**").hasRole("ADMIN")
		.anyRequest()
		.authenticated())
		.exceptionHandling().authenticationEntryPoint(authEntryPoint).and()
		.sessionManagement(session-> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
		.authenticationProvider(authenticationProvider)
		.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}
	
}
