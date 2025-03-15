package com.birkann.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.birkann.model.CVEvaluation;
import com.birkann.model.User;

@Repository
public interface CVEvaluationRepository extends JpaRepository<CVEvaluation, Long> {
    List<CVEvaluation> findByUser(User user);
    List<CVEvaluation> findByUserOrderByEvaluationDateDesc(User user);
} 