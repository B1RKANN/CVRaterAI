package com.birkann.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.birkann.dto.CVEvaluationResponse;
import com.birkann.enums.FileType;
import com.birkann.exception.BaseException;
import com.birkann.exception.ErrorMessage;
import com.birkann.exception.MessageType;
import com.birkann.model.CVEvaluation;
import com.birkann.model.Credit;
import com.birkann.model.User;
import com.birkann.repository.CVEvaluationRepository;
import com.birkann.repository.CreditRepository;
import com.birkann.repository.UserRepository;
import com.birkann.service.ICVEvaluationService;

@Service
public class CVEvaluationService implements ICVEvaluationService {

    private static final Logger logger = LoggerFactory.getLogger(CVEvaluationService.class);
    
    @Value("${file.upload.dir}")
    private String uploadDir;
    
    @Autowired
    private GeminiAIService geminiAIService;
    
    @Autowired
    private CVEvaluationRepository evaluationRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CreditRepository creditRepository;
    
    @Override
    @Transactional
    public CVEvaluationResponse evaluateCV(Long userId, MultipartFile file, String githubUrl, String jobRequirements) {
        // Kullanıcıyı kontrol et
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(new ErrorMessage(MessageType.USER_NOT_FOUND, userId.toString())));
        
        // Kredi kontrolü yap
        Credit credit = user.getCredit();
        if (credit == null || credit.getUserCredit() <= 0) {
            throw new BaseException(new ErrorMessage(MessageType.INSUFFICIENT_CREDIT, "Değerlendirme yapmak için yeterli krediniz bulunmamaktadır."));
        }
        
        try {
            // Dosya türünü kontrol et
            String fileName = file.getOriginalFilename();
            String fileExtension = getFileExtension(fileName).toUpperCase();
            FileType fileType;
            
            try {
                fileType = FileType.valueOf(fileExtension);
            } catch (IllegalArgumentException e) {
                throw new BaseException(new ErrorMessage(MessageType.INVALID_FILE_TYPE, "Desteklenmeyen dosya türü: " + fileExtension));
            }
            
            // Dosyayı kaydet
            String storedFilePath = storeFile(file);
            
            // AI ile CV'yi değerlendir
            Map<String, Object> analysisResult = geminiAIService.analyzeCV(file, githubUrl, jobRequirements);
            
            // Değerlendirme başarılı mı kontrol et
            if (!(boolean) analysisResult.getOrDefault("success", false)) {
                throw new BaseException(new ErrorMessage(MessageType.EVALUATION_FAILED, 
                        (String) analysisResult.getOrDefault("error", "Bilinmeyen hata")));
            }
            
            // Değerlendirme sonuçlarını hazırla
            String evaluationResult = (String) analysisResult.get("evaluationResult");
            Integer score = parseScore(analysisResult.get("score"));
            
            // CV değerlendirme kaydını oluştur
            CVEvaluation evaluation = new CVEvaluation();
            evaluation.setFileName(fileName);
            evaluation.setFilePath(storedFilePath);
            evaluation.setFileType(fileType);
            evaluation.setGithubUrl(githubUrl);
            evaluation.setJobRequirements(jobRequirements);
            evaluation.setEvaluationScore(score);
            evaluation.setEvaluationResult(evaluationResult);
            evaluation.setEvaluationDate(new Date());
            evaluation.setUser(user);
            
            // Değerlendirme kaydını kaydet
            CVEvaluation savedEvaluation = evaluationRepository.save(evaluation);
            
            // Kullanıcı kredisini güncelle
            credit.setUserCredit(credit.getUserCredit() - 1);
            creditRepository.save(credit);
            
            // Sonucu dön
            return mapToResponse(savedEvaluation);
            
        } catch (IOException e) {
            logger.error("Dosya işlenirken hata oluştu", e);
            throw new BaseException(new ErrorMessage(MessageType.FILE_PROCESSING_ERROR, e.getMessage()));
        }
    }

    @Override
    public List<CVEvaluationResponse> getUserEvaluations(Long userId) {
        // Kullanıcıyı kontrol et
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(new ErrorMessage(MessageType.USER_NOT_FOUND, userId.toString())));
        
        // Kullanıcının değerlendirmelerini getir
        List<CVEvaluation> evaluations = evaluationRepository.findByUserOrderByEvaluationDateDesc(user);
        
        // DTO'ya çevir ve dön
        return evaluations.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CVEvaluationResponse getEvaluation(Long evaluationId) {
        // Değerlendirmeyi getir
        CVEvaluation evaluation = evaluationRepository.findById(evaluationId)
                .orElseThrow(() -> new BaseException(new ErrorMessage(MessageType.EVALUATION_NOT_FOUND, evaluationId.toString())));
        
        // DTO'ya çevir ve dön
        return mapToResponse(evaluation);
    }
    
    /**
     * Dosyayı sunucuya kaydeder
     */
    private String storeFile(MultipartFile file) throws IOException {
        // Upload dizinini oluştur
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(uploadPath);
        
        // Dosya adını benzersiz yap
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path targetLocation = uploadPath.resolve(fileName);
        
        // Dosyayı kopyala
        Files.copy(file.getInputStream(), targetLocation);
        
        return targetLocation.toString();
    }
    
    /**
     * Dosya uzantısını alır
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }
    
    /**
     * Puanı integer'a çevirir
     */
    private Integer parseScore(Object scoreObj) {
        if (scoreObj == null) {
            return 0;
        }
        
        if (scoreObj instanceof Integer) {
            return (Integer) scoreObj;
        } else if (scoreObj instanceof Double) {
            return ((Double) scoreObj).intValue();
        } else if (scoreObj instanceof String) {
            try {
                return Integer.parseInt((String) scoreObj);
            } catch (NumberFormatException e) {
                return 0;
            }
        } else {
            return 0;
        }
    }
    
    /**
     * Model'i DTO'ya çevirir
     */
    private CVEvaluationResponse mapToResponse(CVEvaluation evaluation) {
        CVEvaluationResponse response = new CVEvaluationResponse();
        BeanUtils.copyProperties(evaluation, response);
        return response;
    }
} 