package com.birkann.model;

import java.util.Date;

import com.birkann.enums.FileType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "cv_evaluation", schema = "cvraterai")
public class CVEvaluation extends BaseEntity {

    @Column(name = "file_name")
    private String fileName;
    
    @Column(name = "file_path")
    private String filePath;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "file_type")
    private FileType fileType;
    
    @Column(name = "github_url")
    private String githubUrl;
    
    @Lob
    @Column(name = "job_requirements", columnDefinition = "TEXT")
    private String jobRequirements;
    
    @Column(name = "evaluation_score")
    private Integer evaluationScore;
    
    @Lob
    @Column(name = "evaluation_result", columnDefinition = "TEXT")
    private String evaluationResult;
    
    @Column(name = "evaluation_date")
    private Date evaluationDate;
    
    @Column(name = "full_name")
    private String fullName;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getJobRequirements() {
        return jobRequirements;
    }

    public void setJobRequirements(String jobRequirements) {
        this.jobRequirements = jobRequirements;
    }
    
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
} 