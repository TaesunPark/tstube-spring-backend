package com.example.chat.dto;

import lombok.Data;

@Data
public class ChatMessage {
	public enum MessageType {
		ENTER, TALK
	}
	private MessageType type; // 메시지 타입
	private String videoId;
	private String roomId; // 방번호
	private String sender; // 메시지 보낸 사람
	private String message; // 메시지
}
