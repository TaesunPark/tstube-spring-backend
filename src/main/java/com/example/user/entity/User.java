package com.example.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String providerId; // 카카오에서 제공하는 고유 ID

	private String nickname; // 카카오 닉네임
	private String email; // 카카오 이메일 (선택적)
	private String profileImage; // 프로필 이미지 URL

	@Enumerated(EnumType.STRING)
	private Provider provider;

	@Builder
	public User(String providerId, String nickname, String email, String profileImage, Provider provider) {
		this.providerId = providerId;
		this.nickname = nickname;
		this.email = email;
		this.profileImage = profileImage;
		this.provider = provider;
	}

	public User update(String nickname, String profileImage) {
		this.nickname = nickname;
		this.profileImage = profileImage;
		return this;
	}
}
