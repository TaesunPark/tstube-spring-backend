package com.example.security.oauth;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.context.ActiveProfiles;

import com.example.user.repository.UserRepository;

@SpringBootTest
@ActiveProfiles("oauth")
class CustomOAuth2UserServiceIntegrationTest {

	@Autowired
	private CustomOAuth2UserService customOAuth2UserService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ClientRegistrationRepository clientRegistrationRepository;

	@Test
	void 실제_카카오_로그인_통합테스트() {
		// given
		ClientRegistration clientRegistration = clientRegistrationRepository.findByRegistrationId("kakao");

		OAuth2UserRequest userRequest = new OAuth2UserRequest(
			clientRegistration,
			new OAuth2AccessToken(
				OAuth2AccessToken.TokenType.BEARER,
				"cjUZMqt6vAfhwGzta3wZkvBFywjfPKroAAAAAQoqJU4AAAGVdYi-2iHmgQBvj-MV",
				null,
				null
			)
		);

		// when
		OAuth2User result = customOAuth2UserService.loadUser(userRequest);

		// then
		assertNotNull(result);
		assertNotNull(result.getAttribute("id"));

		// DB에 실제로 저장되었는지 확인
		String providerId = result.getAttribute("id").toString();
		assertTrue(userRepository.findByProviderId(providerId).isPresent());
	}

}