package com.birkann.oauth2;

import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2RequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {
    private static final Logger logger = LoggerFactory.getLogger(OAuth2RequestRepository.class);
    
    public static final String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request";
    public static final String REDIRECT_URI_PARAM_COOKIE_NAME = "redirect_uri";
    public static final String OAUTH2_STATE_COOKIE_NAME = "oauth2_state";
    private static final int COOKIE_EXPIRE_SECONDS = 180;

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        logger.debug("Loading authorization request from cookie");
        return getCookieValue(request, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)
                .map(cookie -> deserialize(Base64.getUrlDecoder().decode(cookie), OAuth2AuthorizationRequest.class))
                .orElse(null);
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request, HttpServletResponse response) {
        if (authorizationRequest == null) {
            logger.debug("Authorization request is null, removing cookies");
            removeOAuth2AuthorizationRequestCookies(request, response);
            return;
        }

        logger.debug("Saving authorization request to cookie");
        
        // Create a secure state value
        String stateValue = UUID.randomUUID().toString();
        logger.debug("Generated secure state value: {}", stateValue);
        
        // Set state cookie
        Cookie stateCookie = new Cookie(OAUTH2_STATE_COOKIE_NAME, stateValue);
        stateCookie.setPath("/");
        stateCookie.setHttpOnly(true);
        stateCookie.setMaxAge(COOKIE_EXPIRE_SECONDS);
        response.addCookie(stateCookie);
        
        // Set auth request cookie
        Cookie authCookie = new Cookie(OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME, 
                Base64.getUrlEncoder().encodeToString(serialize(authorizationRequest)));
        authCookie.setPath("/");
        authCookie.setHttpOnly(true);
        authCookie.setMaxAge(COOKIE_EXPIRE_SECONDS);
        response.addCookie(authCookie);

        // Save redirect URI if provided
        String redirectUriParam = request.getParameter("redirect_uri");
        if (redirectUriParam != null && !redirectUriParam.isEmpty()) {
            logger.debug("Saving redirect URI: {}", redirectUriParam);
            Cookie redirectUriCookie = new Cookie(REDIRECT_URI_PARAM_COOKIE_NAME, redirectUriParam);
            redirectUriCookie.setPath("/");
            redirectUriCookie.setMaxAge(COOKIE_EXPIRE_SECONDS);
            redirectUriCookie.setHttpOnly(true);
            response.addCookie(redirectUriCookie);
        }
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request, HttpServletResponse response) {
        logger.debug("Removing authorization request");
        OAuth2AuthorizationRequest authRequest = loadAuthorizationRequest(request);
        removeOAuth2AuthorizationRequestCookies(request, response);
        return authRequest;
    }

    public void removeOAuth2AuthorizationRequestCookies(HttpServletRequest request, HttpServletResponse response) {
        logger.debug("Removing all OAuth2 cookies");
        deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
        deleteCookie(request, response, REDIRECT_URI_PARAM_COOKIE_NAME);
        deleteCookie(request, response, OAUTH2_STATE_COOKIE_NAME);
    }

    public Optional<String> getRedirectUri(HttpServletRequest request) {
        return getCookieValue(request, REDIRECT_URI_PARAM_COOKIE_NAME);
    }
    
    public Optional<String> getState(HttpServletRequest request) {
        return getCookieValue(request, OAUTH2_STATE_COOKIE_NAME);
    }

    private Optional<String> getCookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (name.equals(cookie.getName())) {
                    return Optional.of(cookie.getValue());
                }
            }
        }
        return Optional.empty();
    }

    private void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (name.equals(cookie.getName())) {
                    cookie.setValue("");
                    cookie.setPath("/");
                    cookie.setMaxAge(0);
                    response.addCookie(cookie);
                    logger.debug("Deleted cookie: {}", name);
                    break;
                }
            }
        }
    }

    private <T> byte[] serialize(T obj) {
        return SerializationUtils.serialize(obj);
    }

    private <T> T deserialize(byte[] bytes, Class<T> cls) {
        return cls.cast(SerializationUtils.deserialize(bytes));
    }
} 