package com.example.chat.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.chat.entity.ChatMessage;
import com.example.chat.repository.ChatMessageRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@EnableScheduling
public class ChatMessageBatchProcessor {

	private final ChatMessageRepository chatMessageRepository;
	private final List<ChatMessage> messageBuffer = new ArrayList<>();
	private static final int BATCH_SIZE = 100; // 한 번에 저장할 최대 메시지 수
	private final Object lock = new ReentrantLock(); // 동시성 제어를 위한 락


	public void addMessage(ChatMessage chatMessage) {
		try {
			synchronized (lock) {
				messageBuffer.add(chatMessage);

				// 버퍼가 일정 크기 이상이면 바로 저장
				if (messageBuffer.size() >= BATCH_SIZE) {
					saveMessages();
				}
			}
		} catch (Exception e) {
			log.error("Error adding message to buffer", e);
		}
	}

	@Scheduled(fixedRate = 5000)
	public void processBatch() {
		try {
			synchronized (lock) {
				if (!messageBuffer.isEmpty()) {
					saveMessages();
				}
			}
		} catch (Exception e) {
			log.error("Error processing message batch", e);
		}
	}

	private void saveMessages() {
		List<ChatMessage> messagesToSave = new ArrayList<>(messageBuffer);
		messageBuffer.clear();

		if (!messagesToSave.isEmpty()) {
			try {
				chatMessageRepository.saveAll(messagesToSave);
				log.debug("Saved batch of {} messages", messagesToSave.size());
			} catch (Exception e) {
				log.error("Failed to save message batch", e);
			}
		}
	}

	public List<ChatMessage> getPendingMessages(String roomId) {
		synchronized (lock) {
			return messageBuffer.stream()
				.filter(msg -> msg.getRoomId().equals(roomId))
				.collect(Collectors.toList());
		}
	}
}
