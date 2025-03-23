package com.birkann.oauth2;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(OAuth2AuthenticationFailureHandler.class);
    
    private final OAuth2RequestRepository oAuth2RequestRepository;
    
    @Value("${oauth2.redirectUri}")
    private String defaultRedirectUri;
    
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) 
            throws IOException, ServletException {
        
        logger.error("OAuth2 kimlik doğrulama hatası: {}", exception.getMessage(), exception);
        
        // Hata detaylarını loglayalım
        logRequestDetails(request);
        
        // Frontend yönlendirme URL'sini belirle
        String targetUrl = determineTargetUrl(request);
        
        // Hatayı URL'ye ekle
        String errorMessage = exception.getMessage();
        if (errorMessage == null) {
            errorMessage = "Bilinmeyen kimlik doğrulama hatası";
            logger.error("Hata nedeni: Bilinmiyor");
        } else {
            logger.debug("Hata mesajı: {}", errorMessage);
        }
        
        // URL-encode the error message
        String encodedErrorMessage = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8.toString());
        logger.debug("URL-encoded hata mesajı: {}", encodedErrorMessage);
        
        // Hatayı URL parametresi olarak ekle
        targetUrl = UriComponentsBuilder.fromUriString(targetUrl)
                .queryParam("error", encodedErrorMessage)
                .build().toUriString();
        
        logger.debug("Final yönlendirme URL'si: {}", targetUrl);
        
        // Cookie'leri temizle
        oAuth2RequestRepository.removeOAuth2AuthorizationRequestCookies(request, response);
        
        // Yönlendirme yap
        logger.debug("Yönlendirme URL'si: {}", targetUrl);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
    
    private String determineTargetUrl(HttpServletRequest request) {
        // Frontend yönlendirme URL'sini cookie'den al
        Optional<String> redirectUri = oAuth2RequestRepository.getRedirectUri(request);
        
        // Cookie'de yoksa, varsayılan frontend URL'sini kullan
        if (redirectUri.isPresent()) {
            logger.debug("Cookie'den alınan yönlendirme URL'si: {}", redirectUri.get());
            return redirectUri.get();
        } else {
            logger.debug("Varsayılan yönlendirme URL'si kullanılıyor: {}", defaultRedirectUri);
            return defaultRedirectUri;
        }
    }
    
    private void logRequestDetails(HttpServletRequest request) {
        logger.debug("İstek URI: {}", request.getRequestURI());
        logger.debug("İstek query string: {}", request.getQueryString());
        logger.debug("İstek metodu: {}", request.getMethod());
        
        // Headers
        request.getHeaderNames().asIterator().forEachRemaining(headerName -> 
            logger.debug("Header - {}: {}", headerName, request.getHeader(headerName))
        );
        
        // Parameters
        request.getParameterNames().asIterator().forEachRemaining(paramName -> 
            logger.debug("Parametre - {}: {}", paramName, request.getParameter(paramName))
        );
    }
} 