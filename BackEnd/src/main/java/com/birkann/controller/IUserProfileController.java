package com.birkann.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.birkann.dto.response.UserResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping("/api/v1/profile")
@Tag(name = "User Profile", description = "Kullanıcı profil bilgilerini yönetir")
public interface IUserProfileController {
    
    /**
     * Kullanıcı profilini ID'ye göre getir
     * 
     * @param id Kullanıcı ID'si
     * @return Kullanıcı bilgileri
     */
    @Operation(summary = "Kullanıcı profilini getirir", description = "Verilen ID'ye göre kullanıcı profilini döndürür")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "İşlem başarılı", 
                     content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Kullanıcı bulunamadı"),
        @ApiResponse(responseCode = "500", description = "Sunucu hatası")
    })
    @GetMapping("/{id}")
    @PreAuthorize("@userService.canAccessUser(#id) or hasRole('ADMIN')")
    ResponseEntity<RootEntity<UserResponseDTO>> getUserProfile(
        @Parameter(description = "Kullanıcı ID", required = true) 
        @PathVariable Long id);
} 