package com.birkann.service;

import com.birkann.model.User;

public interface IUserService {
    
    /**
     * Şu anki oturum açmış kullanıcıyı getirir
     * @return Oturum açmış kullanıcı veya null
     */
    User getCurrentUser();
    
    /**
     * Kullanıcının istediği kaynağa erişim yetkisi olup olmadığını kontrol eder
     * @param userId Erişilmek istenen kaynağın kullanıcı ID'si
     * @return Erişim izni varsa true, yoksa false
     */
    boolean canAccessUser(Long userId);
} 