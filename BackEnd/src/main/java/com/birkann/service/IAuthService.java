package com.birkann.service;

import com.birkann.dto.AuthRequest;
import com.birkann.dto.AuthResponse;
import com.birkann.dto.RefreshTokenRequest;
import com.birkann.dto.RegisterRequest;
import com.birkann.model.User;
import jakarta.servlet.http.HttpServletResponse;

public interface IAuthService {
    
    /**
     * Kullanıcı kaydı yapar
     * @param request Kayıt bilgileri
     * @return JWT token içeren yanıt
     */
    AuthResponse signup(RegisterRequest request);
    
    /**
     * Kullanıcı kaydı yapar ve token'ı cookie olarak ayarlar
     * @param request Kayıt bilgileri
     * @param response HTTP response
     * @return JWT token içeren yanıt
     */
    AuthResponse signupWithCookie(RegisterRequest request, HttpServletResponse response);
    
    /**
     * Kullanıcı girişi yapar
     * @param request Giriş bilgileri
     * @return JWT token içeren yanıt
     */
    AuthResponse signin(AuthRequest request);
    
    /**
     * Kullanıcı girişi yapar ve token'ı cookie olarak ayarlar
     * @param request Giriş bilgileri
     * @param response HTTP response
     * @return JWT token içeren yanıt
     */
    AuthResponse signinWithCookie(AuthRequest request, HttpServletResponse response);
    
    /**
     * Token yenileme işlemi
     * @param request Token yenileme isteği
     * @return Yeni JWT token içeren yanıt
     */
    AuthResponse refreshToken(RefreshTokenRequest request);
    
    /**
     * Token yenileme işlemi, cookie kullanarak
     * @param request Token yenileme isteği
     * @param response HTTP response
     * @return Yeni JWT token içeren yanıt
     */
    AuthResponse refreshTokenWithCookie(RefreshTokenRequest request, HttpServletResponse response);
    
    /**
     * Çıkış yapma işlemi, cookie'yi temizler
     * @param response HTTP response
     */
    void logout(HttpServletResponse response);
    
    /**
     * Admin kullanıcı oluşturur
     * @param request Kayıt bilgileri
     * @return Oluşturulan admin kullanıcı
     */
    User createAdminUser(RegisterRequest request);
} 