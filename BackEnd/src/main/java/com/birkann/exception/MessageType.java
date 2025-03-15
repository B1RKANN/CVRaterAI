package com.birkann.exception;

import lombok.Getter;

@Getter
public enum MessageType {
	
	NO_RECORD_EXIST("1004","Kayıt Bulunamadı"),
	TOKEN_IS_EXPIRED("1005","Tokenin Süresi Bitmiştir"),
	USERNAME_NOT_FOUND("1006","Username Bulunamadı"),
	USERNAME_OR_PASSWORD_INVALID("1007","Kullanıcı Adı Veya Şifre Hatalı"),
	REFRESH_TOKEN_NOT_FOUND("1008","Refresh Token Bulunamadı"),
	REFRESH_TOKEN_IS_EXPIRED("1009","Refresh Tokenın Süresi Bitmiştir"),
	INSUFFICIENT_CREDIT("1010","Yetersiz Kredi"),
	USER_NOT_FOUND("1011","Kullanıcı Bulunamadı"),
	INVALID_FILE_TYPE("1012","Geçersiz Dosya Türü"),
	FILE_PROCESSING_ERROR("1013","Dosya İşleme Hatası"),
	EVALUATION_FAILED("1014","Değerlendirme Başarısız"),
	EVALUATION_NOT_FOUND("1015","Değerlendirme Kaydı Bulunamadı"),
	GENERAL_EXCEPTION("9999","Genel Bir Hata Oluştu");
	
	private String code;
	
	private String message;
	
	private MessageType(String code,String message) {
		this.code = code;
		this.message = message;
	}
	
}
