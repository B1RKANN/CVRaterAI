package com.birkann.controller.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import com.birkann.controller.IRestCreditController;
import com.birkann.controller.RestBaseController;
import com.birkann.controller.RootEntity;
import com.birkann.dto.DtoCredit;
import com.birkann.service.ICreditService;

import jakarta.validation.constraints.NotEmpty;

@RestController
@RequestMapping("/rest/api/credit")
public class RestCreditController extends RestBaseController implements IRestCreditController {

	@Autowired
	private ICreditService creditService;
	
	@Override
	@PostMapping("/{id}")
	public RootEntity<DtoCredit> reduceCredit(@PathVariable(name = "id") Long id) {
		return ok(creditService.reduceCredit(id));
	}

}
