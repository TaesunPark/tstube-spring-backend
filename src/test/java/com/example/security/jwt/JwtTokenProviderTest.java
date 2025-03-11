package com.example.security.jwt;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureException;

@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {

	@Mock
	private JwtProperties jwtProperties;

	@InjectMocks
	private JwtTokenProvider jwtTokenProvider;

	private final String TEST_SECRET = "thisIsTestSecretKeyForJwtTokenProviderTestAndItShouldBeLongEnough";
	private final long TEST_VALIDITY = 3600; // 1시간

	@BeforeEach
	void setUp() {
		when(jwtProperties.getSecret()).thenReturn(TEST_SECRET);
		when(jwtProperties.getTokenValidityInSeconds()).thenReturn(TEST_VALIDITY);
	}

	@Test
	@DisplayName("JWT 토큰 생성 테스트")
	void createToken_ValidProviderIdAndProvider_ReturnsToken() {
		// given
		String providerId = "12345";
		String provider = "KAKAO";
		String nickname = "taesun";

		// when
		String token = jwtTokenProvider.createToken(providerId, provider, nickname);

		// then
		assertNotNull(token);
		assertTrue(token.length() > 0);
	}

	@Test
	@DisplayName("JWT 토큰에서 providerId 추출 테스트")
	void getProviderIdFromToken_ValidToken_ReturnsProviderId() {
		// given
		String providerId = "12345";
		String provider = "KAKAO";
		String nickname = "taesun";
		String token = jwtTokenProvider.createToken(providerId, provider, nickname);

		// when
		String extractedProviderId = jwtTokenProvider.getProviderIdFromToken(token);

		// then
		assertEquals(providerId, extractedProviderId);
	}

	@Test
	@DisplayName("만료된 JWT 토큰 검증 테스트")
	void getProviderIdFromToken_ExpiredToken_ThrowsJwtException() {
		// given
		when(jwtProperties.getTokenValidityInSeconds()).thenReturn(0L); // 즉시 만료되는 토큰
		String providerId = "12345";
		String provider = "KAKAO";
		String nickname = "taesun";
		String expiredToken = jwtTokenProvider.createToken(providerId, provider, nickname);

		// when & then
		assertThrows(ExpiredJwtException.class, () -> {
			// 약간의 지연으로 토큰이 만료되도록 함
			Thread.sleep(10);
			jwtTokenProvider.getProviderIdFromToken(expiredToken);
		});
	}

	@Test
	@DisplayName("변조된 JWT 토큰 검증 테스트")
	void getProviderIdFromToken_TamperedToken_ThrowsJwtException() {
		// given
		String providerId = "12345";
		String provider = "KAKAO";
		String nickname = "taesun";
		String token = jwtTokenProvider.createToken(providerId, provider, nickname);
		String tamperedToken = token.concat("tampered");

		// when & then
		assertThrows(SignatureException.class, () -> {
			jwtTokenProvider.getProviderIdFromToken(tamperedToken);
		});
	}

	@Test
	@DisplayName("JWT 토큰에 provider 값이 포함되어 있는지 테스트")
	void createToken_ShouldIncludeProviderInClaims() {
		// given
		String providerId = "12345";
		String provider = "KAKAO";
		String nickname = "taesun";

		// when
		String token = jwtTokenProvider.createToken(providerId, provider, nickname);

		// then
		Claims claims = Jwts.parserBuilder()
			.setSigningKey(TEST_SECRET.getBytes())
			.build()
			.parseClaimsJws(token)
			.getBody();

		assertEquals(provider, claims.get("provider"));
	}
}