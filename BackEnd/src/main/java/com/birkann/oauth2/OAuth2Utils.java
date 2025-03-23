package com.birkann.oauth2;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class OAuth2Utils {
    
    private static final Logger logger = LoggerFactory.getLogger(OAuth2Utils.class);
    
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;
    
    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;
    
    @Value("${spring.security.oauth2.client.registration.google.scope}")
    private String scope;
    
    @Value("${app.oauth2.authorizedRedirectUri}")
    private String redirectUri;
    
    /**
     * Google OAuth2 yetkilendirme URL'sini oluşturur
     * @return Google OAuth2 yetkilendirme URL'si
     */
    public String createGoogleAuthorizationUrl() {
        logger.debug("Creating Google authorization URL with configured parameters");
        
        // Scope formatını düzelt - virgülleri boşluklara dönüştür
        String properlyFormattedScope = scope.replace(",", " ");
        // Gmail API scope'unu boşluk ile ekle
        String fullScope = properlyFormattedScope + " https://www.googleapis.com/auth/gmail.readonly";
        
        logger.debug("Using scopes: {}", fullScope);
        
        // Build the authorization URL using UriComponentsBuilder
        String authUrl = UriComponentsBuilder.fromHttpUrl("https://accounts.google.com/o/oauth2/v2/auth")
            .queryParam("client_id", clientId)
            .queryParam("redirect_uri", redirectUri)
            .queryParam("response_type", "code")
            .queryParam("scope", fullScope)
            .queryParam("access_type", "offline")
            .queryParam("prompt", "consent")
            .queryParam("state", "google")
            .build()
            .toUriString();
        
        logger.debug("Generated Google authorization URL: {}", authUrl);
        return authUrl;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public String buildGoogleAuthorizationUrl(String redirectUri) {
        logger.debug("Building Google authorization URL with redirectUri: {}", redirectUri);
        
        // Google OAuth2 scopes
        List<String> scopes = Arrays.asList(
            "email",
            "profile",
            "https://www.googleapis.com/auth/gmail.readonly",
            "openid"
        );
        
        // Build the scope parameter with space-separated values
        String scope = String.join(" ", scopes);
        logger.debug("Using scopes: {}", scope);
        
        // Build the authorization URL
        String authUrl = UriComponentsBuilder.fromHttpUrl("https://accounts.google.com/o/oauth2/v2/auth")
            .queryParam("client_id", clientId)
            .queryParam("redirect_uri", "http://localhost:8080/oauth2/callback/google")
            .queryParam("response_type", "code")
            .queryParam("scope", scope)
            .queryParam("access_type", "offline")
            .queryParam("prompt", "consent")
            // We will let Spring Security handle the state parameter
            .build()
            .toUriString();
        
        logger.debug("Generated Google authorization URL: {}", authUrl);
        return authUrl;
    }
    
    public String buildGoogleEmailApiUrl(String userId, String accessToken) {
        logger.debug("Building Gmail API URL for userId: {}", userId);
        
        // Build the Gmail API URL
        String apiUrl = UriComponentsBuilder.fromHttpUrl("https://gmail.googleapis.com/gmail/v1/users/" + userId + "/messages")
            .queryParam("access_token", accessToken)
            .build()
            .toUriString();
        
        logger.debug("Generated Gmail API URL: {}", apiUrl);
        return apiUrl;
    }
    
    // OAuth2 providers enum
    public enum Provider {
        GOOGLE("google");
        
        private String providerType;
        
        Provider(String providerType) {
            this.providerType = providerType;
        }
        
        public String getProviderType() {
            return providerType;
        }
        
        public static Provider getProvider(String providerType) {
            return Arrays.stream(Provider.values())
                .filter(provider -> provider.getProviderType().equals(providerType))
                .findFirst()
                .orElse(null);
        }
    }
} 