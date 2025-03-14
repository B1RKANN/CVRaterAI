package com.birkann.handler;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Exception<E> {
	
	private String path;
	
	private Date createTime;
	
	private String hostName;
	
	private E message;
	
	
}
