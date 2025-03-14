package com.birkann.controller;

import com.birkann.dto.DtoCredit;

public interface IRestCreditController {
	
	RootEntity<DtoCredit> reduceCredit(Long id);
	
}
