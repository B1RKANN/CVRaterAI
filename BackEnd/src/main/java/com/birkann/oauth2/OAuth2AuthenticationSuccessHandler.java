package com.birkann.oauth2;

import java.io.IOException;
import java.net.URI;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.birkann.enums.PlanType;
import com.birkann.jwt.JWTService;
import com.birkann.model.Credit;
import com.birkann.model.Role;
import com.birkann.model.User;
import com.birkann.repository.CreditRepository;
import com.birkann.repository.UserRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(OAuth2AuthenticationSuccessHandler.class);
    
    private final JWTService jwtService;
    private final OAuth2RequestRepository oAuth2RequestRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CreditRepository creditRepository;
    
    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;
    
    @Value("${oauth2.redirectUri}")
    private String defaultRedirectUri;
    
    public OAuth2AuthenticationSuccessHandler(JWTService jwtService, OAuth2RequestRepository oAuth2RequestRepository) {
        this.jwtService = jwtService;
        this.oAuth2RequestRepository = oAuth2RequestRepository;
    }
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        logger.info("OAuth2 authentication successful for user: {}", authentication.getName());
        
        // Debug için isteği logla
        logRequestDetails(request);
        
        String targetUrl = determineTargetUrl(request, response, authentication);
        logger.info("Target URL before adding token: {}", targetUrl);
        
        if (response.isCommitted()) {
            logger.warn("Response has already been committed. Unable to redirect to " + targetUrl);
            return;
        }
        
        // Spring Security OIDC ile authentication.getPrincipal() DefaultOidcUser döndürür
        // Bu durumu ele almak için kontrol ekliyoruz
        String token;
        Object principal = authentication.getPrincipal();
        
        logger.info("Principal type: {}", principal.getClass().getName());
        
        if (principal instanceof UserPrincipal) {
            UserPrincipal userPrincipal = (UserPrincipal) principal;
            logger.info("Creating JWT token for UserPrincipal: {}", userPrincipal.getUsername());
            token = jwtService.generateToken(userPrincipal);
        } else if (principal instanceof org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser) {
            // DefaultOidcUser'dan gelen bilgilerle JWT oluşturuyor
            org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser oidcUser = 
                (org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser) principal;
            
            logger.info("Creating JWT token for DefaultOidcUser: {}", oidcUser.getEmail());
            
            // OAuth2 token bilgilerini kullanıcı ile ilişkilendir
            saveOAuth2TokenForUser(oidcUser.getEmail(), authentication);
            
            // DefaultOidcUser'dan JWT token oluştur
            token = jwtService.generateTokenFromOidcUser(oidcUser);
        } else {
            logger.error("Unexpected principal type: {}", principal.getClass().getName());
            throw new RuntimeException("Unsupported principal type: " + principal.getClass().getName());
        }
        
        logger.info("JWT token created successfully! Token length: {}", token.length());
        logger.info("JWT token (first 15 chars): {}...", token.substring(0, Math.min(token.length(), 15)));
        
        // Add token to cookie
        jwtService.addTokenToCookie(response, token);
        logger.info("JWT token added to cookie with name: {}", "jwt");
        
        // Add token to the redirect URL as query parameter
        targetUrl = UriComponentsBuilder.fromUriString(targetUrl)
                .queryParam("token", token)
                .build().toUriString();
        logger.info("Final redirect URL with token: {}", targetUrl);
        
        clearAuthenticationAttributes(request);
        oAuth2RequestRepository.removeOAuth2AuthorizationRequestCookies(request, response);
        
        logger.info("Redirecting to frontend with JWT token");
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
    
    private void logRequestDetails(HttpServletRequest request) {
        logger.debug("Request URI: {}", request.getRequestURI());
        logger.debug("Request query string: {}", request.getQueryString());
        logger.debug("Request method: {}", request.getMethod());
        
        // Headers
        logger.debug("Headers:");
        request.getHeaderNames().asIterator().forEachRemaining(headerName -> 
            logger.debug("  {}: {}", headerName, request.getHeader(headerName))
        );
        
        // Parameters
        logger.debug("Parameters:");
        request.getParameterNames().asIterator().forEachRemaining(paramName -> 
            logger.debug("  {}: {}", paramName, request.getParameter(paramName))
        );
    }
    
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        Optional<String> redirectUri = oAuth2RequestRepository.getRedirectUri(request);
        
        // Google'dan gelen state parametresini kontrol et
        String state = request.getParameter("state");
        logger.debug("OAuth2 state parametresi: {}", state);
        
        // State parametresi "google:http://localhost:3000/oauth/callback" formatında olabilir
        if (state != null && state.startsWith("google:")) {
            String frontendRedirectUri = state.substring("google:".length());
            logger.debug("State parametresinden çıkarılan frontend URI: {}", frontendRedirectUri);
            
            if (!frontendRedirectUri.isEmpty()) {
                logger.debug("Frontend yönlendirme URI'si kullanılıyor: {}", frontendRedirectUri);
                return frontendRedirectUri;
            }
        }
        
        // Eğer state parametresinden çıkarılamadıysa cookie'den veya varsayılandan al
        if (redirectUri.isPresent() && !isAuthorizedRedirectUri(redirectUri.get())) {
            logger.warn("Redirect URI {} is not authorized", redirectUri.get());
            throw new IllegalArgumentException("Sorry! We've got an Unauthorized Redirect URI and can't proceed with the authentication");
        }
        
        String targetUrl = redirectUri.orElse(defaultRedirectUri);
        logger.debug("Target URL determined: {}", targetUrl);
        return targetUrl;
    }
    
    private boolean isAuthorizedRedirectUri(String uri) {
        URI clientRedirectUri = URI.create(uri);
        URI authorizedUri = URI.create(defaultRedirectUri);
        
        // Only validate host and port
        boolean isAuthorized = authorizedUri.getHost().equalsIgnoreCase(clientRedirectUri.getHost())
                && authorizedUri.getPort() == clientRedirectUri.getPort();
        
        logger.debug("Redirect URI {} authorization check: {}", uri, isAuthorized);
        return isAuthorized;
    }
    
    /**
     * OAuth2 access token bilgilerini kullanıcı ile ilişkilendirip veritabanına kaydeder
     */
    private void saveOAuth2TokenForUser(String email, Authentication authentication) {
        try {
            if (!(authentication instanceof OAuth2AuthenticationToken)) {
                logger.error("Authentication, OAuth2AuthenticationToken tipinde değil");
                return;
            }
            
            OAuth2AuthenticationToken oauth2Authentication = (OAuth2AuthenticationToken) authentication;
            String registrationId = oauth2Authentication.getAuthorizedClientRegistrationId();
            logger.info("OAuth2 registration ID: {}", registrationId);
            
            // OAuth2AuthorizedClientService Bean'inden OAuth2 token bilgilerini al
            OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                    registrationId, oauth2Authentication.getName());
            
            if (client == null) {
                logger.error("OAuth2AuthorizedClient bulunamadı, kimlik: {}", oauth2Authentication.getName());
                return;
            }
            
            OAuth2AccessToken accessToken = client.getAccessToken();
            if (accessToken == null) {
                logger.error("Access token bulunamadı");
                return;
            }
            
            logger.info("OAuth2 token başarıyla alındı. Süresi: {}", accessToken.getExpiresAt());
            
            // Refresh token'ı alma
            OAuth2RefreshToken refreshToken = client.getRefreshToken();
            String refreshTokenValue = null;
            if (refreshToken != null) {
                refreshTokenValue = refreshToken.getTokenValue();
                logger.info("Refresh token başarıyla alındı");
            } else {
                logger.warn("Refresh token bulunamadı, OAuth2 provider refresh token sağlamıyor olabilir");
            }
            
            // Kullanıcıyı e-posta adresine göre bul
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (!userOpt.isPresent()) {
                logger.info("Kullanıcı veritabanında bulunamadı, yeni kullanıcı oluşturulacak: {}", email);
                
                // Yeni kredi oluştur
                Credit credit = createNewCredit();
                Credit savedCredit = creditRepository.save(credit);
                logger.info("Yeni kredi oluşturuldu: {}", savedCredit.getId());
                
                // Kullanıcı bulunamazsa yeni bir kullanıcı oluştur
                User newUser = new User();
                newUser.setEmail(email);
                
                if (authentication.getPrincipal() instanceof org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser) {
                    org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser oidcUser = 
                        (org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser) authentication.getPrincipal();
                    
                    newUser.setName(oidcUser.getAttribute("name"));
                } else {
                    newUser.setName(email); // Varsayılan olarak email'i kullan
                }
                
                newUser.setRole(Role.USER);
                newUser.setCredit(savedCredit); // Krediyi kullanıcıya ata
                
                // OAuth2 token bilgilerini set et
                newUser.setOAuth2AccessToken(accessToken.getTokenValue());
                newUser.setOAuth2TokenExpiresAt(accessToken.getExpiresAt());
                newUser.setOAuth2RegistrationId(registrationId);
                
                // Refresh token varsa onu da kaydet
                if (refreshTokenValue != null) {
                    newUser.setOAuth2RefreshToken(refreshTokenValue);
                    logger.info("Refresh token yeni kullanıcı için kaydedildi");
                }
                
                User savedUser = userRepository.save(newUser);
                logger.info("Yeni kullanıcı OAuth2 token bilgileriyle kaydedildi: {}, id: {}", email, savedUser.getId());
                return;
            }
            
            User user = userOpt.get();
            logger.info("Mevcut kullanıcı için OAuth2 token bilgileri güncelleniyor: {}", email);
            
            // Kullanıcının kredisi yoksa oluştur
            if (user.getCredit() == null) {
                Credit credit = createNewCredit();
                Credit savedCredit = creditRepository.save(credit);
                user.setCredit(savedCredit);
                logger.info("Mevcut kullanıcı için yeni kredi oluşturuldu: {}", savedCredit.getId());
            }
            
            // OAuth2 token bilgilerini güncelle
            user.setOAuth2AccessToken(accessToken.getTokenValue());
            user.setOAuth2TokenExpiresAt(accessToken.getExpiresAt());
            user.setOAuth2RegistrationId(registrationId);
            
            // Refresh token varsa onu da kaydet
            if (refreshTokenValue != null) {
                user.setOAuth2RefreshToken(refreshTokenValue);
                logger.info("Refresh token güncellendi");
            }
            
            // Kullanıcıyı güncelle
            User updatedUser = userRepository.save(user);
            
            if (updatedUser != null && updatedUser.getOAuth2AccessToken() != null) {
                logger.info("OAuth2 token bilgileri başarıyla kullanıcı için kaydedildi: {}", email);
            } else {
                logger.error("OAuth2 token bilgileri kaydedilemedi: {}", email);
            }
            
        } catch (Exception e) {
            logger.error("OAuth2 token kaydedilirken hata oluştu: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Yeni kullanıcı için varsayılan kredi oluşturur
     */
    private Credit createNewCredit() {
        Credit credit = new Credit();
        
        // Varsayılan olarak FREE plan ve 5 kredi
        credit.setPlanType(PlanType.FREE);
        credit.setCreditCount(5);
        
        // Bugünden başlayarak 30 gün geçerli
        Date today = new Date();
        credit.setStartDate(today);
        
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        calendar.add(Calendar.DAY_OF_MONTH, 30);
        Date expiryDate = calendar.getTime();
        credit.setExpiredDate(expiryDate);
        
        logger.info("Yeni kredi oluşturuldu: FREE plan, 5 kredi, bitiş tarihi: {}", expiryDate);
        
        return credit;
    }
} 