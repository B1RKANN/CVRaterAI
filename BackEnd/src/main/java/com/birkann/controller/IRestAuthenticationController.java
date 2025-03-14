package com.birkann.controller;

import com.birkann.dto.AuthRequest;
import com.birkann.dto.AuthResponse;
import com.birkann.dto.DtoUser;
import com.birkann.dto.RefreshTokenRequest;
import com.birkann.dto.RegisterRequest;

public interface IRestAuthenticationController {
	
	public RootEntity<DtoUser> register(RegisterRequest input);
	
	public RootEntity<AuthResponse> authenticate(AuthRequest input);
	
	public RootEntity<AuthResponse> refreshToken(RefreshTokenRequest input);

	
	
}
