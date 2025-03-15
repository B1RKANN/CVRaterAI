package com.birkann.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.birkann.dto.CVEvaluationRequest;
import com.birkann.dto.CVEvaluationResponse;

public interface ICVEvaluationService {
    
    /**
     * CV'yi değerlendirir ve sonucu döner
     * @param userId Kullanıcı ID
     * @param file Yüklenen CV dosyası
     * @param githubUrl GitHub URL (opsiyonel)
     * @param jobRequirements İş gereksinimleri (opsiyonel)
     * @return Değerlendirme sonucu
     */
    CVEvaluationResponse evaluateCV(Long userId, MultipartFile file, String githubUrl, String jobRequirements);
    
    /**
     * CV'yi değerlendirir ve sonucu döner (geriye dönük uyumluluk için)
     * @param userId Kullanıcı ID
     * @param file Yüklenen CV dosyası
     * @param githubUrl GitHub URL (opsiyonel)
     * @return Değerlendirme sonucu
     */
    default CVEvaluationResponse evaluateCV(Long userId, MultipartFile file, String githubUrl) {
        return evaluateCV(userId, file, githubUrl, null);
    }
    
    /**
     * Belirli bir kullanıcının tüm değerlendirmelerini getirir
     * @param userId Kullanıcı ID
     * @return Değerlendirme listesi
     */
    List<CVEvaluationResponse> getUserEvaluations(Long userId);
    
    /**
     * Belirli bir değerlendirme kaydını getirir
     * @param evaluationId Değerlendirme ID
     * @return Değerlendirme detayları
     */
    CVEvaluationResponse getEvaluation(Long evaluationId);
    
    /**
     * Tüm değerlendirme kayıtlarını yeni formata dönüştürür
     * @return Dönüştürülen kayıt sayısı
     */
    int convertAllToNewFormat();
} 