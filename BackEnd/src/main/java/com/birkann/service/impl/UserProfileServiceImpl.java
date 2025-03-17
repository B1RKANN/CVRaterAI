package com.birkann.service.impl;

import org.springframework.stereotype.Service;

import com.birkann.dto.response.UserResponseDTO;
import com.birkann.enums.PlanType;
import com.birkann.model.User;
import com.birkann.repository.UserRepository;
import com.birkann.service.IUserProfileService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements IUserProfileService {

    private final UserRepository userRepository;

    @Override
    public UserResponseDTO getUserProfile(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + id));
        
        UserResponseDTO response = UserResponseDTO.builder()
            .name(user.getName())
            .email(user.getEmail())
            .build();
        
        response.setId(user.getId());
        
        // Kullanıcının kredi bilgileri varsa ekle
        if (user.getCredit() != null) {
            response.setUserCredit(user.getCredit().getUserCredit());
            response.setPlanType(user.getCredit().getPlanType());
        } else {
            // Varsayılan değerler
            response.setUserCredit(15);
            response.setPlanType(PlanType.FREE);
        }
        
        return response;
    }
} 