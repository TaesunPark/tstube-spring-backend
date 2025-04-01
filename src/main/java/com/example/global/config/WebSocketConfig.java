package com.example.global.config;

import java.security.Principal;
import java.util.Map;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import com.example.global.config.websocket.WebSocketAuthInterceptor;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

	private final WebSocketHandler webSocketHandler;
	private final WebSocketAuthInterceptor authInterceptor;

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(webSocketHandler, "/ws/chat/**")
			.addInterceptors(authInterceptor)
			.setHandshakeHandler(new CustomHandshakeHandler())
			.setAllowedOrigins("*");
	}

	// principal을 WebSocket에 전달하기 위한 핸들러
	private static class CustomHandshakeHandler extends DefaultHandshakeHandler {
		@Override
		protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler,
			Map<String, Object> attributes) {
			// 인증 객체 생성 및 반환
			return SecurityContextHolder.getContext().getAuthentication();
		}
	}

}
