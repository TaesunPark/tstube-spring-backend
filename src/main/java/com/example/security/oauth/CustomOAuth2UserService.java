package com.example.security.oauth;

import java.util.Collections;
import java.util.Map;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.example.security.jwt.JwtTokenProvider;
import com.example.user.entity.Provider;
import com.example.user.entity.User;
import com.example.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

	private final UserRepository userRepository;
	private final JwtTokenProvider jwtTokenProvider;

	// 테스트를 위해 추가
	protected OAuth2User loadOAuth2User(OAuth2UserRequest userRequest) {
		return super.loadUser(userRequest);
	}

	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		OAuth2User oAuth2User = loadOAuth2User(userRequest);  // 메소드 추출

		// 현재 진행중인 서비스 구분 (kakao)
		String registrationId = userRequest.getClientRegistration().getRegistrationId();
		log.info("OAuth2 로그인 진행 중: {}", registrationId);

		// OAuth2 로그인 진행시 키가 되는 필드값 (kakao의 경우 'id')
		String userNameAttributeName = userRequest.getClientRegistration()
			.getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

		// kakao는 response를 JSON으로 내려주고, 이를 Map으로 받아옴.
		Map<String, Object> attributes = oAuth2User.getAttributes();

		// 프로바이더별 처리 분기 (현재는 카카오만 지원)
		if ("kakao".equals(registrationId)) {
			return processKakaoUser(attributes, userNameAttributeName);
		}

		// 지원하지 않는 프로바이더인 경우 예외 처리
		throw new OAuth2AuthenticationException(String.format("Unsupported provider: {s}", registrationId));
	}

	private OAuth2User processKakaoUser(Map<String, Object> attributes, String userNameAttributeName) {
		try {
			Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
			Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

			String providerId = String.valueOf(attributes.get("id"));
			String nickname = (String) profile.get("nickname");
			String email = (String) kakaoAccount.get("email");
			String profileImageUrl = (String) profile.get("profile_image_url");

			User user = userRepository.findByProviderId(providerId)
				.map(entity -> entity.update(nickname, profileImageUrl))
				.orElse(User.builder()
					.providerId(providerId)
					.nickname(nickname)
					.email(email)
					.profileImage(profileImageUrl)
					.provider(Provider.KAKAO)
					.build());

			userRepository.save(user);

			return new DefaultOAuth2User(
				Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
				attributes,
				userNameAttributeName
			);
		} catch (Exception e) {
			log.error("카카오 사용자 정보 처리 중 오류 발생", e);
			throw new OAuth2AuthenticationException(String.format("Failed to process Kakao user: {s}", e.getMessage()));
		}
	}
}