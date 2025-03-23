package com.birkann.service;

import java.util.List;
import java.util.Map;

public interface IEmailService {
    /**
     * Kullanıcının Gmail hesabından e-postaları çeker
     * @param accessToken OAuth2 access token
     * @param maxResults Çekilecek maksimum e-posta sayısı
     * @return E-posta listesi (Konu, Gönderen, Tarih, İçerik)
     */
    List<Map<String, String>> fetchEmails(String accessToken, int maxResults);
    
    /**
     * Kullanıcının Gmail hesabından belirli bir e-postanın detaylarını çeker
     * @param accessToken OAuth2 access token
     * @param emailId E-posta ID
     * @return E-posta detayları (Konu, Gönderen, Tarih, İçerik)
     */
    Map<String, String> fetchEmailDetails(String accessToken, String emailId);
} 