package com.birkann.service;

import org.springframework.security.core.userdetails.UserDetails;

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
} 