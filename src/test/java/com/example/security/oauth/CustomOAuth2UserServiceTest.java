package com.example.security.oauth;

import com.example.security.jwt.JwtTokenProvider;
import com.example.user.entity.User;
import com.example.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private JwtTokenProvider jwtTokenProvider;

	@Spy
	private DefaultOAuth2UserService defaultOAuth2UserService;

	@InjectMocks
	private TestCustomOAuth2UserService customOAuth2UserService;

	// 테스트용 CustomOAuth2UserService 구현
	private static class TestCustomOAuth2UserService extends CustomOAuth2UserService {
		public TestCustomOAuth2UserService(UserRepository userRepository, JwtTokenProvider jwtTokenProvider) {
			super(userRepository, jwtTokenProvider);
		}

		@Override
		protected OAuth2User loadOAuth2User(OAuth2UserRequest userRequest) {
			Map<String, Object> attributes = new HashMap<>();
			attributes.put("id", "12345");

			Map<String, Object> profile = new HashMap<>();
			profile.put("nickname", "testUser");
			profile.put("profile_image_url", "http://test.com/image.jpg");

			Map<String, Object> kakaoAccount = new HashMap<>();
			kakaoAccount.put("email", "test@test.com");
			kakaoAccount.put("profile", profile);

			attributes.put("kakao_account", kakaoAccount);

			return new DefaultOAuth2User(
				Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
				attributes,
				"id"
			);
		}
	}

	@Test
	void 카카오_신규_회원가입_성공() throws Exception {
		// given
		String providerId = "12345";
		ClientRegistration clientRegistration = ClientRegistration.withRegistrationId("kakao")
			.clientId("test-client-id")
			.clientSecret("test-client-secret")
			.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
			.authorizationUri("https://kakao.com/oauth/authorize")
			.redirectUri("http://localhost:8080/login/oauth2/code/kakao")
			.userNameAttributeName("id")
			.tokenUri("https://kakao.com/oauth/token")
			.build();

		OAuth2UserRequest userRequest = new OAuth2UserRequest(
			clientRegistration,
			new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, "test-token", null, null)
		);

		when(userRepository.findByProviderId("12345")).thenReturn(Optional.empty());

		// when
		OAuth2User result = customOAuth2UserService.loadUser(userRequest);

		// then
		assertNotNull(result);
		assertEquals("12345", result.getAttribute("id"));

		ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
		verify(userRepository).save(userCaptor.capture());

		User savedUser = userCaptor.getValue();
		assertEquals("12345", savedUser.getProviderId());
		assertEquals("testUser", savedUser.getNickname());
		assertEquals("test@test.com", savedUser.getEmail());
	}

}