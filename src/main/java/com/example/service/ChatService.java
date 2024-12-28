package com.example.service;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.example.chat.component.ChatRoomManager;
import com.example.chat.dto.ChatRoom;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatService {

	private final ChatRoomManager chatRoomManager;
	private final ObjectMapper objectMapper;

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

}
