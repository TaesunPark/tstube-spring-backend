package com.example.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserInfoResponse {
	private Long id;
	private String nickname;
	private String email;
	private String profileImage;
	private String provider;
}
