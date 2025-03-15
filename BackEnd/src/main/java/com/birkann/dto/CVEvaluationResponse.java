package com.birkann.dto;

import java.util.Date;

import com.birkann.enums.FileType;

public class CVEvaluationResponse {
    private Long id;
    private String fileName;
    private FileType fileType;
    private String githubUrl;
    private String jobRequirements;
    private Integer evaluationScore;
    private String evaluationResult;
    private Date evaluationDate;
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public FileType getFileType() {
        return fileType;
    }
    
    public void setFileType(FileType fileType) {
        this.fileType = fileType;
    }
    
    public String getGithubUrl() {
        return githubUrl;
    }
    
    public void setGithubUrl(String githubUrl) {
        this.githubUrl = githubUrl;
    }
    
    public Integer getEvaluationScore() {
        return evaluationScore;
    }
    
    public void setEvaluationScore(Integer evaluationScore) {
        this.evaluationScore = evaluationScore;
    }
    
    public String getEvaluationResult() {
        return evaluationResult;
    }
    
    public void setEvaluationResult(String evaluationResult) {
        this.evaluationResult = evaluationResult;
    }
    
    public Date getEvaluationDate() {
        return evaluationDate;
    }
    
    public void setEvaluationDate(Date evaluationDate) {
        this.evaluationDate = evaluationDate;
    }
    
    public String getJobRequirements() {
        return jobRequirements;
    }
    
    public void setJobRequirements(String jobRequirements) {
        this.jobRequirements = jobRequirements;
    }
} 