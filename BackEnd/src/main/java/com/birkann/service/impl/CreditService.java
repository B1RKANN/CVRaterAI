package com.birkann.service.impl;

import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private static final Logger logger = LoggerFactory.getLogger(CreditService.class);

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
		
		Credit credit = optUser.get().getCredit();
		logger.info("Kredi azaltılıyor: UserID={}, CurrentCredit={}", id, credit.getUserCredit());
		
		if (credit.getUserCredit() <= 0) {
			throw new BaseException(new ErrorMessage(MessageType.INSUFFICIENT_CREDIT, credit.getUserCredit().toString()));
		}
		
		Credit newCredit = creditNew(credit);
		logger.info("Kredi azaltıldı: UserID={}, NewCredit={}", id, newCredit.getUserCredit());
		
		BeanUtils.copyProperties(newCredit, dtoCredit);
		return dtoCredit;
	}
	
	private Credit creditNew(Credit credit) {
		credit.setUserCredit(credit.getUserCredit()-1);
		Credit savedCredit = creditRepository.save(credit);
		logger.info("Kredi güncellendi: ID={}, UserCredit={}", savedCredit.getId(), savedCredit.getUserCredit());
		return savedCredit;
	}

}
