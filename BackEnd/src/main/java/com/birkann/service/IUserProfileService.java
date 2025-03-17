package com.birkann.service;

import com.birkann.dto.response.UserResponseDTO;

public interface IUserProfileService {
    /**
     * Kullanıcı bilgilerini ID'ye göre getir
     * @param id Kullanıcı ID
     * @return Kullanıcı bilgileri
     */
    UserResponseDTO getUserProfile(Long id);
} 