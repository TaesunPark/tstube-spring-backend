package com.example.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserInfo {
	private String userId;
	private String userName;
	private boolean isGuest;
	// 게스트인 경우에만
	private String ipAddress;
}
