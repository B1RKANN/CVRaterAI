package com.birkann.dto.response;

import lombok.Data;

@Data
public class JWTAuthenticationResponse {
    private String token;
    private String refreshToken;
    private String role; // Kullanıcı rolünü içeren alan
} 