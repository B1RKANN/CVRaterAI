package com.birkann.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.birkann.dto.CVEvaluationResponse;
import com.birkann.model.CVEvaluation;
import com.birkann.model.User;
import com.birkann.repository.CVEvaluationRepository;
import com.birkann.service.ICVEvaluationService;
import com.birkann.service.IUserService;
import com.birkann.service.impl.CVEvaluationService;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/cv-evaluation")
@Tag(name = "CV Değerlendirme", description = "CV değerlendirme işlemleri API'si")
public class CVEvaluationController {

    private static final Logger logger = LoggerFactory.getLogger(CVEvaluationController.class);
    
    @Autowired
    private ICVEvaluationService cvEvaluationService;
    
    @Autowired
    private IUserService userService;
    
    @Autowired
    private CVEvaluationRepository evaluationRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * CV dosyasını ve opsiyonel olarak GitHub URL'sini alıp değerlendirme yapar
     * 
     * @param userId Kullanıcı ID
     * @param file CV dosyası (PDF, WORD, JPG vs.)
     * @param githubUrl Opsiyonel GitHub URL
     * @param jobRequirements İş gereksinimleri
     * @return Değerlendirme sonucu
     */
    @PostMapping(value = "/evaluate/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CVEvaluationResponse> evaluateCV(
            @PathVariable Long userId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "githubUrl", required = false) String githubUrl,
            @RequestParam(value = "jobRequirements", required = false) String jobRequirements) {
        
        // Mevcut kimlik doğrulama bilgilerini loglayalım
        logCurrentAuthentication("evaluateCV");
        
        // Kullanıcı erişim kontrolü
        if (!userService.canAccessUser(userId)) {
            logger.warn("Yetkisiz erişim denemesi! Kullanıcı sadece kendi hesabına erişebilir. Erişilmek istenen kullanıcı ID: {}", userId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        logger.info("CV değerlendirme isteği alındı. Kullanıcı: {}, Dosya: {}, GitHub URL: {}, İş Gereksinimleri: {}", 
                userId, file.getOriginalFilename(), githubUrl, 
                jobRequirements != null ? (jobRequirements.length() > 50 ? jobRequirements.substring(0, 50) + "..." : jobRequirements) : "Belirtilmemiş");
        
        CVEvaluationResponse response = cvEvaluationService.evaluateCV(userId, file, githubUrl, jobRequirements);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Kullanıcı ID'sine göre CV değerlendirmelerini listeler (sadece id, userId ve fullName döndürür)
     * @param userId Kullanıcı ID
     * @return CV değerlendirme listesi (minimal)
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "Kullanıcı ID'sine göre CV değerlendirmelerini listeler", 
               description = "Belirtilen kullanıcının tüm CV değerlendirmelerini minimal bilgilerle (id, userId, fullName) getirir.")
    @ApiResponse(responseCode = "200", description = "İşlem başarılı")
    @ApiResponse(responseCode = "403", description = "Erişim reddedildi")
    @ApiResponse(responseCode = "404", description = "Kullanıcı bulunamadı")
    public ResponseEntity<List<Map<String, Object>>> getUserEvaluations(
            @Parameter(description = "Kullanıcı ID", required = true) @PathVariable Long userId) {
        
        logger.debug("Kullanıcı değerlendirmeleri isteniyor. Kullanıcı ID: {}", userId);
        
        // Giriş yapmış kullanıcıyı al
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            logger.error("Kimlik doğrulama hatası: Giriş yapmış kullanıcı bulunamadı");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        logger.debug("İstek yapan kullanıcı: id={}, email={}, role={}", 
            currentUser.getId(), currentUser.getEmail(), currentUser.getRole());
        
        try {
            // Kullanıcı ID'sine göre değerlendirmeleri getir (yetki kontrolü servis katmanında yapılır)
            List<CVEvaluationResponse> evaluations = cvEvaluationService.getUserEvaluationsByUserId(currentUser.getId(), userId);
            
            // Sadece id, userId ve fullName içeren minimal yanıt oluştur
            List<Map<String, Object>> minimalResponses = evaluations.stream()
                .map(eval -> {
                    Map<String, Object> minimalResponse = new HashMap<>();
                    minimalResponse.put("id", eval.getId());
                    minimalResponse.put("userId", eval.getUserId());
                    minimalResponse.put("fullName", eval.getFullName());
                    minimalResponse.put("date", eval.getEvaluationDate());
                    return minimalResponse;
                })
                .collect(Collectors.toList());
                
            return ResponseEntity.ok(minimalResponses);
        } catch (Exception e) {
            logger.error("Değerlendirme listesi getirme hatası: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Belirli bir CV değerlendirmesini getirir
     * @param id CV değerlendirme ID
     * @return CV değerlendirme detayları
     */
    @GetMapping("/evaluate/{id}")
    @Operation(summary = "CV değerlendirmesini getirir", 
               description = "Belirtilen ID'ye sahip CV değerlendirmesini getirir. Admin kullanıcıları tüm değerlendirmeleri, normal kullanıcılar ise sadece kendi değerlendirmelerini görebilir.")
    @ApiResponse(responseCode = "200", description = "İşlem başarılı",
                content = @Content(schema = @Schema(implementation = CVEvaluationResponse.class)))
    @ApiResponse(responseCode = "403", description = "Erişim reddedildi")
    @ApiResponse(responseCode = "404", description = "Değerlendirme bulunamadı")
    public ResponseEntity<CVEvaluationResponse> getEvaluation(
            @Parameter(description = "CV değerlendirme ID", required = true) @PathVariable Long id) {
        
        logger.debug("CV değerlendirmesi isteniyor. ID: {}", id);
        
        // Giriş yapmış kullanıcıyı al
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            logger.error("Kimlik doğrulama hatası: Giriş yapmış kullanıcı bulunamadı");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        logger.debug("İstek yapan kullanıcı: id={}, email={}, role={}", 
            currentUser.getId(), currentUser.getEmail(), currentUser.getRole());
        
        // Erişim kontrolü
        if (!cvEvaluationService.canAccessEvaluation(id, currentUser.getId())) {
            logger.warn("Erişim reddedildi. Kullanıcı ID: {}, Değerlendirme ID: {}", currentUser.getId(), id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        try {
            CVEvaluationResponse evaluation = cvEvaluationService.getEvaluation(id);
            if (evaluation == null) {
                logger.warn("Değerlendirme bulunamadı. ID: {}", id);
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(evaluation);
        } catch (Exception e) {
            logger.error("Değerlendirme getirme hatası: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Mevcut kimlik doğrulama bilgilerini loglayan yardımcı metot
     */
    private void logCurrentAuthentication(String methodName) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            logger.debug("{} - Kimlik doğrulama bilgileri: principal={}, name={}, authorities={}, isAuthenticated={}", 
                    methodName, auth.getPrincipal(), auth.getName(), auth.getAuthorities(), auth.isAuthenticated());
        } else {
            logger.warn("{} - Kimlik doğrulama bilgisi bulunamadı!", methodName);
        }
    }
} 