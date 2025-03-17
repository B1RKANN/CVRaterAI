package com.birkann.handler;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;

import com.birkann.exception.BaseException;

@ControllerAdvice
public class GlobalExceptionHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
	
	@ExceptionHandler(value = {BaseException.class})
	public ResponseEntity<ErrorResponse> handlerBaseException(BaseException ex, WebRequest request) {
		ErrorResponse errorResponse = new ErrorResponse();
		errorResponse.setTimestamp(new Date());
		errorResponse.setStatus(HttpStatus.BAD_REQUEST.value());
		errorResponse.setError("Bad Request");
		errorResponse.setMessage(ex.getMessage());
		errorResponse.setPath(request.getDescription(false).substring(4));
		return ResponseEntity.badRequest().body(errorResponse);
	}
	
	@ExceptionHandler(value = {MethodArgumentNotValidException.class})
	public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, WebRequest request) {
		Map<String, List<String>> validationErrors = new HashMap<>();
		
		for (ObjectError objError : ex.getBindingResult().getAllErrors()) {
			String fieldName = ((FieldError)objError).getField();
			validationErrors.computeIfAbsent(fieldName, k -> new ArrayList<>())
							.add(objError.getDefaultMessage());
		}
		
		ErrorResponse errorResponse = new ErrorResponse();
		errorResponse.setTimestamp(new Date());
		errorResponse.setStatus(HttpStatus.BAD_REQUEST.value());
		errorResponse.setError("Validation Error");
		errorResponse.setMessage("Validation failed");
		errorResponse.setPath(request.getDescription(false).substring(4));
		errorResponse.setValidationErrors(validationErrors);
		
		return ResponseEntity.badRequest().body(errorResponse);
	}
	
	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException e, WebRequest request) {
		logger.error("Kimlik doğrulama hatası: ", e);
		
		ErrorResponse errorResponse = new ErrorResponse();
		errorResponse.setTimestamp(new Date());
		errorResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
		errorResponse.setError("Kimlik doğrulama başarısız");
		errorResponse.setMessage(e.getMessage());
		errorResponse.setPath(request.getDescription(false).substring(4));
		
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
	}

	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException e, WebRequest request) {
		logger.error("Hatalı kimlik bilgileri: ", e);
		
		ErrorResponse errorResponse = new ErrorResponse();
		errorResponse.setTimestamp(new Date());
		errorResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
		errorResponse.setError("Hatalı email veya şifre");
		errorResponse.setMessage("Lütfen bilgilerinizi kontrol edip tekrar deneyin");
		errorResponse.setPath(request.getDescription(false).substring(4));
		
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
	}

	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException e, WebRequest request) {
		logger.error("Runtime hatası: ", e);
		
		ErrorResponse errorResponse = new ErrorResponse();
		errorResponse.setTimestamp(new Date());
		errorResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		errorResponse.setError("İşlem başarısız");
		errorResponse.setMessage(e.getMessage());
		errorResponse.setPath(request.getDescription(false).substring(4));
		
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
	}

	@ExceptionHandler(java.lang.Exception.class)
	public ResponseEntity<ErrorResponse> handleException(java.lang.Exception e, WebRequest request) {
		logger.error("Beklenmeyen hata: ", e);
		
		ErrorResponse errorResponse = new ErrorResponse();
		errorResponse.setTimestamp(new Date());
		errorResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		errorResponse.setError("Sunucu hatası");
		errorResponse.setMessage("Beklenmeyen bir hata oluştu, lütfen daha sonra tekrar deneyin");
		errorResponse.setPath(request.getDescription(false).substring(4));
		
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
	}
}
