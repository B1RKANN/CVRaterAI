package com.birkann.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/oauth2/callback")
public class OAuth2ErrorController {
    
    private static final Logger logger = LoggerFactory.getLogger(OAuth2ErrorController.class);
    
    /**
     * OAuth2 hata durumlarını işler
     * @param error Hata mesajı
     * @return Hata yanıtı
     */
    @GetMapping("/google")
    public ResponseEntity<String> handleOAuth2Error(@RequestParam(value = "error", required = false) String error) {
        if (error != null && !error.isEmpty()) {
            logger.error("OAuth2 kimlik doğrulama hatası: {}", error);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("OAuth2 kimlik doğrulama hatası: " + error);
        }
        
        // Eğer hata yoksa 200 OK döndür
        return ResponseEntity.ok("OAuth2 kimlik doğrulama başarılı");
    }
} 