package com.birkann.model;

import java.util.List;
import java.util.Map;

import lombok.Data;

/**
 * GitHub kullanıcı verilerini depolamak için kullanılan model sınıfı
 */
@Data
public class GitHubUserData {
    private boolean success;
    private String error;
    private Map<String, Object> userInfo;
    private List<Map<String, Object>> repos;
    private Map<String, Double> languages;
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getError() {
        return error;
    }
    
    public void setError(String error) {
        this.error = error;
    }
    
    public Map<String, Object> getUserInfo() {
        return userInfo;
    }
    
    public void setUserInfo(Map<String, Object> userInfo) {
        this.userInfo = userInfo;
    }
    
    public List<Map<String, Object>> getRepos() {
        return repos;
    }
    
    public void setRepos(List<Map<String, Object>> repos) {
        this.repos = repos;
    }
    
    public Map<String, Double> getLanguages() {
        return languages;
    }
    
    public void setLanguages(Map<String, Double> languages) {
        this.languages = languages;
    }
} 