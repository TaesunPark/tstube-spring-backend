package com.example.chat.dto;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.springframework.web.socket.WebSocketSession;

import com.example.chat.entity.ChatMessage;
import com.example.chat.service.ChatService;

public class ChatRoom {
	private final String roomId;
	private final Set<WebSocketSession> sessions = new CopyOnWriteArraySet<>();

	public ChatRoom(String roomId){
		this.roomId = roomId;
	}

	public String getRoomId() {
		return roomId;
	}

	public Set<WebSocketSession> getSessions() {
		return sessions;
	}

	public void handleActions(WebSocketSession session, ChatMessage chatMessage, ChatService chatService) {
		if (chatMessage.getType().equals(ChatMessage.MessageType.ENTER)) {
			sessions.add(session);
			chatMessage.setMessage(chatMessage.getSender().concat("님이 입장했습니다."));
		}
		sendMessage(chatMessage, chatService);
	}

	public <T> void sendMessage(T message, ChatService chatService) {
		sessions.parallelStream().forEach(session -> chatService.sendMessage(session, message));
	}

	public void removeSession(WebSocketSession session) {
		sessions.remove(session);
	}

}