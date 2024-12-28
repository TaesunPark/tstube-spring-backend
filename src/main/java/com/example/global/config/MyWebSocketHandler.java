package com.example.global.config;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.example.chat.dto.ChatMessage;
import com.example.chat.dto.ChatRoom;
import com.example.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class MyWebSocketHandler extends TextWebSocketHandler {

	private final ObjectMapper objectMapper;
	private final ChatService chatService;

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		String payload = message.getPayload();
		System.out.println("Received: ".concat(payload));
		ChatMessage chatMessage = objectMapper.readValue(payload, ChatMessage.class);

		ChatRoom room = chatService.getRoom(chatMessage.getVideoId(), session);
		room.handleActions(session, chatMessage, chatService);
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		chatService.removeSessionFromAllRooms(session);
	}
}
