package com.birkann.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.birkann.model.Credit;

@Repository
public interface CreditRepository extends JpaRepository<Credit, Long> {
    List<Credit> findByExpiredDateBefore(Date date);
}
