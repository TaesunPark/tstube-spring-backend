package com.example.chat.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "chat_messages", indexes = {
	@Index(name = "idx_chat_video_id", columnList = "videoId"),
	@Index(name = "idx_chat_user_id", columnList = "userId"),
	@Index(name = "idx_chat_timestamp", columnList = "timestamp")
})
public class ChatMessage {
	public enum MessageType {
		ENTER, TALK, LEAVE
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private MessageType type; // 메시지 타입

	private String videoId;

	private String roomId; // 방번호

	private String sender; // 메시지 보낸 사람

	private String message; // 메시지

	private LocalDateTime timestamp;

	private String userId;
}
