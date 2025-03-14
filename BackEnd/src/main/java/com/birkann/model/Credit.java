package com.birkann.model;

import com.birkann.enums.PlanType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Entity
@Table(name = "credit", schema = "cvraterai")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Credit extends BaseEntity {
	
	@Column(name = "user_credit")
	private Integer userCredit;
	
	@Column(name = "plan_type")
	@Enumerated(EnumType.STRING)
	private PlanType planType;

	@Column(name = "start_date")
	private Date startDate;

	@Column(name = "expired_date")
	private Date expiredDate;
}
