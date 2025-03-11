package com.example.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.security.jwt.JwtAuthenticationFilter;
import com.example.security.oauth.CustomOAuth2UserService;
import com.example.security.oauth.OAuth2AuthenticationFailureHandler;
import com.example.security.oauth.OAuth2SuccessHandler;

import lombok.RequiredArgsConstructor;

// 스프링의 설정 클래스임을 나타내는 어노테이션
@Configuration
// 스프링 시큐리티 설정을 활성화하는 어노테이션
@EnableWebSecurity
// final 필드에 생성자를 자동으로 만들어주는 롬복 어노테이션
@RequiredArgsConstructor
public class SecurityConfig {

	// OAuth2 사용자 정보를 처리하는 서비스 주입
	private final CustomOAuth2UserService customOAuth2UserService;
	// 인증 성공 핸들러 주입
	private final OAuth2SuccessHandler oAuth2SuccessHandler;
	// 인증 실패 핸들러 주입
	private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;
	private final JwtAuthenticationFilter jwtAuthenticationFilter;


	// 스프링 시큐리티의 필터 체인을 구성하는 메서드
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			.cors(cors -> cors.configure(http))
			// CSRF (Cross-Site Request Forgery)  보안 기능 비활성화
			.csrf(AbstractHttpConfigurer::disable)
			// 세션을 생성하지 않고 상태를 저장하지 않음 (JWT 사용함)
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(auth -> auth
				// .requestMatchers("/api/auth/**", "/h2-console/**").permitAll()
				.anyRequest().permitAll()
			)
		.oauth2Login(oAuth2Login ->
			oAuth2Login.userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
				.successHandler(oAuth2SuccessHandler)
				.failureHandler(oAuth2AuthenticationFailureHandler)
				.authorizationEndpoint(endpoint -> endpoint.baseUri("/auth/login"))
		)

		.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

}
