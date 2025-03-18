package com.birkann.controller.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
import com.birkann.model.User;
import com.birkann.service.IAuthService;
import com.birkann.service.IAuthenticationService;

import jakarta.validation.Valid;

@RestController
public class RestAuthenticationImpl extends RestBaseController implements IRestAuthenticationController {
	
	@Autowired
	private IAuthenticationService authenticationService;
	
	@Autowired
    private IAuthService authService;
	
	@Override
	public RootEntity<DtoUser> register(@Valid @RequestBody RegisterRequest input) {
		throw new UnsupportedOperationException("Bu endpoint artık kullanımda değil, lütfen /auth/v2/register endpoint'ini kullanın");
	}

	@Override
	public RootEntity<AuthResponse> authenticate(@Valid @RequestBody AuthRequest input) {
		throw new UnsupportedOperationException("Bu endpoint artık kullanımda değil, lütfen /auth/v2/authenticate endpoint'ini kullanın");
	}
	
	@Override
	public RootEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest input) {
		throw new UnsupportedOperationException("Bu endpoint artık kullanımda değil, lütfen /auth/v2/refreshToken endpoint'ini kullanın");
	}
	
	@Override
    public ResponseEntity<User> createAdminUser(@RequestBody RegisterRequest request) {
        throw new UnsupportedOperationException("Bu endpoint artık kullanımda değil, lütfen /auth/v2/admin/create endpoint'ini kullanın");
    }
}
