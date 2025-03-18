package com.birkann.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.birkann.model.CVEvaluation;
import com.birkann.model.User;

@Repository
public interface CVEvaluationRepository extends JpaRepository<CVEvaluation, Long> {
    List<CVEvaluation> findByUser(User user);
    List<CVEvaluation> findByUserOrderByEvaluationDateDesc(User user);
    
    /**
     * Kullanıcı ID'sine göre tüm CV değerlendirmelerini bulur
     * @param userId Kullanıcı ID
     * @return CV değerlendirme listesi
     */
    @Query("SELECT c FROM CVEvaluation c WHERE c.user.id = :userId ORDER BY c.evaluationDate DESC")
    List<CVEvaluation> findByUserIdOrderByEvaluationDateDesc(@Param("userId") Long userId);
} 