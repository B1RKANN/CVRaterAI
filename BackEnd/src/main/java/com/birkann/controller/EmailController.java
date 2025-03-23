package com.birkann.controller;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.birkann.model.User;
import com.birkann.repository.UserRepository;
import com.birkann.service.IEmailService;
import com.google.api.services.gmail.model.Message;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api")
@Tag(name = "Email", description = "Email API")
public class EmailController {

    private static final Logger logger = LoggerFactory.getLogger(EmailController.class);

    @Autowired
    private IEmailService emailService;

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;
    
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/emails")
    @Operation(
        summary = "Get emails from Gmail",
        description = "Fetches emails from a user's Gmail account using their OAuth2 access token",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved emails",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ArrayList.class)
                )
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Not authorized to access emails"
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Internal server error"
            )
        }
    )
    public ResponseEntity<?> getEmails(
            @Parameter(description = "Maximum number of emails to return") 
            @RequestParam(required = false) Integer maxResults,
            @Parameter(description = "Gmail query to filter emails") 
            @RequestParam(required = false) String q) {
        
        try {
            String accessToken = getAccessToken();
            if (accessToken == null) {
                logger.error("Access token not found");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Collections.singletonMap("error", "User not logged in via OAuth2 or access token not found"));
            }
            
            List<Map<String, String>> emails = emailService.fetchEmails(accessToken, maxResults != null ? maxResults : 10);
            return ResponseEntity.ok(emails);
            
        } catch (Exception e) {
            logger.error("Error fetching emails: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Failed to fetch emails: " + e.getMessage()));
        }
    }
    
    @PostMapping("/emails")
    @Operation(
        summary = "Get emails using provided OAuth2 token",
        description = "Fetches emails from a user's Gmail account using the OAuth2 access token provided in the request body",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved emails",
                content = @Content(
                    mediaType = "application/json", 
                    schema = @Schema(implementation = ArrayList.class)
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid request - token missing"
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Internal server error"
            )
        }
    )
    public ResponseEntity<?> getEmailsWithToken(
            @RequestBody Map<String, Object> requestBody,
            @RequestParam(required = false) Integer maxResults,
            @RequestParam(required = false) String q) {
        
        logger.info("Received request to /api/emails with body: {}", requestBody);
        
        String accessToken = null;
        
        // Request body'den OAuth2 token'ı alma
        if (requestBody != null && requestBody.containsKey("token")) {
            accessToken = requestBody.get("token").toString();
            logger.info("Token received from request body, length: {}", accessToken.length());
        } else {
            // Body'de token yoksa, authentication context'ten token'ı almayı dene
            accessToken = getAccessToken();
            if (accessToken == null) {
                logger.warn("No token provided in request body and no authentication token found");
                
                // Örnek veri döndür
                List<Map<String, String>> exampleEmails = new ArrayList<>();
                Map<String, String> exampleEmail = new HashMap<>();
                exampleEmail.put("id", "example123");
                exampleEmail.put("subject", "Example Email - Please provide OAuth2 token");
                exampleEmail.put("from", "example@example.com");
                exampleEmail.put("snippet", "This is an example email. Please provide your OAuth2 token in the request body to access your real emails.");
                exampleEmail.put("receivedDate", Instant.now().toString());
                exampleEmails.add(exampleEmail);
                
                return ResponseEntity.status(HttpStatus.OK)
                        .body(exampleEmails);
            }
        }
        
        try {
            List<Map<String, String>> emails = emailService.fetchEmails(accessToken, maxResults != null ? maxResults : 10);
            return ResponseEntity.ok(emails);
        } catch (Exception e) {
            logger.error("Error fetching emails with provided token: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Failed to fetch emails: " + e.getMessage()));
        }
    }

    @GetMapping("/email/{id}")
    @Operation(
        summary = "Get email details",
        description = "Fetches the details of a specific email by its ID",
        responses = {
            @ApiResponse(
                responseCode = "200", 
                description = "Successfully retrieved email details"
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Not authorized to access this email"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Email not found"
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Internal server error"
            )
        }
    )
    public ResponseEntity<?> getEmailDetails(
            @Parameter(description = "ID of the email to retrieve") 
            @PathVariable String id) {
        
        try {
            String accessToken = getAccessToken();
            if (accessToken == null) {
                logger.error("Access token not found");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Collections.singletonMap("error", "User not logged in via OAuth2 or access token not found"));
            }
            
            Map<String, String> email = emailService.fetchEmailDetails(accessToken, id);
            if (email == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Collections.singletonMap("error", "Email not found"));
            }
            
            return ResponseEntity.ok(email);
            
        } catch (Exception e) {
            logger.error("Error fetching email details: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Failed to fetch email details: " + e.getMessage()));
        }
    }
    
    @GetMapping("/auth-debug")
    @Operation(
        summary = "Debug authentication details",
        description = "Returns debug information about the current authentication context",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved authentication debug information"
            )
        }
    )
    public ResponseEntity<?> getAuthDebug() {
        Map<String, Object> debugInfo = new HashMap<>();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null) {
            debugInfo.put("isAuthenticated", auth.isAuthenticated());
            debugInfo.put("principal", auth.getPrincipal().toString());
            debugInfo.put("authorities", auth.getAuthorities().toString());
            debugInfo.put("details", auth.getDetails() != null ? auth.getDetails().toString() : "null");
            debugInfo.put("name", auth.getName());
            
            if (auth instanceof OAuth2AuthenticationToken) {
                OAuth2AuthenticationToken oauth2Auth = (OAuth2AuthenticationToken) auth;
                debugInfo.put("OAuth2RegistrationId", oauth2Auth.getAuthorizedClientRegistrationId());
                
                OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                        oauth2Auth.getAuthorizedClientRegistrationId(), oauth2Auth.getName());
                
                if (client != null) {
                    OAuth2AccessToken accessToken = client.getAccessToken();
                    debugInfo.put("accessTokenIssuedAt", accessToken.getIssuedAt());
                    debugInfo.put("accessTokenExpiresAt", accessToken.getExpiresAt());
                    debugInfo.put("refreshTokenAvailable", client.getRefreshToken() != null);
                }
            }
            
            // Kullanıcı bilgilerini veritabanından kontrol et
            try {
                Optional<User> userOpt = userRepository.findByEmail(auth.getName());
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    debugInfo.put("userFoundInDb", true);
                    debugInfo.put("userId", user.getId());
                    debugInfo.put("userEmail", user.getEmail());
                    debugInfo.put("userRole", user.getRole());
                    debugInfo.put("hasOAuth2Token", user.getOAuth2AccessToken() != null);
                    debugInfo.put("OAuth2TokenExpiresAt", user.getOAuth2TokenExpiresAt());
                    debugInfo.put("hasRefreshToken", user.getOAuth2RefreshToken() != null);
                } else {
                    debugInfo.put("userFoundInDb", false);
                }
            } catch (Exception e) {
                debugInfo.put("dbLookupError", e.getMessage());
            }
        } else {
            debugInfo.put("isAuthenticated", false);
            debugInfo.put("error", "No authentication found in SecurityContext");
        }
        
        return ResponseEntity.ok(debugInfo);
    }
    
    private String getAccessToken() {
        logger.info("Getting access token...");
        
        // 1. Önce SecurityContext'ten OAuth2 token'ı almayı dene
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauth2Authentication = (OAuth2AuthenticationToken) authentication;
            String registrationId = oauth2Authentication.getAuthorizedClientRegistrationId();
            
            OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                    registrationId, oauth2Authentication.getName());
            
            if (client != null && client.getAccessToken() != null) {
                logger.info("Retrieved access token from OAuth2AuthorizedClient");
                return client.getAccessToken().getTokenValue();
            }
            
            logger.warn("No access token found in OAuth2AuthorizedClient");
        }
        
        // 2. SecurityContext'ten token alınamazsa, veritabanındaki kullanıcıdan almayı dene
        if (authentication != null) {
            String email = authentication.getName();
            logger.info("Looking up user in database by email: {}", email);
            
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                
                if (user.getOAuth2AccessToken() != null) {
                    // Token süresi dolmuş mu kontrol et
                    if (user.isOAuth2TokenExpired()) {
                        logger.warn("OAuth2 token expired for user: {}", email);
                        return null;
                    }
                    
                    logger.info("Retrieved access token from database for user: {}", email);
                    return user.getOAuth2AccessToken();
                }
                
                logger.warn("No OAuth2 token found in database for user: {}", email);
            } else {
                logger.warn("User not found in database: {}", email);
            }
        }
        
        logger.error("Failed to get access token from any source");
        return null;
    }
} 