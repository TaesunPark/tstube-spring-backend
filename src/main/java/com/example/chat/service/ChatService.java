package com.example.chat.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.example.chat.component.ChatRoomManager;
import com.example.chat.dto.ChatRoom;
import com.example.chat.dto.UserInfo;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@EnableScheduling
public class ChatService {

	private final ChatRoomManager chatRoomManager;
	private final ObjectMapper objectMapper;

	// 방별 접속자 정보 관리 맵 추가
	private final Map<String, Map<String, UserInfo>> roomParticipants = new ConcurrentHashMap<>();

	// 마지막 활동 시간 기록 추가
	private final Map<String, Long> lastActivityTimes = new ConcurrentHashMap<>();

	public ChatRoom getRoom(String roomId, WebSocketSession session) {
		return chatRoomManager.getChatRoom(roomId);
	}

	public void leaveRoom(String roomId, WebSocketSession session) {
		ChatRoom chatRoom = chatRoomManager.getChatRoom(roomId);
		chatRoom.removeSession(session);

		if (chatRoom.getSessions().isEmpty()) {
			chatRoomManager.removeChatRoom(roomId);
		}
	}

	// 접속자 추가
	public void addParticipant(String roomId, WebSocketSession session) {
		// 세션 속성에서 정보 추출
		String userId = (String) session.getAttributes().get("USER_ID");
		String userName = (String) session.getAttributes().get("USER_NAME");
		Boolean isGuest = (Boolean) session.getAttributes().get("IS_GUEST");
		String ipAddress = (String) session.getAttributes().get("IP_ADDRESS");

		Map<String, UserInfo> participants = roomParticipants
			.computeIfAbsent(roomId, k -> new ConcurrentHashMap<>());

		// 참가자 정보 추가
		participants.put(userId, new UserInfo(userId, userName, isGuest, ipAddress));

		lastActivityTimes.put(session.getId(), System.currentTimeMillis());

	}

	public void removeSessionFromAllRooms(WebSocketSession session) {
		chatRoomManager.getAllChatRooms().values().forEach(room -> room.removeSession(session));
	}

	public <T> void sendMessage(WebSocketSession session, T message) {
		try {
			session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
		} catch (Exception e){
			System.out.println(e.getMessage());
		}
	}

	// 특정 채팅방의 접속 중인 사용자 목록 조회
	// WebSocket 세션에서 사용자 정보를 추출하여 반환
	public Map<String, String> getOnlineUsers(String videoId) {
		ChatRoom chatRoom = chatRoomManager.getChatRoom(videoId);
		Set<WebSocketSession> sessions = chatRoom.getSessions();

		Map<String, String> users = new HashMap<>();

		for (WebSocketSession session : sessions) {
			String userId = (String) session.getAttributes().get("USER_ID");
			String userName = (String) session.getAttributes().get("USER_NAME");

			if (userId != null) {
				users.put(userId, userName + ":" + userName);
			}
		}

		log.debug("Online users for room {}: {}", videoId, users);
		return users;
	}

	// 세션 활동 갱신 (메시지 수신, 하트비트 등에서 호출)
	public void updateSessionActivity(WebSocketSession session) {
		lastActivityTimes.put(session.getId(), System.currentTimeMillis());
	}

	@Scheduled(fixedRate = 60000)
	public void cleanupInactiveSessions() {
		long currentTime = System.currentTimeMillis();
		long timeoutInMillis = 5 * 60 * 1000;

		// 방별 세션 검사
		for (ChatRoom room : chatRoomManager.getAllChatRooms().values()) {
			for (WebSocketSession session : new ArrayList<>(room.getSessions())) {
				Long lastActivity = lastActivityTimes.get(session.getId());

				// 마지막 활동 기록이 없거나 타임아웃 초과
				if (lastActivity == null || (currentTime - lastActivity > timeoutInMillis)) {
					// 세션 정보 추출 및 참가자 제거
					String userId = (String) session.getAttributes().get("USER_ID");
					removeParticipant(room.getRoomId(), userId);

					room.removeSession(session);
					lastActivityTimes.remove(session.getId());

					try {
						if (session.isOpen()) {
							session.close();
						}
					} catch (IOException e) {
						log.error("Error closing inactive session", e);
					}
				}
			}
		}
	}

	public void removeParticipant(String roomId, String userId) {
		Map<String, UserInfo> participants = roomParticipants.get(roomId);
		if (participants != null) {
			participants.remove(userId);
		}
	}

}
