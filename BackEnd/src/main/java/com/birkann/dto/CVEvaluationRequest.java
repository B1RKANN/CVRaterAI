package com.birkann.dto;

import org.springframework.web.multipart.MultipartFile;

public class CVEvaluationRequest {
    private MultipartFile file;
    private String githubUrl;
    
    public MultipartFile getFile() {
        return file;
    }
    
    public void setFile(MultipartFile file) {
        this.file = file;
    }
    
    public String getGithubUrl() {
        return githubUrl;
    }
    
    public void setGithubUrl(String githubUrl) {
        this.githubUrl = githubUrl;
    }
} 