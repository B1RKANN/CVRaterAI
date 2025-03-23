package com.birkann.oauth2;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.birkann.model.Role;
import com.birkann.model.User;
import com.birkann.repository.UserRepository;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomOAuth2UserService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);
        
        try {
            return processOAuth2User(oAuth2UserRequest, oAuth2User);
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            // OAuth2 sağlayıcısıyla ilgili bir sorun varsa fırlat
            logger.error("OAuth2 işlemi sırasında hata: {}", ex.getMessage());
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex);
        }
    }
    
    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        // Debug için gelen tüm özellikleri logla
        logger.debug("OAuth2 kullanıcı özellikleri: {}", oAuth2User.getAttributes());
        
        String email = (String) oAuth2User.getAttributes().get("email");
        
        if (email == null || email.isEmpty()) {
            logger.error("Email adresi OAuth2 yanıtında bulunamadı");
            throw new OAuth2AuthenticationException("Email adresi bulunamadı");
        }
        
        logger.info("Kullanıcı kimlik doğrulama - email: {}", email);
        
        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;
        
        if (userOptional.isPresent()) {
            user = userOptional.get();
            logger.info("Mevcut kullanıcı bulundu, email: {}, id: {}", email, user.getId());
            
            // Mevcut kullanıcının bilgilerini güncelle
            updateExistingUser(user, oAuth2User);
        } else {
            logger.info("Yeni kullanıcı oluşturuluyor, email: {}", email);
            user = registerNewUser(oAuth2UserRequest, oAuth2User);
            if (user != null && user.getId() != null) {
                logger.info("Yeni kullanıcı başarıyla oluşturuldu, id: {}", user.getId());
            } else {
                logger.error("Kullanıcı oluşturulamadı, email: {}", email);
                throw new InternalAuthenticationServiceException("Kullanıcı kaydedilemedi");
            }
        }
        
        return UserPrincipal.create(user, oAuth2User.getAttributes());
    }
    
    private void updateExistingUser(User user, OAuth2User oAuth2User) {
        try {
            // Kullanıcı bilgilerini güncelle
            String name = (String) oAuth2User.getAttributes().get("name");
            String picture = (String) oAuth2User.getAttributes().get("picture");
            
            if (name != null && !name.isEmpty()) {
                user.setName(name);
            }
            
            // Diğer bilgileri de güncelleyebilirsiniz (profil resmi vb.)
            
            userRepository.save(user);
            logger.info("Kullanıcı bilgileri güncellendi, email: {}", user.getEmail());
        } catch (Exception e) {
            logger.error("Kullanıcı bilgileri güncellenirken hata oluştu: {}", e.getMessage(), e);
        }
    }
    
    private User registerNewUser(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        User user = new User();
        
        try {
            // Temel kullanıcı bilgilerini ayarla
            String email = (String) oAuth2User.getAttributes().get("email");
            String name = (String) oAuth2User.getAttributes().get("name");
            String picture = (String) oAuth2User.getAttributes().get("picture");
            
            user.setEmail(email);
            user.setName(name);
            user.setRole(Role.USER); // Varsayılan kullanıcı rolü
            
            // OAuth2 bilgilerini ekle
            String registrationId = oAuth2UserRequest.getClientRegistration().getRegistrationId();
            user.setOAuth2RegistrationId(registrationId);
            
            logger.info("Yeni kullanıcı kaydediliyor: {}, registrationId: {}", email, registrationId);
            
            // Kullanıcıyı kaydet
            User savedUser = userRepository.save(user);
            
            if (savedUser == null || savedUser.getId() == null) {
                logger.error("Kullanıcı veritabanına kaydedilemedi, email: {}", email);
                throw new RuntimeException("Kullanıcı kaydedilemedi");
            }
            
            logger.info("Kullanıcı başarıyla veritabanına kaydedildi, id: {}, email: {}", savedUser.getId(), email);
            return savedUser;
        } catch (Exception e) {
            logger.error("Yeni kullanıcı kaydedilirken hata oluştu: {}", e.getMessage(), e);
            throw new InternalAuthenticationServiceException("Kullanıcı kaydedilirken hata: " + e.getMessage(), e);
        }
    }
} 