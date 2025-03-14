package com.birkann.controller;

import lombok.Data;

@Data
public class RootEntity<T> {
	
	private Integer status;
	
	private T payload;
	
	private String errorMessage;
	
	public static <T> RootEntity<T> ok(T payload){
		RootEntity<T> rootEntity  = new RootEntity<>();
		rootEntity.setStatus(200);
		rootEntity.setPayload(payload);
		rootEntity.setErrorMessage(null);
		return rootEntity;
	}
	
	public static <T> RootEntity<T> error(String errorMessage){
		RootEntity<T> rootEntity  = new RootEntity<>();
		rootEntity.setStatus(500);
		rootEntity.setPayload(null);
		rootEntity.setErrorMessage(errorMessage);
		return rootEntity;
	}
}
