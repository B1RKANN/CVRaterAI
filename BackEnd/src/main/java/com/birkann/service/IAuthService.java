package com.birkann.service;

import com.birkann.dto.AuthRequest;
import com.birkann.dto.AuthResponse;
import com.birkann.dto.RefreshTokenRequest;
import com.birkann.dto.RegisterRequest;
import com.birkann.model.User;

public interface IAuthService {
    
    /**
     * Kullanıcı kaydı yapar
     * @param request Kayıt bilgileri
     * @return JWT token içeren yanıt
     */
    AuthResponse signup(RegisterRequest request);
    
    /**
     * Kullanıcı girişi yapar
     * @param request Giriş bilgileri
     * @return JWT token içeren yanıt
     */
    AuthResponse signin(AuthRequest request);
    
    /**
     * Token yenileme işlemi
     * @param request Token yenileme isteği
     * @return Yeni JWT token içeren yanıt
     */
    AuthResponse refreshToken(RefreshTokenRequest request);
    
    /**
     * Admin kullanıcı oluşturur
     * @param request Kayıt bilgileri
     * @return Oluşturulan admin kullanıcı
     */
    User createAdminUser(RegisterRequest request);
} 