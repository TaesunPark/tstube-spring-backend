package com.example.security.oauth;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {
	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
		AuthenticationException exception) throws IOException, ServletException {
		String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:3000/login")
				.queryParam("error", exception.getMessage())
				.build().toUriString();

		response.sendRedirect(targetUrl);
	}
	// OAuth2 로그인 실패 시 처리를 담당하는 핸들러입니다.
}
