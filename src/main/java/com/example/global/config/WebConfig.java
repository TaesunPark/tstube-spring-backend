package com.example.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	final FileStorageProperties fileStorageProperties;

	public WebConfig(FileStorageProperties fileStorageProperties) {
		this.fileStorageProperties = fileStorageProperties;
	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry
			.addMapping("/**")
			.allowedOrigins(
				"http://www.xn----bv7eq1qhzbe7i6wn.shop", // 허용할 도메인 1
				"http://localhost:3000"                  // 허용할 도메인 2
			)
			.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 허용 메서드
			.allowedHeaders("*")                                      // 허용 헤더
			.exposedHeaders("Authorization", "Content-Type")         // 클라이언트에 노출할 헤더
			.allowCredentials(true);                                  // 쿠키 및 인증 정보 허용
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/upload/images/**").addResourceLocations("").addResourceLocations("file:/Users/taesunpark/dev/server/video-platform/upload/images/").setCachePeriod(3600);
	}
}