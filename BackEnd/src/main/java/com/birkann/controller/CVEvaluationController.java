package com.birkann.controller;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.birkann.dto.CVEvaluationResponse;
import com.birkann.service.ICVEvaluationService;
import com.birkann.service.impl.CVEvaluationService;

@RestController
@RequestMapping("/api/v1/cv-evaluation")
public class CVEvaluationController {

    private static final Logger logger = LoggerFactory.getLogger(CVEvaluationController.class);
    
    @Autowired
    private ICVEvaluationService cvEvaluationService;
    
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
        logger.info("Kullanıcı değerlendirmeleri istendi: {}", userId);
        
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
        logger.info("Değerlendirme detayı istendi: {}", evaluationId);
        
        CVEvaluationResponse evaluation = cvEvaluationService.getEvaluation(evaluationId);
        
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
        logger.info("Tüm değerlendirmeleri yeni formata dönüştürme isteği alındı");
        
        int convertedCount = cvEvaluationService.convertAllToNewFormat();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Değerlendirmeler yeni formata başarıyla dönüştürüldü");
        response.put("convertedCount", convertedCount);
        
        return ResponseEntity.ok(response);
    }
} 