package com.birkann.service;

import org.springframework.security.core.userdetails.UserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * JWT token işlemleri için servis arayüzü
 */
public interface IJWTService {
    
    /**
     * JWT token'dan kullanıcı adını çıkarır
     * @param token JWT token
     * @return Kullanıcı adı
     */
    String extractUsername(String token);
    
    /**
     * JWT token'ın geçerli olup olmadığını kontrol eder
     * @param token JWT token
     * @param userDetails Kullanıcı detayları
     * @return Token geçerli ise true, değilse false
     */
    boolean isTokenValid(String token, UserDetails userDetails);
    
    /**
     * Kullanıcı detaylarına göre JWT token oluşturur
     * @param userDetails Kullanıcı detayları
     * @return Oluşturulan JWT token
     */
    String generateToken(UserDetails userDetails);
    
    /**
     * JWT token'ı HTTP response'a cookie olarak ekler
     * @param response HTTP servlet response
     * @param token JWT token
     */
    void addTokenToCookie(HttpServletResponse response, String token);
    
    /**
     * HTTP request içindeki cookie'den JWT token'ı çıkarır
     * @param request HTTP servlet request
     * @return JWT token, eğer cookie yoksa null
     */
    String getTokenFromCookie(HttpServletRequest request);
    
    /**
     * HTTP response'dan JWT cookie'sini siler
     * @param response HTTP servlet response
     */
    void clearTokenCookie(HttpServletResponse response);
} 