package com.birkann.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.birkann.dto.AuthRequest;
import com.birkann.dto.AuthResponse;
import com.birkann.dto.RefreshTokenRequest;
import com.birkann.dto.RegisterRequest;
import com.birkann.model.User;
import com.birkann.service.IAuthService;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/auth/v2")
public class RestAuthController {

    private static final Logger logger = LoggerFactory.getLogger(RestAuthController.class);
    
    @Autowired
    private IAuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        logger.info("Kullanıcı kaydı isteği alındı: email={}", request.getEmail());
        System.out.println("RestAuthController - register metodu çağrıldı: " + request.getEmail());
        return ResponseEntity.ok(authService.signup(request));
    }
    
    /**
     * Kullanıcı kaydı yapar ve token'ı HTTP-only cookie olarak ayarlar
     */
    @PostMapping("/register-with-cookie")
    public ResponseEntity<AuthResponse> registerWithCookie(@RequestBody RegisterRequest request, HttpServletResponse response) {
        logger.info("Cookie ile kullanıcı kaydı isteği alındı: email={}", request.getEmail());
        return ResponseEntity.ok(authService.signupWithCookie(request, response));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthResponse> authenticate(@RequestBody AuthRequest request) {
        logger.info("Kimlik doğrulama isteği alındı: email={}", request.getEmail());
        return ResponseEntity.ok(authService.signin(request));
    }
    
    /**
     * Kullanıcı girişi yapar ve token'ı HTTP-only cookie olarak ayarlar
     */
    @PostMapping("/authenticate-with-cookie")
    public ResponseEntity<AuthResponse> authenticateWithCookie(@RequestBody AuthRequest request, HttpServletResponse response) {
        logger.info("Cookie ile kimlik doğrulama isteği alındı: email={}", request.getEmail());
        return ResponseEntity.ok(authService.signinWithCookie(request, response));
    }

    @PostMapping("/refreshToken")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        logger.info("Token yenileme isteği alındı");
        return ResponseEntity.ok(authService.refreshToken(request));
    }
    
    /**
     * Token yenileme işlemi yapar ve token'ı HTTP-only cookie olarak ayarlar
     */
    @PostMapping("/refreshToken-with-cookie")
    public ResponseEntity<AuthResponse> refreshTokenWithCookie(@RequestBody RefreshTokenRequest request, HttpServletResponse response) {
        logger.info("Cookie ile token yenileme isteği alındı");
        return ResponseEntity.ok(authService.refreshTokenWithCookie(request, response));
    }
    
    /**
     * Çıkış yapar, HTTP-only cookie'yi temizler
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        logger.info("Çıkış isteği alındı");
        authService.logout(response);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Admin kullanıcı oluşturur - sadece ADMIN rolüne sahip kullanıcılar erişebilir
     * @param request Kayıt bilgileri
     * @return Oluşturulan admin kullanıcı
     */
    @PostMapping("/admin/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> createAdminUser(@RequestBody RegisterRequest request) {
        logger.info("Admin kullanıcı oluşturma isteği alındı: email={}", request.getEmail());
        return ResponseEntity.ok(authService.createAdminUser(request));
    }
} 