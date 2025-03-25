package com.example.global.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.video.service.video.UploadVideoService;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class ApplicationStartupConfig {

	private final UploadVideoService uploadVideoService;

	@Bean
	public CommandLineRunner initializeApp() {
		return args -> {
			// 임시 디렉토리 초기화
			uploadVideoService.initTempDirectory();
		};
	}
}
