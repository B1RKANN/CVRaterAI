package com.birkann.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import com.birkann.controller.CreditController;
import com.birkann.model.Credit;
import com.birkann.repository.CreditRepository;

@Service
public class CreditSchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(CreditSchedulerService.class);
    // 1 hafta için milisaniye cinsinden değer
    private static final long ONE_WEEK_IN_MS = 7 * 24 * 60 * 60 * 1000L;

    @Autowired
    private CreditRepository creditRepository;
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Durum kontrol eden scheduler - 1 dakikada bir çalışır
     */
    @Scheduled(fixedRate = 60000) // Her 1 dakikada bir çalışır
    public void logCreditStatus() {
        logger.info("=== Kredi durum kontrolü başladı ===");
        CreditController.addSchedulerLog("Kredi durum kontrolü başladı");
        try {
            List<Credit> allCredits = creditRepository.findAll();
            logger.info("Toplam kredi sayısı: {}", allCredits.size());
            CreditController.addSchedulerLog("Toplam kredi sayısı: " + allCredits.size());
            
            Date now = new Date();
            int expiredCount = 0;
            
            for (Credit credit : allCredits) {
                boolean isExpired = credit.getExpiredDate() != null && 
                    (credit.getExpiredDate().before(now) || credit.getExpiredDate().equals(now));
                
                if (isExpired) {
                    expiredCount++;
                }
                
                logger.info("Kredi: ID={}, UserCredit={}, ExpiredDate={}, Expired={}", 
                    credit.getId(), credit.getUserCredit(), credit.getExpiredDate(), isExpired);
            }
            
            CreditController.addSchedulerLog("Süresi dolmuş kredi sayısı: " + expiredCount);
        } catch (Exception e) {
            logger.error("Kredi durum kontrolü hatası: {}", e.getMessage());
            CreditController.addSchedulerLog("Kredi durum kontrolü HATASI: " + e.getMessage());
        }
        logger.info("=== Kredi durum kontrolü tamamlandı ===");
    }

    /**
     * JPA ile kredi resetleme - 1 dakikada bir çalışır
     */
    @Scheduled(fixedDelay = 60000) // Her 1 dakikada bir çalışır
    @Transactional
    public void checkAndResetCredits() {
        Date currentDate = new Date();
        logger.info("=== Kredi otomatik resetleme işlemi başladı - {} ===", currentDate);
        CreditController.addSchedulerLog("Kredi otomatik resetleme işlemi başladı");
        
        try {
            // Direkt olarak JDBC ile güncelleme
            try {
                logger.info("JDBC ile direkt güncelleme yapılıyor...");
                Date newExpiredDate = new Date(currentDate.getTime() + ONE_WEEK_IN_MS);
                int updatedRows = jdbcTemplate.update(
                    "UPDATE public.credit SET user_credit = 20, start_date = ?, expired_date = ? WHERE expired_date <= ?",
                    currentDate, newExpiredDate, currentDate);
                
                logger.info("JDBC ile {} kredi kaydı güncellendi", updatedRows);
                CreditController.addSchedulerLog("JDBC ile " + updatedRows + " kredi kaydı güncellendi");
                
                if (updatedRows > 0) {
                    logger.info("Başarılı JDBC güncellemesi!");
                    CreditController.addSchedulerLog("Başarılı JDBC güncellemesi: " + updatedRows + " kayıt");
                    return; // Başarılı ise devam etmeye gerek yok
                }
            } catch (Exception e) {
                logger.error("JDBC ile güncelleme yapılamadı: {}", e.getMessage());
                CreditController.addSchedulerLog("JDBC ile güncelleme HATASI: " + e.getMessage());
            }
            
            // JPA ile reset-all benzeri basit güncelleme
            List<Credit> allCredits = creditRepository.findAll();
            logger.info("Süresi dolmuş kredi kontrolü yapılıyor... Toplam: {}", allCredits.size());
            CreditController.addSchedulerLog("JPA ile kredi kontrolü - Toplam: " + allCredits.size());
            
            boolean anyUpdated = false;
            Date newExpiredDate = new Date(currentDate.getTime() + ONE_WEEK_IN_MS);
            
            for (Credit credit : allCredits) {
                if (credit.getExpiredDate() == null) {
                    logger.info("Kredi {} - null expiredDate, güncellenecek", credit.getId());
                    credit.setExpiredDate(newExpiredDate);
                    credit.setStartDate(currentDate);
                    credit.setUserCredit(20);
                    anyUpdated = true;
                    CreditController.addSchedulerLog("Kredi " + credit.getId() + " null expiredDate güncellendi");
                }
                else if (credit.getExpiredDate().before(currentDate)) {
                    logger.info("Kredi {} - süresi dolmuş! ({} < {})", 
                        credit.getId(), credit.getExpiredDate(), currentDate);
                    credit.setUserCredit(20);
                    credit.setStartDate(currentDate);
                    credit.setExpiredDate(newExpiredDate);
                    anyUpdated = true;
                    CreditController.addSchedulerLog("Kredi " + credit.getId() + " süresi dolmuş kredi güncellendi");
                }
            }
            
            if (anyUpdated) {
                List<Credit> savedCredits = creditRepository.saveAll(allCredits);
                logger.info("Toplam {} kredi güncellendi", savedCredits.size());
                CreditController.addSchedulerLog("JPA ile toplam " + savedCredits.size() + " kredi güncellendi");
                entityManager.flush(); // Değişiklikleri hemen uygula
            } else {
                logger.info("Güncellenecek kredi bulunamadı");
                CreditController.addSchedulerLog("JPA ile güncellenecek kredi bulunamadı");
            }
            
        } catch (Exception e) {
            logger.error("Kredi güncellemesi sırasında hata oluştu: ", e);
            CreditController.addSchedulerLog("Kredi güncellemesi HATASI: " + e.getMessage());
            e.printStackTrace();
        }
        
        logger.info("=== Kredi otomatik resetleme işlemi tamamlandı ===");
    }
    
    /**
     * Direkt SQL ile güncelleme - 1 dakikada bir çalışır
     */
    @Scheduled(fixedDelay = 60000) // Her 1 dakikada bir çalışır
    public void directDatabaseReset() {
        logger.info("=== Direkt veritabanı güncelleme işlemi başladı ===");
        CreditController.addSchedulerLog("Direkt veritabanı güncelleme işlemi başladı");
        try {
            Date currentDate = new Date();
            Date newExpireDate = new Date(currentDate.getTime() + ONE_WEEK_IN_MS);
            
            int updated = jdbcTemplate.update(
                "UPDATE public.credit SET user_credit = 20, start_date = ?, expired_date = ? " +
                "WHERE expired_date IS NOT NULL AND expired_date <= ?",
                currentDate, newExpireDate, currentDate);
            
            logger.info("Direkt SQL sorgusu ile {} kredi güncellendi", updated);
            CreditController.addSchedulerLog("Direkt SQL sorgusu ile " + updated + " kredi güncellendi");
        } catch (Exception e) {
            logger.error("Direkt veritabanı güncelleme hatası: {}", e.getMessage());
            CreditController.addSchedulerLog("Direkt veritabanı güncelleme HATASI: " + e.getMessage());
        }
        logger.info("=== Direkt veritabanı güncelleme işlemi tamamlandı ===");
    }
} 