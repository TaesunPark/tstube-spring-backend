package com.example.global.config.websocket;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.example.user.entity.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements HandshakeInterceptor {
	@Override
	public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
		Map<String, Object> attributes) throws Exception {

		String path = request.getURI().getPath();
		String[] pathSegments = path.split("/");
		if (pathSegments.length > 3) {
			String videoId = pathSegments[3];
			attributes.put("ROOM_ID", videoId);
			attributes.put("VIDEO_ID", videoId);
			log.info("WebSocket connection for video/room ID: {}", videoId);
		}

		// 현재 인증된 사용자 정보 가져오기
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		if (auth != null && auth.isAuthenticated() && !("anonymousUser".equals(auth.getPrincipal()))) {
			// 인증된 사용자 정보를 WebSocket 세션에 저장
			// UserDetails에서 추가 정보 추출
			User user = (User) auth.getPrincipal();
			attributes.put("USER_ID", user.getEmail());
			attributes.put("USER_NAME", user.getNickname());
			attributes.put("IS_GUEST", false);
			attributes.put("IP_ADDRESS", null);
			log.info("Authenticated user connectd: {}", auth.getName());
			log.info("Authenticated user connected: {}", user.getNickname());
			return true;
		} else {
			// 비로그인 사용자 - IP + 브라우저 정보 조합으로 식별
			String clientIp = extractIpAddress(request);
			String userAgent = request.getHeaders().getFirst("User-Agent");
			String acceptLanguage = request.getHeaders().getFirst("Accept-Language");

			// 고유 식별자 생성
			String visitorId = generateVisitorId(clientIp, userAgent, acceptLanguage);
			// 비인증 사용자 처리 (필요에 따라 거부 가능)
			attributes.put("USER_ID", "guest-" + visitorId);
			attributes.put("USER_NAME", "익명-" + visitorId.substring(0, 6));
			attributes.put("IS_GUEST", true);
			attributes.put("IP_ADDRESS", clientIp);
			return true; // 연결 허용
		}
	}

	private String extractIpAddress(ServerHttpRequest request) {
		// X-Forwarded-For 헤더 확인
		List<String> forwardedHeaders = request.getHeaders().get("X-Forwarded-For");
		if (forwardedHeaders != null && !forwardedHeaders.isEmpty()) {
			return forwardedHeaders.get(0).split(",")[0].trim();
		}

		// 일반 IP 주소

		InetSocketAddress remoteAddress = request.getRemoteAddress();
		if (remoteAddress != null) {
			InetAddress address = remoteAddress.getAddress();
			if (address != null) {
				return address.getHostAddress();
			}
		}

		return "unknown";
	}

	private String generateVisitorId(String clientIp, String userAgent, String acceptLanguage) {
		// 요소 조합
		String combined = clientIp + "|" + (userAgent != null ? userAgent : "") + "|" +
							(acceptLanguage != null ? acceptLanguage : "");

		// 	해시 생성 (MessageDigest 사용)
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(combined.getBytes());

			// 바이트를 16진수 문자열로 변환
			StringBuilder hexString = new StringBuilder();
			for (byte b : hash) {
				String hex = Integer.toHexString(0xff & b);
				if (hex.length() == 1) hexString.append('0');
				hexString.append(hex);
			}
			return hexString.toString();
		} catch (NoSuchAlgorithmException e) {
			return String.valueOf(combined.hashCode());
		}

	}

	@Override
	public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
		Exception exception) {

	}
}
