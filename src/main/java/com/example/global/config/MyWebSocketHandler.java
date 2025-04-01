package com.example.global.config;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.example.chat.entity.ChatMessage;
import com.example.chat.dto.ChatRoom;
import com.example.chat.service.ChatMessageBatchProcessor;
import com.example.chat.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class MyWebSocketHandler extends TextWebSocketHandler {

	private final ObjectMapper objectMapper;
	private final ChatService chatService;
	private final ChatMessageBatchProcessor chatMessageBatchProcessor;

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		String payload = message.getPayload();
		log.debug("Received message: {}", payload);
		ChatMessage chatMessage = objectMapper.readValue(payload, ChatMessage.class);

		// 세션 활동 시간 갱신
		chatService.updateSessionActivity(session);

		// 세션 속성에서 사용자 정보 가져오기
		String userId = (String) session.getAttributes().get("USER_ID");
		String userName = (String) session.getAttributes().get("USER_NAME");

		// 사용자 정보를 메시지에 설정
		if (userId != null) {
			chatMessage.setUserId(userId);
		}

		if (chatMessage.getSender() == null || chatMessage.getSender().isEmpty()) {
			chatMessage.setSender(userName);
		}

		// 타임 스탬프 설정
		chatMessage.setTimestamp(LocalDateTime.now());

		// videoId와 roomId 일치시키기
		String roomId = chatMessage.getVideoId();
		chatMessage.setRoomId(roomId);

		ChatRoom room = chatService.getRoom(roomId, session);
		room.handleActions(session, chatMessage, chatService);

		// TALK 타입 메시지만 저장
		if (chatMessage.getType() == ChatMessage.MessageType.TALK) {
			ChatMessage messageToSave = new ChatMessage();
			messageToSave.setType(chatMessage.getType());
			messageToSave.setRoomId(chatMessage.getRoomId());
			messageToSave.setVideoId(chatMessage.getVideoId());
			messageToSave.setSender(chatMessage.getSender());
			messageToSave.setMessage(chatMessage.getMessage());
			messageToSave.setTimestamp(chatMessage.getTimestamp());
			messageToSave.setUserId(chatMessage.getUserId());

			chatMessageBatchProcessor.addMessage(messageToSave);
		}

		room.handleActions(session, chatMessage, chatService);
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		// 세션이 처음 연결될 때 실행
		log.debug("New WebSocket connection established: {}", session.getId());

		// 세션 속성에서 채팅방 ID 가져오기
		String roomId = (String) session.getAttributes().get("ROOM_ID");
		if (roomId == null) {
			// ROOM_ID가 없으면 videoId를 사용
			roomId = (String) session.getAttributes().get("VIDEO_ID");
		}

		if (roomId != null) {
			// 채팅방에 참가자 추가
			chatService.addParticipant(roomId, session);

			// 입장 메시지 생성 및 전송
			ChatMessage enterMessage = new ChatMessage();
			enterMessage.setType(ChatMessage.MessageType.ENTER);
			enterMessage.setRoomId(roomId);
			enterMessage.setVideoId(roomId);
			enterMessage.setSender((String) session.getAttributes().get("USER_NAME"));

			// 채팅방 가져오기 및 입장 메시지 처리
			ChatRoom room = chatService.getRoom(roomId, session);
			if (room != null) {
				room.handleActions(session, enterMessage, chatService);
			}
		}
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		log.debug("WebSocket connection closed: {}, status: {}", session.getId(), status);

		// 세션 속성에서 채팅방 ID 가져오기
		String roomId = (String) session.getAttributes().get("ROOM_ID");
		String userId = (String) session.getAttributes().get("USER_ID");

		if (roomId != null) {
			// 채팅방에서 사용자 제거
			chatService.removeParticipant(roomId, userId);

			// 퇴장 메시지 생성 및 전송
			ChatMessage leaveMessage = new ChatMessage();
			leaveMessage.setType(ChatMessage.MessageType.LEAVE);
			leaveMessage.setRoomId(roomId);
			leaveMessage.setVideoId(roomId);
			leaveMessage.setSender((String) session.getAttributes().get("USER_NAME"));

			// 채팅방 가져오기 및 퇴장 처리
			ChatRoom room = chatService.getRoom(roomId, session);
			if (room != null) {
				room.handleActions(session, leaveMessage, chatService);

				// 채팅방에서 세션 제거
				chatService.leaveRoom(roomId, session);
			}
		}

		// 모든 채팅방에서 세션 제거 (이미 있는 코드)
		chatService.removeSessionFromAllRooms(session);
	}

}
