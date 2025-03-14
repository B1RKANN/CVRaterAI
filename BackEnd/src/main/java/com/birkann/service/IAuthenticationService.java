package com.birkann.service;

import com.birkann.dto.AuthRequest;
import com.birkann.dto.AuthResponse;
import com.birkann.dto.DtoUser;
import com.birkann.dto.RefreshTokenRequest;
import com.birkann.dto.RegisterRequest;

public interface IAuthenticationService {
	
	public DtoUser register(RegisterRequest input);
	
	public AuthResponse authenticate(AuthRequest input);
	
	public AuthResponse refreshToken(RefreshTokenRequest input);
	
	
}
