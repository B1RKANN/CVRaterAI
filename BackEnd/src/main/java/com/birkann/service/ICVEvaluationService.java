package com.birkann.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.birkann.dto.CVEvaluationRequest;
import com.birkann.dto.CVEvaluationResponse;
import com.birkann.dto.CVEvaluationSummaryResponse;

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
    
    /**
     * Verilen CV değerlendirme ID'sine ilgili kullanıcının erişim yetkisi olup olmadığını kontrol eder
     * @param evaluationId Değerlendirme ID
     * @param userId Kullanıcı ID
     * @return Erişim yetkisi varsa true, yoksa false
     */
    boolean canAccessEvaluation(Long evaluationId, Long userId);
    
    /**
     * Verilen kullanıcı ID'sine göre CV değerlendirmelerini listeler
     * @param viewerId Görüntüleyen kullanıcının ID'si
     * @param userId Değerlendirme sahibinin ID'si
     * @return Değerlendirme listesi (yetki kontrolü yapılmış)
     */
    List<CVEvaluationResponse> getUserEvaluationsByUserId(Long viewerId, Long userId);
    
    /**
     * Verilen kullanıcı ID'sine göre CV değerlendirmelerinin özet bilgilerini listeler
     * @param viewerId Görüntüleyen kullanıcının ID'si
     * @param userId Değerlendirme sahibinin ID'si
     * @return Değerlendirme özet listesi (yetki kontrolü yapılmış)
     */
    List<CVEvaluationSummaryResponse> getUserEvaluationSummariesByUserId(Long viewerId, Long userId);
} 