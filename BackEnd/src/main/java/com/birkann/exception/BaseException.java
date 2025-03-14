package com.birkann.exception;

public class BaseException extends RuntimeException{
	public BaseException(ErrorMessage errorMessage) {
		super(errorMessage.prepareErrorMessage());
	}
}
