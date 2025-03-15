package com.birkann.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import com.birkann.dto.AuthRequest;
import com.birkann.dto.AuthResponse;
import com.birkann.dto.DtoUser;
import com.birkann.dto.RefreshTokenRequest;
import com.birkann.dto.RegisterRequest;
import com.birkann.model.User;

import jakarta.validation.Valid;

/**
 * REST kimlik doğrulama controller interface'i
 */
public interface IRestAuthenticationController {
    
    /**
     * Kullanıcı kaydı yapar
     * @param request Kayıt bilgileri
     * @return JWT token içeren yanıt
     */
    RootEntity<DtoUser> register(@Valid @RequestBody RegisterRequest request);
    
    /**
     * Kullanıcı girişi yapar
     * @param request Giriş bilgileri
     * @return JWT token içeren yanıt
     */
    RootEntity<AuthResponse> authenticate(@Valid @RequestBody AuthRequest request);
    
    /**
     * Token yenileme işlemi yapar
     * @param request Token yenileme isteği
     * @return Yeni JWT token içeren yanıt
     */
    RootEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request);
    
    /**
     * Admin kullanıcı oluşturur - sadece ADMIN rolüne sahip kullanıcılar erişebilir
     * @param request Kayıt bilgileri
     * @return Oluşturulan admin kullanıcı
     */
    ResponseEntity<User> createAdminUser(@RequestBody RegisterRequest request);
}
