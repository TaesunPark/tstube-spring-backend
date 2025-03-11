package com.example.security.oauth;

import java.io.IOException;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.security.jwt.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	private final JwtTokenProvider jwtTokenProvider;
	private final ObjectMapper objectMapper;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException {
		// 인증된 사용자 정보를 OAuth2User로 변환
		OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
		// 사용자의 고유 식별자 추출
		String providerId = String.valueOf(oAuth2User.getAttributes().get("id"));

		String nickname = setNicknameFromKakao(oAuth2User);

		// JWT 토큰 생성
		String token = jwtTokenProvider.createToken(providerId, "KAKAO", nickname);

		// 프론트엔드 리다이렉트 URL 생성 및 토큰을 쿼리 파라미터로 전달
		String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:3000/oauth/callback").queryParam("token", token).build().toUriString();

		// HTTP 응답 헤더에 JWT 토큰 추가
		response.setHeader("Authorization", String.format("Bearer {s}", token));

		// JWT 토큰을 저장할 쿠키 생성
		Cookie cookie = new Cookie("auth_token", token);

		// 모든 경로에서 쿠키에 접근할 수 있도록 설정
		cookie.setPath("/");
		// Javascript에서 쿠키에 접근할 수 없도록 설정 XSS 공격 방지
		cookie.setHttpOnly(true);
		cookie.setMaxAge(24 * 60 * 60);
		response.addCookie(cookie);

		// 사용자를 프론트엔드의 콜백 URL로 리다이렉트합니다.
		getRedirectStrategy().sendRedirect(request, response, targetUrl);

	}

	public String setNicknameFromKakao(OAuth2User oAuth2User) {
		String nickname = null;
		Map<String, Object> attributes = oAuth2User.getAttributes();
		if (attributes.containsKey("properties")) {
			Map<String, Object> properties = (Map<String, Object>)attributes.get("properties");
			nickname = (String)properties.get("nickname");
		} else if (attributes.containsKey("kakao_account")) {
			Map<String, Object> kakaoAccount = (Map<String, Object>)attributes.get("kakao_account");
			if (kakaoAccount.containsKey("profile")) {
				Map<String, Object> profile = (Map<String, Object>)kakaoAccount.get("profile");
				nickname = (String)profile.get("nickname");
			}
		}
		return nickname;
	}
}
