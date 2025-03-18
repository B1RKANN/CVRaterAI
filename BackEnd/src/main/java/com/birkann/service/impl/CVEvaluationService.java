package com.birkann.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
import com.birkann.dto.CVEvaluationSummaryResponse;
import com.birkann.enums.FileType;
import com.birkann.exception.BaseException;
import com.birkann.exception.ErrorMessage;
import com.birkann.exception.MessageType;
import com.birkann.model.CVEvaluation;
import com.birkann.model.Credit;
import com.birkann.model.Role;
import com.birkann.model.User;
import com.birkann.repository.CVEvaluationRepository;
import com.birkann.repository.CreditRepository;
import com.birkann.repository.UserRepository;
import com.birkann.service.ICVEvaluationService;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    
    @Autowired
    private ObjectMapper objectMapper;
    
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
            
            // Eski formatı yeni formata dönüştür
            evaluationResult = convertToNewFormat(evaluationResult);
            
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
            
            // İsim ve soyismi JSON'dan çıkarıp birleştir
            try {
                Map<String, Object> evaluationMap = objectMapper.readValue(evaluationResult, Map.class);
                if (evaluationMap.containsKey("userInformation")) {
                    Map<String, Object> userInfo = (Map<String, Object>) evaluationMap.get("userInformation");
                    String name = (String) userInfo.getOrDefault("name", "");
                    String surname = (String) userInfo.getOrDefault("surname", "");
                    
                    // Boş değilse birleştir
                    if (!name.equals("Belirtilmemiş") || !surname.equals("Belirtilmemiş")) {
                        String fullName = name + " " + surname;
                        evaluation.setFullName(fullName.trim());
                        logger.debug("İsim soyisim birleştirildi: {}", fullName);
                    }
                }
            } catch (Exception e) {
                logger.warn("JSON parse hatası, isim-soyisim birleştirilemedi: {}", e.getMessage());
            }
            
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
        logger.debug("Kullanıcı değerlendirmeleri getiriliyor. Kullanıcı ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(new ErrorMessage(MessageType.USER_NOT_FOUND, userId.toString())));
        
        List<CVEvaluation> evaluations = evaluationRepository.findByUserOrderByEvaluationDateDesc(user);
        
        return evaluations.stream().map(evaluation -> {
            CVEvaluationResponse response = new CVEvaluationResponse();
            BeanUtils.copyProperties(evaluation, response);
            response.setId(evaluation.getId());
            // UserId değerini ayarla
            response.setUserId(userId);
            return response;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CVEvaluationResponse getEvaluation(Long evaluationId) {
        logger.debug("değerlendirme getiriliyor. ID: {}", evaluationId);
        
        Optional<CVEvaluation> evaluationOptional = evaluationRepository.findById(evaluationId);
        if (!evaluationOptional.isPresent()) {
            logger.warn("Değerlendirme bulunamadı. ID: {}", evaluationId);
            return null;
        }
        
        CVEvaluation evaluation = evaluationOptional.get();
        
        // LOB alanlarının yüklenmesini zorla
        if (evaluation.getEvaluationResult() != null) {
            evaluation.getEvaluationResult().length();
        }
        if (evaluation.getJobRequirements() != null) {
            evaluation.getJobRequirements().length();
        }
        
        CVEvaluationResponse response = new CVEvaluationResponse();
        
        // Temel özellikleri kopyala
        BeanUtils.copyProperties(evaluation, response);
        
        // ID değerini ayarla
        response.setId(evaluation.getId());
        
        // UserId değerini ayarla (null kontrol yaparak)
        if (evaluation.getUser() != null) {
            response.setUserId(evaluation.getUser().getId());
            logger.debug("Değerlendirme için kullanıcı ID'si ayarlandı: {}", evaluation.getUser().getId());
        } else {
            logger.warn("Değerlendirme kaydı için kullanıcı bilgisi bulunamadı. ID: {}", evaluationId);
        }
        
        return response;
    }
    
    @Override
    public int convertAllToNewFormat() {
        int count = 0;
        
        // Tüm değerlendirme kayıtlarını getir
        List<CVEvaluation> allEvaluations = evaluationRepository.findAll();
        logger.info("Toplam {} değerlendirme bulundu", allEvaluations.size());
        
        for (CVEvaluation evaluation : allEvaluations) {
            try {
                // Eski formatı kontrol et
                String result = evaluation.getEvaluationResult();
                
                if (result != null && !result.trim().isEmpty()) {
                    // Yeni formata dönüştür
                    String newFormat = convertToNewFormat(result);
                    
                    // Eğer değişiklik olduysa, kaydı güncelle
                    if (!result.equals(newFormat)) {
                        evaluation.setEvaluationResult(newFormat);
                        evaluationRepository.save(evaluation);
                        count++;
                        logger.info("Değerlendirme #{} yeni formata dönüştürüldü", evaluation.getId());
                    }
                }
            } catch (Exception e) {
                logger.error("Değerlendirme #{} dönüştürülürken hata: {}", evaluation.getId(), e.getMessage());
            }
        }
        
        logger.info("Toplam {} değerlendirme yeni formata dönüştürüldü", count);
        return count;
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
        
        // UserId değerini ayarla
        if (evaluation.getUser() != null) {
            response.setUserId(evaluation.getUser().getId());
            logger.debug("Değerlendirme #{} için kullanıcı ID'si ayarlandı: {}", 
                    evaluation.getId(), evaluation.getUser().getId());
        } else {
            logger.warn("Değerlendirme #{} için kullanıcı bilgisi bulunamadı", evaluation.getId());
        }
        
        // Full name bilgisini eksikse JSON'dan çıkar ve ayarla
        if ((evaluation.getFullName() == null || evaluation.getFullName().isEmpty()) && 
            evaluation.getEvaluationResult() != null && !evaluation.getEvaluationResult().isEmpty()) {
            try {
                Map<String, Object> evaluationMap = objectMapper.readValue(evaluation.getEvaluationResult(), Map.class);
                if (evaluationMap.containsKey("userInformation")) {
                    Map<String, Object> userInfo = (Map<String, Object>) evaluationMap.get("userInformation");
                    String name = (String) userInfo.getOrDefault("name", "");
                    String surname = (String) userInfo.getOrDefault("surname", "");
                    
                    // Boş değilse birleştir ve kaydet
                    if (!name.equals("Belirtilmemiş") || !surname.equals("Belirtilmemiş")) {
                        String fullName = name + " " + surname;
                        
                        // Veritabanında güncelle
                        evaluation.setFullName(fullName.trim());
                        evaluationRepository.save(evaluation);
                        
                        // Yanıta da ekle
                        response.setFullName(fullName.trim());
                        logger.debug("mapToResponse: İsim soyisim birleştirildi ve kaydedildi: {}", fullName);
                    }
                }
            } catch (Exception e) {
                logger.warn("mapToResponse: JSON parse hatası, isim-soyisim birleştirilemedi: {}", e.getMessage());
            }
        } else if (evaluation.getFullName() != null) {
            // Full name zaten varsa, yanıta ekle
            response.setFullName(evaluation.getFullName());
        }
        
        return response;
    }

    /**
     * Eski JSON formatını yeni JSON formatına dönüştürür
     */
    public String convertToNewFormat(String oldFormatJson) {
        try {
            // Boş kontrol
            if (oldFormatJson == null || oldFormatJson.trim().isEmpty()) {
                return oldFormatJson;
            }
            
            // JSON olup olmadığını kontrol et
            if (!oldFormatJson.trim().startsWith("{") || !oldFormatJson.trim().endsWith("}")) {
                return oldFormatJson;
            }
            
            // JSON'ı parse et
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> oldFormat = objectMapper.readValue(oldFormatJson, Map.class);
            
            // Yeni format JSON oluştur
            Map<String, Object> newFormat = new HashMap<>();
            
            // userInformation alanı
            Map<String, Object> userInfo = new HashMap<>();
            
            // Eğer yeni format zaten varsa, doğrudan döndür
            if (oldFormat.containsKey("userInformation")) {
                return oldFormatJson;
            }
            
            // Kişisel bilgileri al
            if (oldFormat.containsKey("kisiselBilgiler")) {
                Map<String, Object> kisiselBilgiler = (Map<String, Object>) oldFormat.get("kisiselBilgiler");
                userInfo.put("name", kisiselBilgiler.getOrDefault("name", "Belirtilmemiş"));
                userInfo.put("surname", kisiselBilgiler.getOrDefault("surname", "Belirtilmemiş"));
                userInfo.put("email", kisiselBilgiler.getOrDefault("email", "Belirtilmemiş"));
                userInfo.put("phone", kisiselBilgiler.getOrDefault("phoneNumber", "Belirtilmemiş"));
                
                // Skills alanını işle
                Object skills = kisiselBilgiler.get("skills");
                if (skills instanceof List) {
                    // Liste ise birleştir
                    List<String> skillsList = (List<String>) skills;
                    userInfo.put("skills", String.join(", ", skillsList));
                } else if (skills != null) {
                    userInfo.put("skills", skills.toString());
                } else {
                    userInfo.put("skills", "");
                }
            } else {
                userInfo.put("name", "Belirtilmemiş");
                userInfo.put("surname", "Belirtilmemiş");
                userInfo.put("email", "Belirtilmemiş");
                userInfo.put("phone", "Belirtilmemiş");
                userInfo.put("skills", "");
            }
            newFormat.put("userInformation", userInfo);
            
            // skillRatings alanı - teknikYetenekler'den ayrıştır
            List<Map<String, Object>> skillRatings = new ArrayList<>();
            if (oldFormat.containsKey("teknikYetenekler")) {
                String teknikYetenekler = oldFormat.get("teknikYetenekler").toString();
                
                // Örnek: "Java %50, Kotlin %65, Android %40, HTML %60, CSS %55, JS %50, Figma %30"
                String[] pairs = teknikYetenekler.split(",");
                for (String pair : pairs) {
                    pair = pair.trim();
                    if (pair.contains("%")) {
                        String[] parts = pair.split("%");
                        if (parts.length > 0) {
                            String language = parts[0].trim();
                            int percentage = 0;
                            try {
                                if (parts.length > 1) {
                                    percentage = Integer.parseInt(parts[1].trim());
                                }
                            } catch (NumberFormatException e) {
                                // Yüzde değerini çıkaramazsak 0 kullan
                            }
                            
                            Map<String, Object> skill = new HashMap<>();
                            skill.put("language", language);
                            skill.put("percentage", percentage);
                            skillRatings.add(skill);
                        }
                    }
                }
                newFormat.put("skillRatings", skillRatings);
            } else if (oldFormat.containsKey("githubDiller")) {
                // GitHub dilleri varsa onları da ekle
                Map<String, Object> githubDiller = (Map<String, Object>) oldFormat.get("githubDiller");
                for (Map.Entry<String, Object> entry : githubDiller.entrySet()) {
                    String language = entry.getKey();
                    double percentage = 0;
                    
                    try {
                        Object value = entry.getValue();
                        if (value instanceof Number) {
                            percentage = ((Number) value).doubleValue();
                        } else if (value instanceof String) {
                            percentage = Double.parseDouble(value.toString());
                        }
                    } catch (NumberFormatException e) {
                        // Yüzde değerini çıkaramazsak 0 kullan
                    }
                    
                    Map<String, Object> skill = new HashMap<>();
                    skill.put("language", language);
                    skill.put("percentage", (int) percentage);
                    skillRatings.add(skill);
                }
                
                // Eğer hala boş ise, null olarak bırak
                if (skillRatings.isEmpty()) {
                    newFormat.put("skillRatings", null);
                } else {
                    newFormat.put("skillRatings", skillRatings);
                }
            } else {
                newFormat.put("skillRatings", null);
            }
            
            // compatibilityStatus - puan değerini kullan
            int compatibilityStatus = 0;
            if (oldFormat.containsKey("puan")) {
                Object puanObj = oldFormat.get("puan");
                if (puanObj instanceof Number) {
                    compatibilityStatus = ((Number) puanObj).intValue();
                } else if (puanObj instanceof String) {
                    try {
                        compatibilityStatus = Integer.parseInt(puanObj.toString());
                    } catch (NumberFormatException e) {
                        // Parse hatası, 0 kullan
                    }
                }
            }
            newFormat.put("compatibilityStatus", compatibilityStatus);
            
            // explanation - genelDegerlendirme veya gereksinimUyumlulugu alanlarını kullan
            StringBuilder explanation = new StringBuilder();
            if (oldFormat.containsKey("genelDegerlendirme")) {
                explanation.append(oldFormat.get("genelDegerlendirme"));
            }
            if (oldFormat.containsKey("gereksinimUyumlulugu")) {
                if (explanation.length() > 0) {
                    explanation.append("\n\n");
                }
                explanation.append(oldFormat.get("gereksinimUyumlulugu"));
            }
            if (explanation.length() == 0 && oldFormat.containsKey("gucluYonler")) {
                explanation.append(oldFormat.get("gucluYonler"));
            }
            
            newFormat.put("explanation", explanation.toString());
            
            // Yeni JSON formatını string olarak döndür
            return objectMapper.writeValueAsString(newFormat);
            
        } catch (Exception e) {
            // Hata durumunda orijinal string'i döndür
            return oldFormatJson;
        }
    }

    @Override
    public boolean canAccessEvaluation(Long evaluationId, Long userId) {
        logger.debug("CV değerlendirme erişim kontrolü. Değerlendirme ID: {}, Kullanıcı ID: {}", evaluationId, userId);
        
        // Admin kullanıcılar her zaman erişebilir
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(new ErrorMessage(MessageType.USER_NOT_FOUND, userId.toString())));
        
        if (user.getRole() == Role.ADMIN) {
            logger.debug("Admin kullanıcısı, erişim izni verildi. Kullanıcı ID: {}", userId);
            return true;
        }
        
        // ID'si 1 olan kullanıcı özel durum kontrolü
        if (userId == 1L) {
            // Değerlendirmenin sahibini bul
            Optional<CVEvaluation> evaluation = evaluationRepository.findById(evaluationId);
            if (!evaluation.isPresent()) {
                logger.warn("Değerlendirme bulunamadı. ID: {}", evaluationId);
                return false;
            }
            
            // Kullanıcının kendi değerlendirmesi değilse erişim yasak
            if (!evaluation.get().getUser().getId().equals(userId)) {
                logger.warn("ID'si 1 olan kullanıcı başka kullanıcının değerlendirmesine erişemez. Değerlendirme ID: {}", evaluationId);
                return false;
            }
        }
        
        // Değerlendirmenin sahibi olup olmadığını kontrol et
        Optional<CVEvaluation> evaluation = evaluationRepository.findById(evaluationId);
        if (!evaluation.isPresent()) {
            logger.warn("Değerlendirme bulunamadı. ID: {}", evaluationId);
            return false;
        }
        
        boolean hasAccess = evaluation.get().getUser().getId().equals(userId);
        
        if (!hasAccess) {
            logger.warn("Erişim reddedildi. Kullanıcı ID: {}, Değerlendirme ID: {}, Değerlendirme sahibi: {}", 
                userId, evaluationId, evaluation.get().getUser().getId());
        }
        
        return hasAccess;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CVEvaluationResponse> getUserEvaluationsByUserId(Long viewerId, Long userId) {
        logger.debug("Kullanıcı değerlendirmeleri getiriliyor. Görüntüleyen ID: {}, Kullanıcı ID: {}", viewerId, userId);
        
        // Admin kullanıcıları her zaman tüm değerlendirmeleri görebilir
        User viewer = userRepository.findById(viewerId)
                .orElseThrow(() -> new BaseException(new ErrorMessage(MessageType.USER_NOT_FOUND, viewerId.toString())));
        
        // ID'si 1 olan kullanıcı özel durum kontrolü
        if (viewerId == 1L && !viewerId.equals(userId)) {
            logger.warn("ID'si 1 olan kullanıcı başka kullanıcıların değerlendirmelerini görüntüleyemez.");
            throw new BaseException(new ErrorMessage(MessageType.ACCESS_DENIED, "Bu kaynağa erişim izniniz yok."));
        }
        
        // Normal kullanıcılar sadece kendi değerlendirmelerini görebilir
        if (!viewer.getRole().equals(Role.ADMIN) && !viewerId.equals(userId)) {
            logger.warn("Erişim reddedildi. Görüntüleyen ID: {}, Kullanıcı ID: {}", viewerId, userId);
            throw new BaseException(new ErrorMessage(MessageType.ACCESS_DENIED, "Bu kaynağa erişim izniniz yok."));
        }
        
        try {
            // Değerlendirmeleri getir
            List<CVEvaluation> evaluations = evaluationRepository.findByUserIdOrderByEvaluationDateDesc(userId);
            
            // LOB alanlarının yüklenmesini sağlamak için tüm alanları ilk etapta yükle
            evaluations.forEach(eval -> {
                if (eval.getEvaluationResult() != null) {
                    eval.getEvaluationResult().length(); // LOB alanının yüklenmesini zorla
                }
                if (eval.getJobRequirements() != null) {
                    eval.getJobRequirements().length(); // LOB alanının yüklenmesini zorla
                }
            });
            
            return evaluations.stream().map(evaluation -> {
                CVEvaluationResponse response = new CVEvaluationResponse();
                BeanUtils.copyProperties(evaluation, response);
                response.setId(evaluation.getId());
                response.setUserId(userId);
                return response;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Değerlendirme verileri alınırken hata: {}", e.getMessage(), e);
            throw new BaseException(new ErrorMessage(MessageType.GENERAL_EXCEPTION, "Değerlendirme verileri alınırken hata: " + e.getMessage()));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<CVEvaluationSummaryResponse> getUserEvaluationSummariesByUserId(Long viewerId, Long userId) {
        logger.debug("Kullanıcı değerlendirme özetleri getiriliyor. Görüntüleyen ID: {}, Kullanıcı ID: {}", viewerId, userId);
        
        // Admin kullanıcıları her zaman tüm değerlendirmeleri görebilir
        User viewer = userRepository.findById(viewerId)
                .orElseThrow(() -> new BaseException(new ErrorMessage(MessageType.USER_NOT_FOUND, viewerId.toString())));
        
        // ID'si 1 olan kullanıcı özel durum kontrolü
        if (viewerId == 1L && !viewerId.equals(userId)) {
            logger.warn("ID'si 1 olan kullanıcı başka kullanıcıların değerlendirmelerini görüntüleyemez.");
            throw new BaseException(new ErrorMessage(MessageType.ACCESS_DENIED, "Bu kaynağa erişim izniniz yok."));
        }
        
        // Normal kullanıcılar sadece kendi değerlendirmelerini görebilir
        if (!viewer.getRole().equals(Role.ADMIN) && !viewerId.equals(userId)) {
            logger.warn("Erişim reddedildi. Görüntüleyen ID: {}, Kullanıcı ID: {}", viewerId, userId);
            throw new BaseException(new ErrorMessage(MessageType.ACCESS_DENIED, "Bu kaynağa erişim izniniz yok."));
        }
        
        try {
            // Değerlendirmeleri getir
            List<CVEvaluation> evaluations = evaluationRepository.findByUserIdOrderByEvaluationDateDesc(userId);
            
            return evaluations.stream().map(evaluation -> {
                CVEvaluationSummaryResponse response = new CVEvaluationSummaryResponse();
                response.setId(evaluation.getId());
                response.setUserId(userId);
                response.setFullName(evaluation.getFullName());
                response.setEvaluationScore(evaluation.getEvaluationScore());
                response.setEvaluationDate(evaluation.getEvaluationDate());
                return response;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Değerlendirme özet verileri alınırken hata: {}", e.getMessage(), e);
            throw new BaseException(new ErrorMessage(MessageType.GENERAL_EXCEPTION, "Değerlendirme özet verileri alınırken hata: " + e.getMessage()));
        }
    }
} 