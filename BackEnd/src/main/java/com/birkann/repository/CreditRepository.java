package com.birkann.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.birkann.model.Credit;

@Repository
public interface CreditRepository extends JpaRepository<Credit, Long>{

}
