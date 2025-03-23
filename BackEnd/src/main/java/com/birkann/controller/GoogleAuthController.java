package com.birkann.controller;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import com.birkann.model.User;
import com.birkann.oauth2.OAuth2Utils;
import com.birkann.oauth2.UserPrincipal;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth/google")
@RequiredArgsConstructor
public class GoogleAuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(GoogleAuthController.class);
    
    private final OAuth2Utils oAuth2Utils;
    
    /**
     * Google OAuth2 yetkilendirme URL'sine yönlendirir
     * @return Google OAuth2 yetkilendirme sayfasına yönlendirme
     */
    @GetMapping("/login")
    public RedirectView loginWithGoogle() {
        logger.info("Received Google login request");
        
        String redirectUri = "http://localhost:3000/oauth/callback";
        
        // OAuth2RequestRepository sınıfı Spring Security OAuth2 filtresi tarafından kullanılacak
        // Bu nedenle yetkilendirme URL'sini burada manuel olarak oluşturmak yerine,
        // Spring'in OAuth2 yetkilendirme URL'sine yönlendireceğiz
        
        // Spring Security OAuth2 Login akışını başlatmak için kullanılan endpoint
        String authorizationUrl = "/oauth2/authorize/google";
        
        logger.info("Redirecting to Spring Security OAuth2 endpoint: {}", authorizationUrl);
        
        return new RedirectView(authorizationUrl);
    }
    
    @GetMapping("/url")
    public ResponseEntity<String> getGoogleAuthUrl() {
        logger.info("Fetching Google authorization URL");
        
        String redirectUri = "http://localhost:3000/oauth/callback";
        String authorizationUrl = oAuth2Utils.buildGoogleAuthorizationUrl(redirectUri);
        
        logger.info("Generated Google authorization URL: {}", authorizationUrl);
        
        return ResponseEntity.ok(authorizationUrl);
    }
    
    /**
     * Mevcut oturumdaki kullanıcı bilgilerini döndürür
     * @param oauth2User OAuth2User nesnesi
     * @return Kullanıcı bilgileri
     */
    @GetMapping("/me")
    public ResponseEntity<Object> getCurrentUser(@AuthenticationPrincipal Object principal) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        logger.debug("Kimlik doğrulama kontrolü: {}", authentication != null ? "var" : "yok");
        
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warn("Kullanıcı oturum açmamış");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Kullanıcı oturum açmamış");
        }
        
        logger.debug("Principal tipi: {}", principal != null ? principal.getClass().getName() : "null");
        
        if (principal instanceof UserPrincipal) {
            UserPrincipal userPrincipal = (UserPrincipal) principal;
            User user = userPrincipal.getUser();
            logger.debug("Kullanıcı bilgileri: id={}, email={}", userPrincipal.getId(), userPrincipal.getUsername());
            return ResponseEntity.ok(user);
        } else {
            logger.warn("Beklenen principal türü (UserPrincipal) bulunamadı");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Principal türü uyumsuz");
        }
    }
} 