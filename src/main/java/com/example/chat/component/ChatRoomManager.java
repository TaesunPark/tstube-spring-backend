package com.example.chat.component;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import com.example.chat.dto.ChatRoom;

import lombok.Getter;

@Component
public class ChatRoomManager {
	private final Map<String, ChatRoom> chatRooms = new ConcurrentHashMap<>();

	public ChatRoom getChatRoom(String roomId) {
		return chatRooms.computeIfAbsent(roomId, ChatRoom::new);
	}

	public void removeChatRoom(String roomId) {
		chatRooms.remove(roomId);
	}

	public Map<String, ChatRoom> getAllChatRooms() {
		return chatRooms;
	}
}
