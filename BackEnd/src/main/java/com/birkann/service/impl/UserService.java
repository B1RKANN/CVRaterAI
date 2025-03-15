package com.birkann.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.birkann.model.Role;
import com.birkann.model.User;
import com.birkann.repository.UserRepository;
import com.birkann.service.IUserService;

@Service
public class UserService implements IUserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warn("Kimlik doğrulama bulunamadı veya kimlik doğrulanmadı");
            return null;
        }
        
        logger.debug("Kimlik doğrulama adı: {}", authentication.getName());
        User user = userRepository.findByEmail(authentication.getName()).orElse(null);
        
        if (user == null) {
            logger.warn("Kullanıcı bulunamadı: {}", authentication.getName());
        } else {
            logger.debug("Kullanıcı bulundu: id={}, email={}, role={}", user.getId(), user.getEmail(), user.getRole());
        }
        
        return user;
    }
    
    /**
     * Kullanıcının istediği kaynağa erişim yetkisi olup olmadığını kontrol eder
     * @param userId Erişilmek istenen kaynağın kullanıcı ID'si
     * @return Erişim izni varsa true, yoksa false
     */
    @Override
    public boolean canAccessUser(Long userId) {
        if (userId == null) {
            logger.warn("Erişim kontrolü - userId null!");
            return false;
        }
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warn("Erişim kontrolü - Kimlik doğrulama bulunamadı veya kimlik doğrulanmadı");
            return false;
        }
        
        logger.debug("Erişim kontrolü - Kimlik doğrulama adı: {}, yetkileri: {}", 
            authentication.getName(), authentication.getAuthorities());
        
        // Kullanıcı ADMIN rolüne sahipse tüm kaynaklara erişebilir
        SimpleGrantedAuthority adminAuthority = new SimpleGrantedAuthority("ROLE_ADMIN");
        boolean isAdmin = authentication.getAuthorities().contains(adminAuthority);
        
        if (isAdmin) {
            logger.debug("Erişim kontrolü - Kullanıcı ADMIN rolüne sahip, erişim izni verildi");
            return true;
        }
        
        // Normal kullanıcılar sadece kendi kaynaklarına erişebilir
        User currentUser = userRepository.findByEmail(authentication.getName()).orElse(null);
        if (currentUser == null) {
            logger.warn("Erişim kontrolü - Kullanıcı bulunamadı: {}", authentication.getName());
            return false;
        }
        
        boolean hasAccess = currentUser.getId().equals(userId);
        
        logger.debug("Erişim kontrolü - Kullanıcı: id={}, email={}, role={}, istemci userId={}, erişim: {}", 
            currentUser.getId(), currentUser.getEmail(), currentUser.getRole(), userId, hasAccess);
        
        return hasAccess;
    }
} 