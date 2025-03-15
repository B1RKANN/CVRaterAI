package com.birkann.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.birkann.model.RefreshToken;
import com.birkann.model.User;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
	
	Optional<RefreshToken> findByRefreshToken(String refreshToken);
	
	Optional<RefreshToken> findByUser(User user);
	
	@Transactional
	void deleteByUser(User user);
	
	default Optional<RefreshToken> findByToken(String token) {
		return findByRefreshToken(token);
	}
}
