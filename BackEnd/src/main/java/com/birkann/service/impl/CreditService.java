package com.birkann.service.impl;

import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.birkann.dto.DtoCredit;
import com.birkann.exception.BaseException;
import com.birkann.exception.ErrorMessage;
import com.birkann.exception.MessageType;
import com.birkann.model.Credit;
import com.birkann.model.User;
import com.birkann.repository.CreditRepository;
import com.birkann.repository.UserRepository;
import com.birkann.service.ICreditService;

@Service
public class CreditService implements ICreditService {

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private CreditRepository creditRepository;
	
	@Override
	public DtoCredit reduceCredit(Long id) {
		DtoCredit dtoCredit = new DtoCredit();
		Optional<User> optUser = userRepository.findById(id);
		if (optUser.isEmpty()) {
			throw new BaseException(new ErrorMessage(MessageType.NO_RECORD_EXIST, id.toString()));
		}
		if (optUser.get().getCredit().getUserCredit()<=0) {
			throw new BaseException(new ErrorMessage(MessageType.INSUFFICIENT_CREDIT, optUser.get().getCredit().getUserCredit().toString()));
		}
		Credit newCredit = creditNew(optUser.get().getCredit());
		BeanUtils.copyProperties(newCredit, dtoCredit);
		return dtoCredit;
	}
	
	private Credit creditNew(Credit credit) {
		credit.setUserCredit(credit.getUserCredit()-1);
		return creditRepository.save(credit);
	}

}
