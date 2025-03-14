package com.birkann.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.birkann.model.Credit;
import com.birkann.model.User;
import com.birkann.repository.CreditRepository;
import com.birkann.repository.UserRepository;
import com.birkann.service.impl.CreditSchedulerService;

@RestController
@RequestMapping("/api/v1/credit")
public class CreditController {

    private static final Logger logger = LoggerFactory.getLogger(CreditController.class);
    private static final List<String> schedulerLogs = new ArrayList<>();
    private static boolean schedulerActive = false;
    
    // 1 hafta için milisaniye cinsinden değer
    private static final long ONE_WEEK_IN_MS = 7 * 24 * 60 * 60 * 1000L;
    
    static {
        // Başlangıçta scheduler durumunu belirt
        schedulerLogs.add("Scheduler log kaydı başlatıldı: " + new Date());
    }

    @Autowired
    private CreditSchedulerService creditSchedulerService;
    
    @Autowired
    private CreditRepository creditRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    // Scheduler'ın son durumunu ekleme
    public static void addSchedulerLog(String message) {
        String logEntry = new Date() + " - " + message;
        schedulerLogs.add(logEntry);
        // Maksimum 100 log tut
        if (schedulerLogs.size() > 100) {
            schedulerLogs.remove(0);
        }
    }
    
    @GetMapping("/scheduler-logs")
    public ResponseEntity<List<String>> getSchedulerLogs() {
        return ResponseEntity.ok(schedulerLogs);
    }
    
    @GetMapping("/scheduler-status")
    public ResponseEntity<String> getSchedulerStatus() {
        return ResponseEntity.ok("Scheduler aktif mi: " + schedulerActive + 
                "\nSon log sayısı: " + schedulerLogs.size());
    }
    
    @PostMapping("/force-check")
    public ResponseEntity<String> forceCheckCredits() {
        logger.info("Kredi kontrolü manuel olarak tetiklendi");
        try {
            creditSchedulerService.logCreditStatus();
            creditSchedulerService.checkAndResetCredits();
            creditSchedulerService.directDatabaseReset();
            addSchedulerLog("Kredi kontrolü manuel olarak tetiklendi ve başarılı");
            schedulerActive = true;
            return ResponseEntity.ok("Kredi kontrolü manuel olarak tetiklendi ve başarıyla tamamlandı");
        } catch (Exception e) {
            addSchedulerLog("Kredi kontrolü manuel tetikleme HATASI: " + e.getMessage());
            return ResponseEntity.ok("Kredi kontrolü hatası: " + e.getMessage());
        }
    }
    
    @PostMapping("/test-scheduler/{userCreditValue}")
    public ResponseEntity<String> testScheduler(@PathVariable int userCreditValue) {
        logger.info("Scheduler test işlemi başlatılıyor...");
        List<Credit> allCredits = creditRepository.findAll();
        
        if (allCredits.isEmpty()) {
            return ResponseEntity.badRequest().body("Krediler bulunamadı!");
        }
        
        // Test için ilk krediyi eski bir tarihe set et
        Credit testCredit = allCredits.get(0);
        Date pastDate = new Date(System.currentTimeMillis() - (60 * 60 * 1000)); // 1 saat önce 
        
        testCredit.setExpiredDate(pastDate);
        testCredit.setUserCredit(userCreditValue); // Test değeri
        
        Credit savedCredit = creditRepository.save(testCredit);
        
        logger.info("Test kredi ayarlandı: ID={}, UserCredit={}, ExpiredDate={}", 
                savedCredit.getId(), savedCredit.getUserCredit(), savedCredit.getExpiredDate());
        
        addSchedulerLog("Scheduler test için kredi ayarlandı: " + savedCredit.getId() +
                " UserCredit=" + userCreditValue + ", ExpiredDate=" + pastDate);
        
        return ResponseEntity.ok("Test kredi ayarlandı. 1 dakika içinde tekrar kontrol edin (api/v1/credit/scheduler-logs endpointinden)");
    }
    
    @PostMapping("/reset/{userId}")
    public ResponseEntity<?> resetUserCredit(@PathVariable Long userId) {
        logger.info("Kullanıcı kredisi manuel olarak resetleniyor: {}", userId);
        
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            return ResponseEntity.badRequest().body("Kullanıcı bulunamadı: " + userId);
        }
        
        User user = userOpt.get();
        Credit credit = user.getCredit();
        
        if (credit == null) {
            return ResponseEntity.badRequest().body("Kullanıcının kredi kaydı bulunamadı: " + userId);
        }
        
        logger.info("Mevcut kredi durumu: ID={}, UserCredit={}", credit.getId(), credit.getUserCredit());
        
        // Krediyi direkt olarak güncelle
        credit.setUserCredit(20);
        credit.setStartDate(new Date());
        credit.setExpiredDate(new Date(System.currentTimeMillis() + ONE_WEEK_IN_MS));
        
        Credit savedCredit = creditRepository.save(credit);
        logger.info("Kredi manuel olarak 20'ye resetlendi: ID={}, UserCredit={}", 
            savedCredit.getId(), savedCredit.getUserCredit());
            
        return ResponseEntity.ok(savedCredit);
    }
    
    @PostMapping("/reset-all")
    public ResponseEntity<?> resetAllCredits() {
        logger.info("Tüm krediler manuel olarak resetleniyor");
        
        List<Credit> allCredits = creditRepository.findAll();
        logger.info("Toplam kredi sayısı: {}", allCredits.size());
        
        Date currentDate = new Date();
        Date newExpiredDate = new Date(currentDate.getTime() + ONE_WEEK_IN_MS);
        
        for (Credit credit : allCredits) {
            logger.info("Kredi resetleniyor: ID={}, Eski UserCredit={}", credit.getId(), credit.getUserCredit());
            credit.setUserCredit(20);
            credit.setStartDate(currentDate);
            credit.setExpiredDate(newExpiredDate);
        }
        
        List<Credit> savedCredits = creditRepository.saveAll(allCredits);
        logger.info("{} kredi başarıyla 20'ye resetlendi", savedCredits.size());
        
        return ResponseEntity.ok("Tüm krediler başarıyla resetlendi. Toplam: " + savedCredits.size());
    }
} 