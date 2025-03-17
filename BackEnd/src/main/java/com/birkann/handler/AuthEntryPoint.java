package com.birkann.handler;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthEntryPoint implements AuthenticationEntryPoint {
	
	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException, ServletException {
		System.out.println("AuthEntryPoint commence metodu çağrıldı: " + request.getRequestURI() + ", Hata: " + authException.getMessage());
		System.out.println("Metot: " + request.getMethod());
		System.out.println("Headers: " + request.getHeaderNames());
		response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Yetkilendirme hatası: " + authException.getMessage());
	}
	
}
