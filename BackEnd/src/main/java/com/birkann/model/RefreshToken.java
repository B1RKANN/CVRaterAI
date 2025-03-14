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
	
}
