package com.example.user.contoller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.user.dto.UserInfoResponse;
import com.example.user.entity.User;
import com.example.video.response.ApiResponse;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

	@GetMapping("/me")
	public ApiResponse<UserInfoResponse> getCurrentUser(@AuthenticationPrincipal User user) {
		UserInfoResponse userInfo = UserInfoResponse.builder()
				.id(user.getId())
				.nickname(user.getNickname())
				.email(user.getEmail())
				.profileImage(user.getProfileImage())
				.provider(user.getProvider().name())
				.build();
		return new ApiResponse<>(true, "유저 정보 검색 성공", userInfo);
	}

	@PostMapping("/logout")
	public ApiResponse<Void> logout(HttpServletResponse response) {
		// 인증 쿠키 제거
		Cookie cookie = new Cookie("auth_token", null);
		cookie.setPath("/");
		cookie.setHttpOnly(true);
		cookie.setMaxAge(0);
		response.addCookie(cookie);

		return new ApiResponse<>(true, "로그아웃 성공", null);
	}



}
