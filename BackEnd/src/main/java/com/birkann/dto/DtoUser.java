package com.birkann.dto;

import lombok.Data;

@Data
public class DtoUser extends DtoBase{
	
	private String name;
	
	private String email;
	
	private String password;
	
	
}
