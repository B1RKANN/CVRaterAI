package com.birkann.controller.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.birkann.controller.IRestAuthenticationController;
import com.birkann.controller.RestBaseController;
import com.birkann.controller.RootEntity;
import com.birkann.dto.AuthRequest;
import com.birkann.dto.AuthResponse;
import com.birkann.dto.DtoUser;
import com.birkann.dto.RefreshTokenRequest;
import com.birkann.dto.RegisterRequest;
import com.birkann.service.IAuthenticationService;

import jakarta.validation.Valid;

@RestController
public class RestAuthenticationImpl extends RestBaseController implements IRestAuthenticationController {
	
	@Autowired
	private IAuthenticationService authenticationService;
	
	
	@PostMapping("/register")
	@Override
	public RootEntity<DtoUser> register(@Valid @RequestBody RegisterRequest input) {
		return ok(authenticationService.register(input));
	}


	@PostMapping("/authenticate")
	@Override
	public RootEntity<AuthResponse> authenticate(@Valid @RequestBody AuthRequest input) {
		// TODO Auto-generated method stub
		return ok(authenticationService.authenticate(input));
	}

	
	@PostMapping("/refreshToken")
	@Override
	public RootEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest input) {
		return ok(authenticationService.refreshToken(input));
	}
	
}
