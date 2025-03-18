package com.birkann.dto;

import java.util.Date;

/**
 * CV değerlendirme özet yanıt sınıfı.
 * Sadece temel özet bilgileri içerir, detaylı içerikleri barındırmaz.
 */
public class CVEvaluationSummaryResponse {
    private Long id;
    private Long userId;
    private String fullName;
    private Integer evaluationScore;
    private Date evaluationDate;
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public Integer getEvaluationScore() {
        return evaluationScore;
    }
    
    public void setEvaluationScore(Integer evaluationScore) {
        this.evaluationScore = evaluationScore;
    }
    
    public Date getEvaluationDate() {
        return evaluationDate;
    }
    
    public void setEvaluationDate(Date evaluationDate) {
        this.evaluationDate = evaluationDate;
    }
} 