package com.example.security.jwt;

import java.security.Key;
import java.util.Date;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

	private final JwtProperties jwtProperties;

	// JWT 서명에 사용할 키를 생성하는 메서드
	private Key getSigningKey() {
		byte[] keyBytes = jwtProperties.getSecret().getBytes();
		return Keys.hmacShaKeyFor(keyBytes);
	}

	// JWT 토큰을 생성하는 메서드
	public String createToken(String providerId, String provider, String nickname) {
		Claims claims = Jwts.claims().setSubject(providerId);
		claims.put("provider", provider);
		claims.put("nickname", nickname);

		Date now = new Date();
		// 현재 시간 + 유효시간으로 만료시간 설정
		Date validity = new Date(now.getTime() + jwtProperties.getTokenValidityInSeconds() * 10000);

		return Jwts.builder()
				.setClaims(claims)
				.setIssuedAt(now)		// 토큰 발행 시간
				.setExpiration(validity)	// 토큰 만료 시간
				.signWith(getSigningKey(), SignatureAlgorithm.HS512)
				.compact();
	}

	// 토큰에서 providerId 추출
	public String getProviderIdFromToken(String token) {
		Claims claims = Jwts.parserBuilder()
			.setSigningKey(getSigningKey())
			.build()
			.parseClaimsJws(token)
			.getBody();

		return claims.getSubject();
	}

}
