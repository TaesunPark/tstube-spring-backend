package com.example.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "file")
public class FileStorageProperties {
	private String uploadDir;
	private String uploadImageDir;

	public String getUploadDir(){
		return uploadDir;
	}

	public String getUploadImageDir(){
		return uploadImageDir;
	}

	public void setUploadDir(String uploadDir){
		this.uploadDir = uploadDir;
	}

	public void setUploadImageDir(String uploadImageDir){
		this.uploadImageDir = uploadImageDir;
	}
}
