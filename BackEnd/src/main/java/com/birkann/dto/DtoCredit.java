package com.birkann.dto;

import com.birkann.enums.PlanType;

import lombok.Data;

@Data
public class DtoCredit extends DtoBase{
	
	private Integer userCredit;
	
	private PlanType planType;
	
}
