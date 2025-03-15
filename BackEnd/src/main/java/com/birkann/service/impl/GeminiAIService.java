package com.birkann.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.core.io.FileSystemResource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

// Apache PDFBox için import
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

// Apache POI için importlar (Word dosyaları için)
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import org.springframework.http.HttpMethod;
import org.springframework.util.StringUtils;

@Service
public class GeminiAIService {

    private static final Logger logger = LoggerFactory.getLogger(GeminiAIService.class);
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    // Maksimum kabul edilebilir dosya boyutu (1MB)
    private static final long MAX_FILE_SIZE = 1024 * 1024;
    
    @Value("${gemini.api.key}")
    private String apiKey;
    
    @Value("${gemini.api.url}")
    private String apiUrl;
    
    @Value("${github.api.token:}")
    private String githubToken;
    
    // OCR.space API anahtarı
    private final String ocrSpaceApiKey = "K89635280088957";
    private final String ocrSpaceApiUrl = "https://api.ocr.space/parse/image";
    
    // Özel RestTemplate ve zaman aşımı süreleri
    private RestTemplate ocrRestTemplate;
    
    public GeminiAIService() {
        this.restTemplate = new RestTemplate();
        
        // OCR API istekleri için özel RestTemplate oluştur (timeout ayarları ile)
        this.ocrRestTemplate = new RestTemplate();
        org.springframework.http.client.SimpleClientHttpRequestFactory requestFactory = 
            new org.springframework.http.client.SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(60000); // 60 saniye bağlantı zaman aşımı
        requestFactory.setReadTimeout(60000);    // 60 saniye okuma zaman aşımı
        this.ocrRestTemplate.setRequestFactory(requestFactory);
        
        this.objectMapper = new ObjectMapper();
        logger.info("GeminiAIService başlatıldı - OCR.space API ile bulut tabanlı OCR kullanılacak (Yeni API Anahtarı)");
    }
    
    /**
     * CV dosyasını analiz eder
     * 
     * @param file CV dosyası
     * @param githubUrl GitHub URL (opsiyonel)
     * @param jobRequirements İş gereksinimleri
     * @return Analiz sonucu
     */
    public Map<String, Object> analyzeCV(MultipartFile file, String githubUrl, String jobRequirements) {
        try {
            logger.info("CV analizi başlatıldı, dosya adı: {}, boyutu: {} bytes, tipi: {}, GitHub URL: {}, İş Gereksinimleri: {}", 
                    file.getOriginalFilename(), file.getSize(), file.getContentType(), githubUrl, 
                    jobRequirements != null ? jobRequirements.substring(0, Math.min(50, jobRequirements.length())) + "..." : "Belirtilmemiş");
            
            // Dosyayı geçici bir yere kaydet
            Path tempFile = Files.createTempFile("cv_tmp_", "_" + file.getOriginalFilename());
            file.transferTo(tempFile.toFile());
            logger.info("Geçici dosya oluşturuldu: {}", tempFile);
            
            // Dosya içeriğini ve tipini belirle
            String fileName = file.getOriginalFilename();
            String fileType = "";
            String contentType = file.getContentType();
            
            if (fileName != null) {
                fileType = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
            }
            logger.info("Dosya türü: {}, Content Type: {}", fileType, contentType);
            
            // Dosya içeriğini çıkar
            String fileContent = extractFileContent(tempFile.toFile(), fileType, contentType);
            
            // GitHub verilerini çek (eğer GitHub URL'si verilmişse)
            Map<String, Object> githubData = new HashMap<>();
            if (githubUrl != null && !githubUrl.isEmpty()) {
                githubData = fetchGitHubData(githubUrl);
            }
            
            // API için prompt oluştur
            String prompt = createPrompt(fileContent, fileType, contentType, githubUrl, githubData, jobRequirements);
            logger.info("Prompt oluşturuldu, uzunluk: {} karakter", prompt.length());
            
            // API isteği gönder
            Map<String, Object> response = callGeminiAPI(prompt);
            
            // Geçici dosyayı temizle
            Files.deleteIfExists(tempFile);
            logger.info("Geçici dosya silindi: {}", tempFile);
            
            return processResponse(response);
        } catch (IOException e) {
            logger.error("Dosya işleme hatası: {}", e.getMessage(), e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("error", "Dosya işleme hatası: " + e.getMessage());
            return errorResult;
        } catch (Exception e) {
            logger.error("CV analiz hatası: {}", e.getMessage(), e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("error", "CV analiz hatası: " + e.getMessage());
            return errorResult;
        }
    }
    
    // Eski metodu da koruyalım, böylece uyumluluk sorunları yaşanmaz
    public Map<String, Object> analyzeCV(MultipartFile file, String githubUrl) {
        // İş gereksinimleri olmadan analiz için boş gereksinim gönder
        return analyzeCV(file, githubUrl, null);
    }
    
    /**
     * GitHub URL'sinden kullanıcı verilerini çeker
     * @param githubUrl GitHub profil URL'si
     * @return GitHub verileri (diller, repolar, vb.)
     */
    private Map<String, Object> fetchGitHubData(String githubUrl) {
        Map<String, Object> githubData = new HashMap<>();
        
        try {
            // URL'den GitHub kullanıcı adını çıkar
            String username = extractGitHubUsername(githubUrl);
            if (username == null || username.isEmpty()) {
                logger.warn("Geçerli GitHub kullanıcı adı bulunamadı: {}", githubUrl);
                githubData.put("error", "Geçerli GitHub kullanıcı adı bulunamadı");
                return githubData;
            }
            
            // Kullanıcı bilgilerini çek
            Map<String, Object> userInfo = fetchGitHubUserInfo(username);
            if (userInfo != null) {
                githubData.put("userInfo", userInfo);
            }
            
            // Kullanıcının repolarını çek
            List<Map<String, Object>> repos = fetchGitHubRepos(username);
            if (repos != null && !repos.isEmpty()) {
                githubData.put("repos", repos);
                
                // Dil istatistiklerini çek ve hesapla
                Map<String, Double> languageStats = calculateLanguageStats(repos, username);
                githubData.put("languages", languageStats);
            }
            
            githubData.put("success", true);
            
        } catch (Exception e) {
            logger.error("GitHub veri çekme hatası: {}", e.getMessage(), e);
            githubData.put("success", false);
            githubData.put("error", "GitHub API hatası: " + e.getMessage());
        }
        
        return githubData;
    }
    
    /**
     * GitHub URL'sinden kullanıcı adını çıkarır
     */
    private String extractGitHubUsername(String githubUrl) {
        if (githubUrl == null || githubUrl.isEmpty()) {
            return null;
        }
        
        // https://github.com/username
        // https://github.com/username/repo
        try {
            String[] parts = githubUrl.trim().split("/");
            for (int i = 0; i < parts.length; i++) {
                if (parts[i].equalsIgnoreCase("github.com") && i + 1 < parts.length) {
                    return parts[i + 1];
                }
            }
        } catch (Exception e) {
            logger.warn("GitHub kullanıcı adı çıkarılamadı: {}", githubUrl);
        }
        
        return null;
    }
    
    /**
     * GitHub API'den kullanıcı bilgilerini çeker
     */
    private Map<String, Object> fetchGitHubUserInfo(String username) {
        try {
            String apiUrl = "https://api.github.com/users/" + username;
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/vnd.github.v3+json");
            
            // GitHub token varsa ekle
            if (githubToken != null && !githubToken.isEmpty() && !githubToken.equals("your_github_personal_access_token")) {
                headers.set("Authorization", "token " + githubToken);
                logger.info("GitHub API isteği için token kullanılıyor");
            }
            
            ResponseEntity<Map> response = restTemplate.exchange(
                apiUrl, 
                HttpMethod.GET, 
                new HttpEntity<>(headers), 
                Map.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> userInfo = response.getBody();
                return userInfo;
            }
        } catch (Exception e) {
            logger.warn("GitHub kullanıcı bilgisi alınamadı: {}", username, e);
        }
        
        return null;
    }
    
    /**
     * GitHub API'den kullanıcının repolarını çeker
     */
    private List<Map<String, Object>> fetchGitHubRepos(String username) {
        try {
            String apiUrl = "https://api.github.com/users/" + username + "/repos";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/vnd.github.v3+json");
            
            // GitHub token varsa ekle
            if (githubToken != null && !githubToken.isEmpty() && !githubToken.equals("your_github_personal_access_token")) {
                headers.set("Authorization", "token " + githubToken);
            }
            
            ResponseEntity<List> response = restTemplate.exchange(
                apiUrl, 
                HttpMethod.GET, 
                new HttpEntity<>(headers), 
                List.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> repos = response.getBody();
                return repos;
            }
        } catch (Exception e) {
            logger.warn("GitHub repo bilgisi alınamadı: {}", username, e);
        }
        
        return new ArrayList<>();
    }
    
    /**
     * Repolara göre dil istatistiklerini hesaplar
     */
    private Map<String, Double> calculateLanguageStats(List<Map<String, Object>> repos, String username) {
        Map<String, Double> languageStats = new HashMap<>();
        
        try {
            // Her repo için dil bilgilerini çek
            for (Map<String, Object> repo : repos) {
                String repoName = (String) repo.get("name");
                if (repoName == null) continue;
                
                try {
                    String langUrl = "https://api.github.com/repos/" + username + "/" + repoName + "/languages";
                    HttpHeaders headers = new HttpHeaders();
                    headers.set("Accept", "application/vnd.github.v3+json");
                    
                    // GitHub token varsa ekle
                    if (githubToken != null && !githubToken.isEmpty() && !githubToken.equals("your_github_personal_access_token")) {
                        headers.set("Authorization", "token " + githubToken);
                    }
                    
                    ResponseEntity<Map> response = restTemplate.exchange(
                        langUrl, 
                        HttpMethod.GET, 
                        new HttpEntity<>(headers), 
                        Map.class
                    );
                    
                    if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                        @SuppressWarnings("unchecked")
                        Map<String, Integer> languages = response.getBody();
                        
                        for (Map.Entry<String, Integer> entry : languages.entrySet()) {
                            String language = entry.getKey();
                            Integer bytes = entry.getValue();
                            
                            languageStats.put(language, languageStats.getOrDefault(language, 0.0) + bytes);
                        }
                    }
                } catch (Exception e) {
                    logger.warn("Repo {} için dil bilgisi alınamadı: {}", repoName, e.getMessage());
                }
            }
            
            // Toplam byte hesapla
            double total = languageStats.values().stream().mapToDouble(Double::doubleValue).sum();
            
            // Yüzdelikleri hesapla
            if (total > 0) {
                Map<String, Double> percentages = new HashMap<>();
                for (Map.Entry<String, Double> entry : languageStats.entrySet()) {
                    double percentage = (entry.getValue() / total) * 100;
                    percentages.put(entry.getKey(), Math.round(percentage * 10) / 10.0); // 1 ondalık basamak
                }
                return percentages;
            }
            
        } catch (Exception e) {
            logger.error("Dil istatistikleri hesaplanamadı: {}", e.getMessage(), e);
        }
        
        return languageStats;
    }
    
    /**
     * Dosya içeriğini çıkarır
     */
    private String extractFileContent(File file, String fileType, String contentType) throws IOException {
        try {
            byte[] fileData = Files.readAllBytes(file.toPath());
            
            // Dosya bilgilerini detaylı logla
            logger.info("Dosya inceleniyor: tip={}, contentType={}, boyut={} bytes", 
                fileType, contentType, fileData.length);
            
            // Dosya çok büyükse kısa bilgi ver
            if (fileData.length > MAX_FILE_SIZE) {
                return "CV dosyası çok büyük olduğundan tam olarak işlenemedi. " +
                       "Lütfen kişisel bilgileri (isim, soyisim, e-posta, telefon) ve teknik becerileri genel hatlarıyla değerlendir.";
            }
            
            // PDF belgesi
            if (isPdfFile(fileType, contentType)) {
                logger.info("PDF dosyası tespit edildi, metin çıkarılıyor: {}", fileType);
                
                try (PDDocument document = PDDocument.load(file)) {
                    PDFTextStripper stripper = new PDFTextStripper();
                    // PDF'yi daha doğru okumak için ayarlar
                    stripper.setSortByPosition(true); // Metnin pozisyona göre sıralanması
                    stripper.setShouldSeparateByBeads(true); // Doğru bölünme
                    stripper.setAddMoreFormatting(true); // Daha fazla formatlama ekle
                    stripper.setSpacingTolerance(0.5f); // Boşluk toleransını azalt
                    
                    String pdfText = stripper.getText(document);
                    
                    if (pdfText != null && !pdfText.trim().isEmpty()) {
                        logger.info("PDF metni başarıyla çıkarıldı, {} karakter", pdfText.length());
                        
                        // Kişisel bilgileri çıkarmak için regex araması yap
                        pdfText = enhanceContactInfoExtraction(pdfText);
                        
                        return pdfText;
                    } else {
                        logger.warn("PDF'den metin çıkarılamadı");
                        return "PDF belgesinden metin çıkarılamadı. Lütfen içeriği değerlendirmeye çalış.";
                    }
                } catch (Exception e) {
                    logger.error("PDF işleme hatası: {}", e.getMessage());
                    return "PDF işleme hatası: " + e.getMessage() + 
                           ". Lütfen CV'yi genel hatlarıyla değerlendir.";
                }
            }
            
            // Word belgesi
            else if (isWordFile(fileType, contentType)) {
                logger.info("Word dosyası tespit edildi, metin çıkarılıyor: {}", fileType);
                
                try (XWPFDocument document = new XWPFDocument(Files.newInputStream(file.toPath()))) {
                    XWPFWordExtractor extractor = new XWPFWordExtractor(document);
                    String docText = extractor.getText();
                    
                    if (docText != null && !docText.trim().isEmpty()) {
                        logger.info("Word metni başarıyla çıkarıldı, {} karakter", docText.length());
                        return cleanExtractedText(docText);
                    } else {
                        logger.warn("Word belgesinden metin çıkarılamadı");
                        return "Word belgesinden metin çıkarılamadı. Lütfen içeriği değerlendirmeye çalış.";
                    }
                } catch (Exception e) {
                    logger.error("Word dosyası işleme hatası: {}", e.getMessage());
                    return "Word işleme hatası: " + e.getMessage() + 
                           ". Lütfen CV'yi genel hatlarıyla değerlendir.";
                }
            }
            
            // Görüntü dosyası (JPG, PNG)
            else if (isImageFile(fileType, contentType)) {
                // Yeni eklediğimiz yöntemi çağır
                return handleImageFile(file, fileType, contentType);
            }
            
            // Düz metin dosyası ise doğrudan içeriği gönder
            else {
                logger.info("Metin dosyası tespit edildi, doğrudan metin olarak kullanılacak");
                String textContent = new String(fileData);
                return cleanExtractedText(textContent);
            }
        } catch (Exception e) {
            logger.error("Dosya içeriği çıkarılamadı: {}", e.getMessage());
            return "CV dosyası işlenirken hata oluştu: " + e.getMessage() + 
                   ". Lütfen CV'yi genel hatlarıyla değerlendir.";
        }
    }
    
    /**
     * Çıkarılan metni temizler ve okunabilir hale getirir
     */
    private String cleanExtractedText(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        
        logger.info("Çıkarılan metin temizleniyor, orijinal uzunluk: {} karakter", text.length());
        
        // OCR sonucu metnini temizle
        String cleanedText = text;
        
        // Aşırı satır sonlarını temizle (3+ satır sonu karakterini 2 ile değiştir)
        cleanedText = cleanedText.replaceAll("\\n{3,}", "\n\n");
        
        // Çoklu boşlukları tek boşluğa indirgeme (3+ boşluk karakterini 1 ile değiştir)
        cleanedText = cleanedText.replaceAll("\\s{3,}", " ");
        
        // OCR hatalarını düzelt
        cleanedText = cleanedText.replaceAll("\\bl\\b", "1"); // tek 'l' harfini 1 ile değiştir
        cleanedText = cleanedText.replaceAll("\\bO\\b", "0"); // tek 'O' harfini 0 ile değiştir
        
        // CV'lerde sık yapılan OCR hatalarını düzelt
        cleanedText = cleanedText.replaceAll("(?i)Egitim", "Eğitim");
        cleanedText = cleanedText.replaceAll("(?i)Universite", "Üniversite");
        cleanedText = cleanedText.replaceAll("(?i)Lisans", "Lisans");
        cleanedText = cleanedText.replaceAll("(?i)Deneyim", "Deneyim");
        cleanedText = cleanedText.replaceAll("(?i)Beceri", "Beceri");
        cleanedText = cleanedText.replaceAll("(?i)Tecrube", "Tecrübe");
        cleanedText = cleanedText.replaceAll("(?i)Proje", "Proje");
        cleanedText = cleanedText.replaceAll("(?i)iletisim", "İletişim");
        cleanedText = cleanedText.replaceAll("(?i)E-posta", "E-posta");
        cleanedText = cleanedText.replaceAll("(?i)kisisel", "Kişisel");
        cleanedText = cleanedText.replaceAll("(?i)bilgiler", "Bilgiler");
        
        // Telefon numarası formatını düzelt
        cleanedText = cleanedText.replaceAll("(\\d)\\s+(\\d)", "$1$2"); // telefon numaralarındaki boşlukları kaldır
        
        // E-posta formatını düzelt
        cleanedText = cleanedText.replaceAll("(?i)\\b([a-z0-9._%+-]+)\\s+@\\s+([a-z0-9.-]+)\\s+\\.\\s+([a-z]{2,})\\b", "$1@$2.$3");
        
        // Paragraf düzenlemesi - liste maddelerini düzelt
        cleanedText = cleanedText.replaceAll("(?m)^[•●\\-*]\\s*", "• ");
        
        // Adresi daha okunabilir hale getir
        cleanedText = cleanedText.replaceAll("(?i)\\b(Adres)\\s*:?\\s*", "Adres: ");
        
        // Eğitim bölümünü daha belirgin yap
        cleanedText = cleanedText.replaceAll("(?i)\\b(Eğitim|EĞİTİM)\\s*:?\\s*", "\nEĞİTİM:\n");
        
        // İş deneyimini daha belirgin yap
        cleanedText = cleanedText.replaceAll("(?i)\\b(İş Deneyimi|Deneyim|İŞ DENEYİMİ)\\s*:?\\s*", "\nİŞ DENEYİMİ:\n");
        
        // Becerileri daha belirgin yap
        cleanedText = cleanedText.replaceAll("(?i)\\b(Beceriler|Yetenekler|BECERİLER)\\s*:?\\s*", "\nBECERİLER:\n");
        
        // Kişisel bilgileri daha belirgin yap
        cleanedText = cleanedText.replaceAll("(?i)\\b(Kişisel Bilgiler|KİŞİSEL BİLGİLER)\\s*:?\\s*", "\nKİŞİSEL BİLGİLER:\n");
        
        logger.info("Metin temizleme tamamlandı, yeni uzunluk: {} karakter", cleanedText.length());
        
        return cleanedText;
    }
    
    /**
     * Dosyanın görüntü dosyası olup olmadığını kontrol eder
     */
    private boolean isImageFile(String fileType, String contentType) {
        if (contentType != null && contentType.startsWith("image/")) {
            return true;
        }
        return fileType != null && (
            fileType.equalsIgnoreCase("jpg") || 
            fileType.equalsIgnoreCase("jpeg") || 
            fileType.equalsIgnoreCase("png") || 
            fileType.equalsIgnoreCase("gif") || 
            fileType.equalsIgnoreCase("bmp")
        );
    }
    
    /**
     * Dosyanın PDF olup olmadığını kontrol eder
     */
    private boolean isPdfFile(String fileType, String contentType) {
        if (contentType != null && contentType.equals("application/pdf")) {
            return true;
        }
        return fileType != null && fileType.equalsIgnoreCase("pdf");
    }
    
    /**
     * Dosyanın Word dosyası olup olmadığını kontrol eder
     */
    private boolean isWordFile(String fileType, String contentType) {
        if (contentType != null && (
            contentType.equals("application/msword") || 
            contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
        )) {
            return true;
        }
        return fileType != null && (
            fileType.equalsIgnoreCase("doc") || 
            fileType.equalsIgnoreCase("docx")
        );
    }
    
    /**
     * Prompt oluşturur
     */
    private String createPrompt(String fileContent, String fileType, String contentType, String githubUrl, Map<String, Object> githubData, String jobRequirements) {
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("Sen bir CV analiz uzmanısın. Aşağıdaki CV'yi analiz et ve istenen JSON formatında sonuç döndür.\n\n");
        
        // Dosya içeriğini ekle
        promptBuilder.append("### CV İÇERİĞİ ###\n\n");
        promptBuilder.append(fileContent).append("\n\n");
        promptBuilder.append("### CV İÇERİĞİ SONU ###\n\n");
        
        // GitHub bilgisi varsa ekle
        if (githubUrl != null && !githubUrl.isEmpty()) {
            promptBuilder.append("GitHub Profili: ").append(githubUrl).append("\n\n");
            
            // GitHub API'den alınan veriler varsa ekle
            if (githubData != null && !githubData.isEmpty() && githubData.containsKey("success") && (boolean)githubData.get("success")) {
                // Dil istatistikleri
                if (githubData.containsKey("languages")) {
                    @SuppressWarnings("unchecked")
                    Map<String, Double> languages = (Map<String, Double>) githubData.get("languages");
                    promptBuilder.append("GitHub'da Kullanılan Diller:\n");
                    
                    for (Map.Entry<String, Double> entry : languages.entrySet()) {
                        promptBuilder.append("- ").append(entry.getKey()).append(": %").append(entry.getValue()).append("\n");
                    }
                    promptBuilder.append("\n");
                }
            }
        }
        
        // İş gereksinimlerini ekle
        if (jobRequirements != null && !jobRequirements.trim().isEmpty()) {
            promptBuilder.append("### İŞ GEREKSİNİMLERİ ###\n");
            promptBuilder.append(jobRequirements).append("\n\n");
        }
        
        // NET VE KESİN JSON FORMAT TALİMATLARI
        promptBuilder.append("ÖNEMLİ: SADECE ve SADECE aşağıdaki JSON formatında cevap ver. Bunun dışında HİÇBİR açıklama ekleme.\n\n");
        
        promptBuilder.append("```json\n");
        promptBuilder.append("{\n");
        promptBuilder.append("  \"userInformation\": {\n");
        promptBuilder.append("    \"name\": \"İsim\",\n");
        promptBuilder.append("    \"surname\": \"Soyisim\",\n");
        promptBuilder.append("    \"email\": \"E-posta\",\n");
        promptBuilder.append("    \"phone\": \"Telefon\",\n");
        promptBuilder.append("    \"skills\": \"Becerilerin özeti\"\n");
        promptBuilder.append("  },\n");
        promptBuilder.append("  \"skillRatings\": [\n");
        promptBuilder.append("    { \"language\": \"Programlama Dili/Teknoloji\", \"percentage\": 0-100 arası sayı }\n");
        promptBuilder.append("  ],\n");
        promptBuilder.append("  \"compatibilityStatus\": 0-100 arası sayı,\n");
        promptBuilder.append("  \"explanation\": \"Kısa açıklama\"\n");
        promptBuilder.append("}\n");
        promptBuilder.append("```\n\n");
        
        promptBuilder.append("KURALLAR:\n");
        promptBuilder.append("1. SADECE yukarıdaki formatı kullan.\n");
        promptBuilder.append("2. Kişinin yazılımla ilgisi yoksa, skillRatings null olmalı (skillRatings: null).\n");
        promptBuilder.append("3. Başka hiçbir açıklama ekleme, sadece JSON döndür.\n");
        promptBuilder.append("4. JSON içinde Türkçe karakter kullanma.\n");
        promptBuilder.append("5. Cevabını MUTLAKA JSON biçiminde formatla - başka metin ekleme.\n");
        
        return promptBuilder.toString();
    }
    
    /**
     * API çağrısı yapar
     */
    private Map<String, Object> callGeminiAPI(String prompt) {
        try {
            logger.info("API URL: {}", apiUrl);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            Map<String, Object> requestBody = new HashMap<>();
            List<Map<String, Object>> contentsList = new ArrayList<>();
            
            Map<String, Object> contents = new HashMap<>();
            List<Map<String, Object>> parts = new ArrayList<>();
            
            Map<String, Object> textPart = new HashMap<>();
            textPart.put("text", prompt);
            parts.add(textPart);
            
            contents.put("parts", parts);
            contentsList.add(contents);
            
            requestBody.put("contents", contentsList);
            
            // API modelini konfigüre et - daha iyi CV analizi için ayarları optimize ettim
            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("temperature", 0.1);       // Daha da düşük sıcaklık - daha doğru ve kesin kişisel bilgiler için
            generationConfig.put("maxOutputTokens", 4096);  // Daha uzun yanıtlar için token limitini arttırdım
            generationConfig.put("topP", 0.95);             // Daha fazla kesinlik için topP değerini yükselttim
            generationConfig.put("topK", 30);               // Daha odaklı yanıtlar için topK değerini düşürdüm
            requestBody.put("generationConfig", generationConfig);
            
            // Güvenlik ayarları - sakıncalı içerik için alarm çalmasını engelle
            Map<String, Object> safetySettings = new HashMap<>();
            List<Map<String, Object>> safetyList = new ArrayList<>();
            
            // Güvenlik kategori ayarları - CV analizi için önemli bilgileri engelleme
            String[] categories = {
                "HARM_CATEGORY_HARASSMENT", 
                "HARM_CATEGORY_HATE_SPEECH", 
                "HARM_CATEGORY_SEXUALLY_EXPLICIT", 
                "HARM_CATEGORY_DANGEROUS_CONTENT"
            };
            
            for (String category : categories) {
                Map<String, Object> safety = new HashMap<>();
                safety.put("category", category);
                safety.put("threshold", "BLOCK_NONE"); // CV analizi için tüm içeriğe izin ver
                safetyList.add(safety);
            }
            
            requestBody.put("safetySettings", safetyList);
            
            // Tam URL 
            String fullUrl = apiUrl + "?key=" + apiKey;
            logger.info("API isteği gönderiliyor: {}", fullUrl.replace(apiKey, "API_KEY"));
            
            // İsteği gönder
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(fullUrl, request, String.class);
            
            logger.info("API yanıtı alındı. Durum kodu: {}", response.getStatusCode());
            
            // Tam yanıtı logla
            logger.info("OCR.space API tam yanıtı: {}", response.getBody());
            
            // Yanıtı JSON olarak parse et
            Map<String, Object> responseMap = objectMapper.readValue(response.getBody(), Map.class);
            
            // Hata kontrolü
            if (response.getStatusCode().is2xxSuccessful()) {
                return responseMap;
            } else {
                logger.error("API hatası: {}", responseMap);
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("success", false);
                errorResult.put("error", "API hatası: " + responseMap);
                return errorResult;
            }
        } catch (JsonProcessingException e) {
            logger.error("JSON işleme hatası: {}", e.getMessage(), e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("error", "JSON işleme hatası: " + e.getMessage());
            return errorResult;
        } catch (Exception e) {
            logger.error("API hatası: {}", e.getMessage(), e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("error", "API hatası: " + e.getMessage());
            return errorResult;
        }
    }
    
    /**
     * API yanıtını işler
     */
    private Map<String, Object> processResponse(Map<String, Object> response) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            logger.debug("API yanıt anahtarları: {}", response.keySet());
            
            // Hata kontrolü
            if (response.containsKey("error")) {
                logger.error("API hata döndürdü: {}", response.get("error"));
                result.put("success", false);
                result.put("error", "API hatası: " + response.get("error"));
                
                // Hata durumunda da formatlanmış JSON döndür
                Map<String, Object> defaultResponse = getDefaultJsonResponse();
                result.put("evaluationResult", objectMapper.writeValueAsString(defaultResponse));
                return result;
            }
            
            // Gemini yanıtını işle
            if (!response.containsKey("candidates") || response.get("candidates") == null) {
                logger.error("API yanıtında candidates bulunamadı");
                result.put("success", false);
                result.put("error", "API yanıtında candidates bulunamadı");
                
                // Hata durumunda da formatlanmış JSON döndür
                Map<String, Object> defaultResponse = getDefaultJsonResponse();
                result.put("evaluationResult", objectMapper.writeValueAsString(defaultResponse));
                return result;
            }
            
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            if (candidates.isEmpty()) {
                logger.error("API yanıtındaki candidates listesi boş");
                result.put("success", false);
                result.put("error", "API yanıtındaki candidates listesi boş");
                
                // Hata durumunda da formatlanmış JSON döndür
                Map<String, Object> defaultResponse = getDefaultJsonResponse();
                result.put("evaluationResult", objectMapper.writeValueAsString(defaultResponse));
                return result;
            }
            
            Map<String, Object> candidate = candidates.get(0);
            if (!candidate.containsKey("content") || candidate.get("content") == null) {
                logger.error("API yanıtında content bulunamadı");
                result.put("success", false);
                result.put("error", "API yanıtında content bulunamadı");
                
                // Hata durumunda da formatlanmış JSON döndür
                Map<String, Object> defaultResponse = getDefaultJsonResponse();
                result.put("evaluationResult", objectMapper.writeValueAsString(defaultResponse));
                return result;
            }
            
            Map<String, Object> content = (Map<String, Object>) candidate.get("content");
            if (!content.containsKey("parts") || content.get("parts") == null) {
                logger.error("API yanıtında parts bulunamadı");
                result.put("success", false);
                result.put("error", "API yanıtında parts bulunamadı");
                
                // Hata durumunda da formatlanmış JSON döndür
                Map<String, Object> defaultResponse = getDefaultJsonResponse();
                result.put("evaluationResult", objectMapper.writeValueAsString(defaultResponse));
                return result;
            }
            
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            if (parts.isEmpty() || !parts.get(0).containsKey("text") || parts.get(0).get("text") == null) {
                logger.error("API yanıtında text bulunamadı");
                result.put("success", false);
                result.put("error", "API yanıtında text bulunamadı");
                
                // Hata durumunda da formatlanmış JSON döndür
                Map<String, Object> defaultResponse = getDefaultJsonResponse();
                result.put("evaluationResult", objectMapper.writeValueAsString(defaultResponse));
                return result;
            }
            
            String text = (String) parts.get(0).get("text");
            logger.info("Yanıt metni alındı, uzunluk: {}, ilk 100 karakter: {}", 
                text.length(), 
                text.substring(0, Math.min(100, text.length())));
            
            // İlk olarak e-posta ve telefon bilgilerini bulmak için özel regex kullan
            checkForContactInfo(text);
            
            // JSON formatında dönen yanıtı parse et
            try {
                // JSON şablon karakterlerini temizle
                String cleanedText = text;
                
                // Markdown JSON kod bloğunu temizle (```json ... ``` formatı)
                if (text.contains("```json") || text.contains("```")) {
                    cleanedText = text.replaceAll("```json\\s*", "").replaceAll("```\\s*", "");
                    logger.info("JSON bloğu temizlendi");
                }
                
                // Başında ve sonunda boş karakterleri temizle
                cleanedText = cleanedText.trim();
                
                // JSON formatı kontrolü
                if (!cleanedText.startsWith("{") || !cleanedText.endsWith("}")) {
                    logger.warn("Yanıt geçerli bir JSON formatında değil, JSON bloğunu bulmaya çalışıyorum");
                    
                    // JSON bloğunu bulmaya çalış: {} arasındaki kısmı ara
                    int startIndex = text.indexOf('{');
                    int endIndex = text.lastIndexOf('}');
                    
                    if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
                        cleanedText = text.substring(startIndex, endIndex + 1);
                        logger.info("JSON bloğu metinden çıkarıldı: {}", cleanedText.substring(0, Math.min(50, cleanedText.length())));
                    } else {
                        logger.warn("Metinde JSON formatında bir blok bulunamadı, varsayılan JSON formatı döndürülüyor");
                        Map<String, Object> defaultResponse = getDefaultJsonResponse();
                        result.put("success", true);
                        result.put("evaluationResult", objectMapper.writeValueAsString(defaultResponse));
                        result.put("score", 0);
                        return result;
                    }
                }
                
                try {
                    // JSON'ı parse et
                    Map<String, Object> evaluation = objectMapper.readValue(cleanedText, Map.class);
                    
                    // İstenen JSON formatını dön
                    Map<String, Object> cvAnalysisResults = createCvAnalysisResults(evaluation);
                    
                    // Tüm JSON'ı logla
                    logger.info("CV Analiz Sonuçları: {}", objectMapper.writeValueAsString(cvAnalysisResults));
                    
                    result.put("success", true);
                    result.put("evaluationResult", objectMapper.writeValueAsString(cvAnalysisResults));
                    
                    // Eski değer için de compatibilityStatus değerini koy
                    result.put("score", cvAnalysisResults.get("compatibilityStatus"));
                    
                    return result;
                    
                } catch (Exception e) {
                    logger.warn("JSON parse hatası: {}, JSON: {}", e.getMessage(), cleanedText);
                    
                    // JSON parse hatası, başka bir yöntemle tekrar deneyelim
                    // Regexp ile { ve } arasındaki JSON'ı çıkarmaya çalış
                    String jsonPattern = "\\{[^\\{\\}]*(\\{[^\\{\\}]*\\})*[^\\{\\}]*\\}";
                    java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(jsonPattern);
                    java.util.regex.Matcher matcher = pattern.matcher(text);
                    
                    if (matcher.find()) {
                        String extractedJson = matcher.group(0);
                        logger.info("Regex ile JSON çıkarıldı: {}", extractedJson.substring(0, Math.min(50, extractedJson.length())));
                        
                        try {
                            Map<String, Object> evaluation = objectMapper.readValue(extractedJson, Map.class);
                            
                            // İstenen JSON formatında yanıt oluştur
                            Map<String, Object> cvAnalysisResults = createCvAnalysisResults(evaluation);
                            
                            // Tüm JSON'ı logla
                            logger.info("CV Analiz Sonuçları (Regex sonrası): {}", objectMapper.writeValueAsString(cvAnalysisResults));
                            
                            result.put("success", true);
                            result.put("evaluationResult", objectMapper.writeValueAsString(cvAnalysisResults));
                            result.put("score", cvAnalysisResults.get("compatibilityStatus"));
                            
                            return result;
                            
                        } catch (Exception ex) {
                            logger.error("Regex sonrası JSON parse hatası: {}", ex.getMessage(), ex);
                            // Varsayılan JSON formatında yanıt oluştur
                            Map<String, Object> defaultResponse = getDefaultJsonResponse();
                            result.put("success", true);
                            result.put("evaluationResult", objectMapper.writeValueAsString(defaultResponse));
                            result.put("score", 0);
                            return result;
                        }
                    } else {
                        logger.warn("Regex ile JSON çıkarılamadı, varsayılan JSON formatı döndürülüyor");
                        // Varsayılan JSON formatında yanıt oluştur
                        Map<String, Object> defaultResponse = getDefaultJsonResponse();
                        result.put("success", true);
                        result.put("evaluationResult", objectMapper.writeValueAsString(defaultResponse));
                        result.put("score", 0);
                        return result;
                    }
                }
                
            } catch (Exception e) {
                logger.error("Yanıt işleme hatası: {}", e.getMessage(), e);
                
                // Varsayılan JSON formatında yanıt oluştur
                Map<String, Object> defaultResponse = getDefaultJsonResponse();
                result.put("success", false);
                result.put("error", "Yanıt işleme hatası: " + e.getMessage());
                result.put("evaluationResult", objectMapper.writeValueAsString(defaultResponse));
                result.put("score", 0);
            }
            
            return result;
        } catch (Exception e) {
            logger.error("Yanıt işleme hatası: {}", e.getMessage(), e);
            
            try {
                // Varsayılan JSON formatında yanıt oluştur
                Map<String, Object> defaultResponse = getDefaultJsonResponse();
                result.put("success", false);
                result.put("error", "Yanıt işleme hatası: " + e.getMessage());
                result.put("evaluationResult", objectMapper.writeValueAsString(defaultResponse));
                result.put("score", 0);
            } catch (Exception ex) {
                logger.error("Varsayılan JSON formatı oluşturma hatası: {}", ex.getMessage(), ex);
                result.put("success", false);
                result.put("error", "Kritik hata: " + e.getMessage() + ", " + ex.getMessage());
                result.put("evaluationResult", "{}");
                result.put("score", 0);
            }
            
            return result;
        }
    }
    
    /**
     * Değerlendirme verilerinden CV analiz sonuçlarını oluşturan yardımcı metot
     */
    private Map<String, Object> createCvAnalysisResults(Map<String, Object> evaluation) {
        Map<String, Object> cvAnalysisResults = new HashMap<>();
        
        // userInformation alanını işle
        if (evaluation.containsKey("userInformation")) {
            cvAnalysisResults.put("userInformation", evaluation.get("userInformation"));
        } else {
            Map<String, Object> defaultUserInfo = new HashMap<>();
            defaultUserInfo.put("name", "Belirtilmemiş");
            defaultUserInfo.put("surname", "Belirtilmemiş");
            defaultUserInfo.put("email", "Belirtilmemiş");
            defaultUserInfo.put("phone", "Belirtilmemiş");
            defaultUserInfo.put("skills", "");
            cvAnalysisResults.put("userInformation", defaultUserInfo);
        }
        
        // skillRatings alanını işle
        if (evaluation.containsKey("skillRatings")) {
            cvAnalysisResults.put("skillRatings", evaluation.get("skillRatings"));
        } else {
            cvAnalysisResults.put("skillRatings", null);
        }
        
        // compatibilityStatus alanını işle
        if (evaluation.containsKey("compatibilityStatus")) {
            Object compatibilityStatus = evaluation.get("compatibilityStatus");
            // Sayı tipine dönüştür
            if (compatibilityStatus instanceof Integer) {
                cvAnalysisResults.put("compatibilityStatus", compatibilityStatus);
            } else if (compatibilityStatus instanceof Double) {
                cvAnalysisResults.put("compatibilityStatus", ((Double) compatibilityStatus).intValue());
            } else if (compatibilityStatus instanceof String) {
                try {
                    cvAnalysisResults.put("compatibilityStatus", Integer.parseInt(compatibilityStatus.toString()));
                } catch (NumberFormatException e) {
                    cvAnalysisResults.put("compatibilityStatus", 0);
                }
            } else {
                cvAnalysisResults.put("compatibilityStatus", 0);
            }
        } else {
            cvAnalysisResults.put("compatibilityStatus", 0);
        }
        
        // explanation alanını işle
        if (evaluation.containsKey("explanation")) {
            cvAnalysisResults.put("explanation", evaluation.get("explanation"));
        } else {
            cvAnalysisResults.put("explanation", "");
        }
        
        return cvAnalysisResults;
    }
    
    /**
     * Varsayılan JSON yanıt formatı oluşturan yardımcı metot
     */
    private Map<String, Object> getDefaultJsonResponse() {
        Map<String, Object> defaultResponse = new HashMap<>();
        
        // Varsayılan kullanıcı bilgileri
        Map<String, Object> defaultUserInfo = new HashMap<>();
        defaultUserInfo.put("name", "Belirtilmemiş");
        defaultUserInfo.put("surname", "Belirtilmemiş");
        defaultUserInfo.put("email", "Belirtilmemiş");
        defaultUserInfo.put("phone", "Belirtilmemiş");
        defaultUserInfo.put("skills", "");
        defaultResponse.put("userInformation", defaultUserInfo);
        
        // Varsayılan beceri oranları (null olarak ayarla)
        defaultResponse.put("skillRatings", null);
        
        // Varsayılan uyumluluk durumu
        defaultResponse.put("compatibilityStatus", 0);
        
        // Varsayılan açıklama
        defaultResponse.put("explanation", "CV analizi sırasında bir hata oluştu.");
        
        return defaultResponse;
    }
    
    /**
     * Metinde e-posta ve telefon numaralarını kontrol eder (debug için)
     */
    private void checkForContactInfo(String text) {
        if (text == null || text.isEmpty()) {
            return;
        }
        
        // E-posta formatlarını ara
        java.util.regex.Pattern emailPattern = java.util.regex.Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
        java.util.regex.Matcher emailMatcher = emailPattern.matcher(text);
        
        // Bulunan tüm e-postaları logla
        while (emailMatcher.find()) {
            String email = emailMatcher.group();
            logger.info("API yanıtında e-posta tespit edildi: {}", email);
        }
        
        // Telefon numarası formatlarını ara - Türkçe telefon numaraları için
        java.util.regex.Pattern phonePattern = java.util.regex.Pattern.compile("(\\+90[ ]?|0)?(5\\d{2}[ ]?\\d{3}[ ]?\\d{2}[ ]?\\d{2}|5\\d{2}[ ]?\\d{3}[ ]?\\d{4})");
        java.util.regex.Matcher phoneMatcher = phonePattern.matcher(text);
        
        // Bulunan tüm telefonları logla
        while (phoneMatcher.find()) {
            String phone = phoneMatcher.group();
            logger.info("API yanıtında telefon tespit edildi: {}", phone);
        }
    }
    
    /**
     * Görüntü dosyası (JPG, PNG)
     */
    private String handleImageFile(File file, String fileType, String contentType) {
        logger.info("Görüntü dosyası tespit edildi: format={}, contentType={}", fileType, contentType);
        logger.info("🔍 İşlem başlatılıyor - Dosya: {} ({} bytes)", file.getName(), file.length());
        
        try {
            // Tesseract kullanımını kaldırdık, doğrudan OCR.space API kullanılıyor
            logger.info("🔄 OCR.space API ile bulut tabanlı OCR başlatılıyor...");
            
            try {
                String cloudOcrText = performCloudOCR(file, fileType);
                
                if (cloudOcrText != null && !cloudOcrText.trim().isEmpty()) {
                    logger.info("✅ BULUT OCR BAŞARILI: {} karakter çıkarıldı", cloudOcrText.length());
                    logger.info("📄 BULUT OCR METNİ ÖRNEĞİ: {}", cloudOcrText.substring(0, Math.min(300, cloudOcrText.length())));
                    
                    // OCR metnini temizle
                    String cleanedText = cleanExtractedText(cloudOcrText);
                    return "Bu CV'den Bulut OCR (OCR.space) ile çıkarılan metin:\n\n" + cleanedText + 
                           "\n\nLütfen OCR hatalarını göz önünde bulundurarak kişisel bilgileri (isim, soyisim, e-posta, telefon) ve teknik becerileri doğru tespit etmeye çalış.";
                } else {
                    logger.warn("❌ BULUT OCR BAŞARISIZ: Metin çıkarılamadı, alternatif yöntem kullanılıyor");
                }
            } catch (Exception e) {
                logger.error("❌ Bulut OCR işlemi sırasında hata: {}", e.getMessage(), e);
            }
            
            // OCR.space API başarısız olduysa, alternatif işlemeyi kullan
            return useAlternativeImageProcessing(file, fileType, contentType);
            
        } catch (Exception e) {
            logger.error("❌ Görüntü işleme sırasında hata: {}", e.getMessage(), e);
            return useAlternativeImageProcessing(file, fileType, contentType);
        }
    }
    
    /**
     * OCR.space API kullanarak bulut tabanlı OCR gerçekleştirir
     */
    private String performCloudOCR(File file, String fileType) throws IOException {
        logger.info("==== BULUT OCR İŞLEMİ BAŞLIYOR ====");
        logger.info("📷 İşlenecek dosya: {}, Dosya tipi: {}", file.getAbsolutePath(), fileType);
        logger.info("📏 Dosya boyutu: {} bytes", file.length());
        
        try {
            // API isteği için multipart form oluştur
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set("apikey", ocrSpaceApiKey);
            
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new FileSystemResource(file));
            
            // OCR.space API parametreleri - tek dil kodu kullan
            body.add("language", "tur");       // Sadece Türkçe (OCR.space gelişmiş dil paketiyle birlikte gelir)
            body.add("OCREngine", "2");        // Daha gelişmiş OCR motoru (2 = Neural OCR)
            body.add("scale", "true");         // Görüntüyü ölçeklendir
            body.add("detectOrientation", "true"); // Otomatik yönlendirme algılama
            
            // DPI optimize et ve görüntü iyileştirmelerini etkinleştir
            body.add("isCreateSearchablePdf", "false"); // PDF üretme
            body.add("isSearchablePdfHideTextLayer", "false"); // Gereksiz
            
            // İstek ve yanıt loglarını iyileştir
            logger.info("OCR.space API isteği gönderiliyor: language=tur, OCREngine=2, scale=true, detectOrientation=true");
            
            // İnternet bağlantısı kontrolü - basit ping testi
            try {
                boolean isNetworkAvailable = java.net.InetAddress.getByName("api.ocr.space").isReachable(5000);
                if (!isNetworkAvailable) {
                    logger.warn("⚠️ İnternet bağlantısı sorunu: OCR.space sunucusuna erişilemiyor");
                }
            } catch (Exception e) {
                logger.warn("⚠️ İnternet bağlantısı kontrolü yapılamadı: {}", e.getMessage());
            }
            
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            
            logger.info("⏱️ OCR.space API isteği gönderiliyor...");
            long startTime = System.currentTimeMillis();
            
            // Hata yakalamayı geliştir
            ResponseEntity<String> response;
            try {
                // Artırılmış zaman aşımı ayarları ile özel RestTemplate kullan
                response = ocrRestTemplate.postForEntity(ocrSpaceApiUrl, requestEntity, String.class);
            } catch (org.springframework.web.client.ResourceAccessException e) {
                logger.error("⚠️ OCR.space API bağlantı hatası: {} - Internet bağlantınızı kontrol edin!", e.getMessage());
                return null;
            } catch (Exception e) {
                logger.error("⚠️ OCR.space API istisna: {}", e.getMessage());
                return null;
            }
            
            long endTime = System.currentTimeMillis();
            long durationMs = endTime - startTime;
            logger.info("⏱️ OCR.space API yanıtı alındı: {} ms", durationMs);
            
            // Hata kontrolü
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // Yanıtın ilk 1000 karakterini logla
                String responsePreview = response.getBody().length() > 1000 ? 
                                        response.getBody().substring(0, 1000) + "..." : 
                                        response.getBody();
                logger.info("OCR.space API yanıtı (önizleme): {}", responsePreview);
                
                String ocrText = processOCRSpaceResponse(response.getBody());
                
                // Yanıt istatistikleri
                if (ocrText != null && !ocrText.isEmpty()) {
                    int textLength = ocrText.length();
                    int lineCount = ocrText.split("\n").length;
                    int wordCount = ocrText.split("\\s+").length;
                    
                    logger.info("📊 BULUT OCR İSTATİSTİKLERİ:");
                    logger.info("  - Metin uzunluğu: {} karakter", textLength);
                    logger.info("  - Satır sayısı: {}", lineCount);
                    logger.info("  - Kelime sayısı: {}", wordCount);
                    
                    // Ham OCR metninin örneğini göster
                    logger.info("📑 BULUT OCR METNİ:");
                    logger.info("────────────────────────────────────────────");
                    String[] lines = ocrText.split("\n");
                    int lineMax = Math.min(25, lines.length);
                    for (int i = 0; i < lineMax; i++) {
                        logger.info("Satır {}: {}", i+1, lines[i]);
                    }
                    
                    if (lines.length > lineMax) {
                        logger.info("... ve {} satır daha ...", lines.length - lineMax);
                    }
                    logger.info("────────────────────────────────────────────");
                    
                    // Metni geliştirilmiş algoritma ile temizle
                    String enhancedText = enhanceContactInfoExtraction(ocrText);
                    
                    logger.info("✅ BULUT OCR İŞLEMİ BAŞARILI");
                    logger.info("==== BULUT OCR İŞLEMİ TAMAMLANDI ====");
                    
                    return enhancedText;
                } else {
                    logger.error("❌ BULUT OCR METNİ BOŞ");
                    logger.info("==== BULUT OCR İŞLEMİ TAMAMLANDI ====");
                    return null;
                }
            } else {
                logger.error("❌ BULUT OCR API YANIT HATASI: {}", response.getStatusCodeValue());
                if (response.getBody() != null) {
                    logger.error("Hata yanıtı: {}", response.getBody());
                }
                logger.info("==== BULUT OCR İŞLEMİ TAMAMLANDI ====");
                return null;
            }
        } catch (Exception e) {
            logger.error("❌ BULUT OCR İŞLEMİ BAŞARISIZ: {}", e.getMessage(), e);
            logger.error("Hata sınıfı: {}", e.getClass().getName());
            logger.info("==== BULUT OCR İŞLEMİ HATALI TAMAMLANDI ====");
            return null;
        }
    }
    
    /**
     * OCR.space API yanıtını işler
     */
    private String processOCRSpaceResponse(String responseJson) {
        try {
            logger.info("OCR.space API yanıtı işleniyor");
            
            // Boş yanıt kontrolü
            if (responseJson == null || responseJson.trim().isEmpty()) {
                logger.error("OCR.space API boş yanıt döndü");
                return null;
            }
            
            // JSON parse et
            Map<String, Object> responseMap;
            try {
                responseMap = objectMapper.readValue(responseJson, Map.class);
            } catch (Exception e) {
                logger.error("API yanıtını JSON olarak parse etme hatası: {}", e.getMessage());
                return null;
            }
            
            // Hata kontrolü
            if (responseMap.containsKey("IsErroredOnProcessing") && Boolean.TRUE.equals(responseMap.get("IsErroredOnProcessing"))) {
                String errorMessage = responseMap.containsKey("ErrorMessage") ? 
                                     String.valueOf(responseMap.get("ErrorMessage")) : "Bilinmeyen hata";
                logger.error("OCR.space API hatası: {}", errorMessage);
                return null;
            }
            
            // ParsedResults içinden metni çıkar
            StringBuilder fullText = new StringBuilder();
            
            if (responseMap.containsKey("ParsedResults")) {
                Object parsedResultsObj = responseMap.get("ParsedResults");
                
                if (parsedResultsObj instanceof List) {
                    List<Object> results = (List<Object>) parsedResultsObj;
                    logger.info("ParsedResults listesi içeriyor: {} sonuç", results.size());
                    
                    if (!results.isEmpty()) {
                        for (Object resultObj : results) {
                            // Her sonucu Map olarak dönüştür
                            if (resultObj instanceof Map) {
                                Map<String, Object> resultItem = (Map<String, Object>) resultObj;
                                
                                // ParsedText kontrolü ve türü güvenli bir şekilde kontrol et
                                if (resultItem.containsKey("ParsedText")) {
                                    Object parsedTextObj = resultItem.get("ParsedText");
                                    
                                    // ParsedText türüne göre işle
                                    if (parsedTextObj instanceof String) {
                                        // String ise doğrudan kullan
                                        String parsedText = (String) parsedTextObj;
                                        fullText.append(parsedText).append("\n\n");
                                        
                                        logger.info("Metin başarıyla çıkarıldı: {} karakter", parsedText.length());
                                    } 
                                    else if (parsedTextObj instanceof List) {
                                        // Liste ise, listeyi birleştir
                                        List<Object> textList = (List<Object>) parsedTextObj;
                                        logger.info("ParsedText liste olarak döndü: {} öğe", textList.size());
                                        
                                        for (Object textItem : textList) {
                                            if (textItem instanceof String) {
                                                fullText.append((String)textItem).append("\n");
                                            } else if (textItem != null) {
                                                fullText.append(textItem.toString()).append("\n");
                                            }
                                        }
                                        
                                        logger.info("Liste olarak metin çıkarıldı: {} öğe", textList.size());
                                    }
                                    else {
                                        // Diğer türler için toString kullan
                                        String parsedText = String.valueOf(parsedTextObj);
                                        fullText.append(parsedText).append("\n\n");
                                        
                                        logger.info("Metin türü dönüştürülerek çıkarıldı: {}", parsedTextObj.getClass().getName());
                                    }
                                } else {
                                    logger.warn("ParsedText alanı bulunamadı");
                                }
                            } else if (resultObj != null) {
                                logger.warn("ParsedResults içindeki öğe Map değil: {}", resultObj.getClass().getName());
                            }
                        }
                    } else {
                        logger.warn("ParsedResults listesi boş");
                    }
                } else {
                    logger.warn("ParsedResults liste değil: {}", parsedResultsObj.getClass().getName());
                }
            } else {
                logger.warn("ParsedResults alanı bulunamadı. Mevcut alanlar: {}", responseMap.keySet());
            }
            
            String extractedText = fullText.toString().trim();
            
            if (!extractedText.isEmpty()) {
                // Çıkarılan metni temizle ve düzenle
                extractedText = cleanExtractedText(extractedText);
                
                logger.info("OCR.space API başarıyla metin çıkarıldı: {} karakter", extractedText.length());
                return extractedText;
            } else {
                logger.error("OCR.space API'den metin çıkarılamadı");
                return null;
            }
            
        } catch (Exception e) {
            logger.error("OCR.space API yanıtı işlenirken hata: {}", e.getMessage(), e);
            logger.error("Hata türü: {}", e.getClass().getName());
            return null;
        }
    }
    
    /**
     * Tesseract olmadan alternatif görüntü işleme yöntemi
     */
    private String useAlternativeImageProcessing(File file, String fileType, String contentType) {
        try {
            logger.info("🔄 Alternatif görüntü işleme yöntemi kullanılıyor");
            
            // Görüntü dosyası hakkında temel bilgileri çıkar
            StringBuilder imageInfo = new StringBuilder();
            imageInfo.append("CV Görüntü Dosyası Bilgileri:\n\n");
            imageInfo.append("- Dosya Adı: ").append(file.getName()).append("\n");
            imageInfo.append("- Dosya Tipi: ").append(fileType.toUpperCase()).append("\n");
            imageInfo.append("- Dosya Boyutu: ").append(file.length()).append(" bytes\n");
            
            // Dosya karakteristiklerini kontrol et
            imageInfo.append("\nBu CV bir görüntü dosyası (").append(fileType.toUpperCase()).append(") olduğu için ");
            imageInfo.append("metni doğrudan çıkaramıyorum. OCR yazılımı (Tesseract) şu anda kullanılamıyor.\n\n");
            
            imageInfo.append("Lütfen aşağıdaki bilgilere dikkat ederek CV'yi değerlendir:\n\n");
            imageInfo.append("1. Kişinin adı, soyadı, iletişim bilgileri (e-posta, telefon) genellikle CV'nin üst kısmında bulunur.\n");
            imageInfo.append("2. Eğitim, deneyim ve beceriler bölümleri CV'nin ana gövdesinde yer alır.\n");
            imageInfo.append("3. Kişisel bilgilere, eğitim durumuna ve iş deneyimine bakarak değerlendirme yap.\n");
            imageInfo.append("4. Görünür teknik becerileri listele ve gerçekçi yüzdelerle değerlendir.\n\n");
            
            imageInfo.append("CV'yi bu bilgiler ışığında değerlendirip, JSON formatında bir sonuç döndür. ");
            imageInfo.append("Eğer bilgi yetersizse, verileri 'Belirtilmemiş' olarak işaretle, ancak genel bir değerlendirme yap.");
            
            return imageInfo.toString();
        } catch (Exception e) {
            logger.error("Alternatif görüntü işleme hatası: {}", e.getMessage());
            return "CV görüntü dosyası işlenirken hata oluştu. Lütfen CV'yi manuel olarak değerlendir.";
        }
    }
    
    /**
     * Kişisel bilgilerin (e-posta, telefon) daha iyi çıkarılması için metin iyileştirmesi yapar
     */
    private String enhanceContactInfoExtraction(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        StringBuilder enhancedText = new StringBuilder(text);
        
        // E-posta formatlarını vurgula
        java.util.regex.Pattern emailPattern = java.util.regex.Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
        java.util.regex.Matcher emailMatcher = emailPattern.matcher(text);
        
        // E-posta adreslerini bul ve vurgula
        while (emailMatcher.find()) {
            String email = emailMatcher.group();
            enhancedText.append("\n\nBULUNAN E-POSTA: ").append(email).append("\n");
        }
        
        // Telefon numarası formatlarını vurgula - Türkçe telefon numaraları için
        // +90 5XX XXX XXXX, 05XX XXX XXXX, 5XX XXX XX XX formatları
        java.util.regex.Pattern phonePattern = java.util.regex.Pattern.compile("(\\+90[ ]?|0)?(5\\d{2}[ ]?\\d{3}[ ]?\\d{2}[ ]?\\d{2}|5\\d{2}[ ]?\\d{3}[ ]?\\d{4})");
        java.util.regex.Matcher phoneMatcher = phonePattern.matcher(text);
        
        // Telefon numaralarını bul ve vurgula
        while (phoneMatcher.find()) {
            String phone = phoneMatcher.group();
            enhancedText.append("\n\nBULUNAN TELEFON: ").append(phone).append("\n");
        }
        
        return enhancedText.toString();
    }
} 