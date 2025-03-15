package com.birkann.model;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



@Entity
@Table(name = "refresh_token")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken extends BaseEntity {
	
	@Column(name = "refresh_token")
	private String refreshToken;
	
	@Column(name = "create_time")
	@DateTimeFormat(iso = ISO.DATE_TIME)
	private Date createTime;
	
	@Column(name = "expired_time")
	private Date expiredTime;
	
	@ManyToOne
	private User user;
	
	/**
	 * Token değerini döndürür
	 */
	public String getToken() {
		return refreshToken;
	}
	
	/**
	 * Token değerini ayarlar
	 */
	public void setToken(String token) {
		this.refreshToken = token;
	}
	
	/**
	 * Sona erme tarihini döndürür
	 */
	public Date getExpiryDate() {
		return expiredTime;
	}
	
	/**
	 * Sona erme tarihini ayarlar
	 */
	public void setExpiryDate(Date date) {
		this.expiredTime = date;
	}
}
