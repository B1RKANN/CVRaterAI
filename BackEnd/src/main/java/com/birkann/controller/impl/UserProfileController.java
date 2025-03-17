package com.birkann.controller.impl;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.birkann.controller.IUserProfileController;
import com.birkann.controller.RestBaseController;
import com.birkann.controller.RootEntity;
import com.birkann.dto.response.UserResponseDTO;
import com.birkann.service.IUserProfileService;
import com.birkann.service.IUserService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Profile", description = "Kullanıcı profil bilgileri API'si")
public class UserProfileController extends RestBaseController implements IUserProfileController {

    private final IUserProfileService userProfileService;
    private final IUserService userService;

    @Override
    @PreAuthorize("@userService.canAccessUser(#id) or hasRole('ADMIN')")
    public ResponseEntity<RootEntity<UserResponseDTO>> getUserProfile(Long id) {
        log.info("Retrieving user profile for ID: {}", id);
        try {
            UserResponseDTO userProfile = userProfileService.getUserProfile(id);
            return ResponseEntity.ok(ok(userProfile));
        } catch (Exception e) {
            log.error("Error retrieving user profile: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(error(e.getMessage()));
        }
    }
} 