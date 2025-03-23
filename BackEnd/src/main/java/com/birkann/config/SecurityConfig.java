package com.birkann.config;

import static org.springframework.security.config.Customizer.withDefaults;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.birkann.jwt.JWTAuthenticaterFilter;
import com.birkann.oauth2.CustomOAuth2UserService;
import com.birkann.oauth2.OAuth2AuthenticationFailureHandler;
import com.birkann.oauth2.OAuth2AuthenticationSuccessHandler;
import com.birkann.oauth2.OAuth2RequestRepository;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
	private final String[] WHITE_LIST = {
			"/api/v1/auth/**",
			"/api/v1/check-email/**",
			"/api-docs",
			"/swagger-ui/**",
			"/swagger-ui.html",
			"/v3/api-docs/**",
			"/auth/**",
			"/oauth2/**",
			"/uploads/**",
			"/swagger-resources/**",
			"/configuration/ui",
			"/configuration/security",
			"/webjars/**",
			"/api/emails/**"
	};

	private final String[] ERROR_PATHS = {
		"/oauth2/callback/google"
	};

	private final JWTAuthenticaterFilter jwtAuthFilter;
	private final AuthenticationProvider authenticationProvider;
	private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
	private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;
	private final CustomOAuth2UserService customOAuth2UserService;
	private final OAuth2RequestRepository oAuth2RequestRepository;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.cors(withDefaults())
			.csrf(csrf -> csrf.disable())
			.authorizeHttpRequests(auth -> auth
				.requestMatchers(WHITE_LIST).permitAll()
				.requestMatchers(ERROR_PATHS).permitAll()
				.anyRequest().authenticated()
			)
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authenticationProvider(authenticationProvider)
			.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
			.oauth2Login(oauth2 -> oauth2
				.authorizationEndpoint(authorization -> authorization
					.baseUri("/oauth2/authorize")
					.authorizationRequestRepository(oAuth2RequestRepository)
				)
				.redirectionEndpoint(redirection -> redirection
					.baseUri("/oauth2/callback/*")
				)
				.userInfoEndpoint(userInfo -> userInfo
					.userService(customOAuth2UserService)
				)
				.successHandler(oAuth2AuthenticationSuccessHandler)
				.failureHandler(oAuth2AuthenticationFailureHandler)
			);

		return http.build();
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:8080", "http://127.0.0.1:3000", "http://127.0.0.1:8080", "https://cvraterai.com", "http://cvraterai.com"));
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(Arrays.asList("*"));
		configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));
		configuration.setAllowCredentials(true);
		configuration.setMaxAge(3600L);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}
