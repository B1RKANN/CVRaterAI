package com.birkann.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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



@RestController
@RequestMapping("/api/v1/cv-evaluation")
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
     * Kullanıcının tüm değerlendirmelerini getirir
     * 
     * @param userId Kullanıcı ID
     * @return Değerlendirme listesi
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CVEvaluationResponse>> getUserEvaluations(@PathVariable Long userId) {
        // Mevcut kimlik doğrulama bilgilerini loglayalım
        logCurrentAuthentication("getUserEvaluations");
        
        // Kullanıcı erişim kontrolü
        if (!userService.canAccessUser(userId)) {
            logger.warn("Yetkisiz erişim denemesi! Kullanıcı sadece kendi değerlendirmelerine erişebilir. Erişilmek istenen kullanıcı ID: {}", userId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        List<CVEvaluationResponse> evaluations = cvEvaluationService.getUserEvaluations(userId);
        
        // JSON içeriğini dönüştür (eski formattan yeni formata)
        if (evaluations != null && !evaluations.isEmpty()) {
            for (CVEvaluationResponse evaluation : evaluations) {
                try {
                    String result = evaluation.getEvaluationResult();
                    if (result != null && result.contains("kisiselBilgiler") && !result.contains("userInformation")) {
                        logger.info("Eski format JSON tespit edildi, yeni formata dönüştürülüyor: {}", evaluation.getId());
                        String convertedResult = ((CVEvaluationService)cvEvaluationService).convertToNewFormat(result);
                        evaluation.setEvaluationResult(convertedResult);
                    }
                } catch (Exception e) {
                    logger.error("JSON dönüştürme sırasında hata: {}", e.getMessage());
                }
            }
        }
        
        return ResponseEntity.ok(evaluations);
    }
    
    /**
     * Belirli bir değerlendirme kaydını getirir
     * 
     * @param evaluationId Değerlendirme ID
     * @return Değerlendirme detayları
     */
    @GetMapping("/{evaluationId}")
    public ResponseEntity<CVEvaluationResponse> getEvaluation(@PathVariable Long evaluationId) {
        // Mevcut kimlik doğrulama bilgilerini loglayalım
        logCurrentAuthentication("getEvaluation");
        
        logger.info("Değerlendirme detayı istendi: {}", evaluationId);
        
        CVEvaluationResponse evaluation = cvEvaluationService.getEvaluation(evaluationId);
        
        if (evaluation == null) {
            logger.warn("Değerlendirme bulunamadı: {}", evaluationId);
            return ResponseEntity.notFound().build();
        }
        
        // Kullanıcının ID'sini logla
        logger.debug("Değerlendirme için kullanıcı ID: {}", evaluation.getUserId());
        
        // Erişim kontrolü öncesi kullanıcı servisi kontrol edilsin
        User currentUser = userService.getCurrentUser();
        if (currentUser != null) {
            logger.debug("Giriş yapmış kullanıcı: id={}, email={}, role={}", 
                    currentUser.getId(), currentUser.getEmail(), currentUser.getRole());
        } else {
            logger.warn("Şu anda giriş yapmış kullanıcı bulunamadı!");
        }
        
        // Kullanıcı erişim kontrolü - getEvaluation için problemli olan kısım
        if (!userService.canAccessUser(evaluation.getUserId())) {
            logger.warn("Yetkisiz erişim denemesi! evaluationId: {}, userId: {}", 
                    evaluationId, evaluation.getUserId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        logger.info("Erişim izni var, değerlendirme sonucu döndürülüyor. evaluationId: {}", evaluationId);
        
        // JSON içeriğini dönüştür (eski formattan yeni formata)
        if (evaluation != null && evaluation.getEvaluationResult() != null) {
            try {
                String result = evaluation.getEvaluationResult();
                if (result.contains("kisiselBilgiler") && !result.contains("userInformation")) {
                    logger.info("Eski format JSON tespit edildi, yeni formata dönüştürülüyor: {}", evaluationId);
                    String convertedResult = ((CVEvaluationService)cvEvaluationService).convertToNewFormat(result);
                    evaluation.setEvaluationResult(convertedResult);
                }
            } catch (Exception e) {
                logger.error("JSON dönüştürme sırasında hata: {}", e.getMessage());
            }
        }
        
        return ResponseEntity.ok(evaluation);
    }
    
    /**
     * Tüm CV değerlendirmelerini yeni formata dönüştürür.
     * Bu endpoint sadece geçiş döneminde kullanılmalıdır.
     * 
     * @return Dönüştürülen kayıt sayısı
     */
    @PostMapping("/convert-to-new-format")
    public ResponseEntity<Map<String, Object>> convertAllToNewFormat() {
        // Mevcut kimlik doğrulama bilgilerini loglayalım
        logCurrentAuthentication("convertAllToNewFormat");
        
        // Bu metot zaten Security Config'de tanımlanan ADMIN yetkisi kontrolünden geçmiş olmalı
        int convertedCount = cvEvaluationService.convertAllToNewFormat();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", convertedCount + " değerlendirme yeni formata dönüştürüldü");
        response.put("convertedCount", convertedCount);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Mevcut değerlendirme kayıtlarının fullName alanlarını günceller
     * @return Güncellenen kayıt sayısı
     */
    @PostMapping("/update-fullnames")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateAllFullNames() {
        // Mevcut kimlik doğrulama bilgilerini loglayalım
        logCurrentAuthentication("updateAllFullNames");
        
        int updatedCount = 0;
        List<CVEvaluation> allEvaluations = evaluationRepository.findAll();
        logger.info("Toplam {} değerlendirme bulundu", allEvaluations.size());
        
        for (CVEvaluation evaluation : allEvaluations) {
            try {
                String evaluationResult = evaluation.getEvaluationResult();
                if (evaluationResult != null && !evaluationResult.isEmpty()) {
                    Map<String, Object> evaluationMap = objectMapper.readValue(evaluationResult, Map.class);
                    if (evaluationMap.containsKey("userInformation")) {
                        Map<String, Object> userInfo = (Map<String, Object>) evaluationMap.get("userInformation");
                        String name = (String) userInfo.getOrDefault("name", "");
                        String surname = (String) userInfo.getOrDefault("surname", "");
                        
                        if (!name.equals("Belirtilmemiş") && !surname.equals("Belirtilmemiş")) {
                            String fullName = name + " " + surname;
                            evaluation.setFullName(fullName.trim());
                            evaluationRepository.save(evaluation);
                            logger.info("Değerlendirme #{} için fullName güncellendi: {}", evaluation.getId(), fullName);
                            updatedCount++;
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("ID {} güncelleme hatası: {}", evaluation.getId(), e.getMessage());
            }
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", updatedCount + " değerlendirme kaydı güncellendi");
        response.put("updatedCount", updatedCount);
        
        return ResponseEntity.ok(response);
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