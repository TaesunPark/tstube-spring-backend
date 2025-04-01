package com.example.chat.controller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.chat.entity.ChatMessage;
import com.example.chat.repository.ChatMessageRepository;
import com.example.chat.service.ChatMessageBatchProcessor;
import com.example.chat.service.ChatService;
import com.example.user.entity.User;
import com.example.video.response.ApiResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {
	private final ChatMessageRepository chatMessageRepository;
	private final ChatService chatService;
	private final ChatMessageBatchProcessor chatMessageBatchProcessor;

	@GetMapping("/history/{videoId}")
	public ApiResponse<Page<ChatMessage>> getChatHistory(
		@PathVariable String videoId,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "50") int size,
		@AuthenticationPrincipal User user)
	{
		// 시간 역순 정렬 (최신 메시지부터)
		PageRequest pageRequest = PageRequest.of(page, size, Sort.by("timestamp").descending());
		Page<ChatMessage> savedMessages = chatMessageRepository.findByRoomId(videoId, pageRequest);

		if (page == 0) {
			List<ChatMessage> pendingMessages = chatMessageBatchProcessor.getPendingMessages(videoId);

			if (!pendingMessages.isEmpty()) {
				// 기존 페이지 결과에 버퍼 메시지 추가
				List<ChatMessage> allMessages = new ArrayList<>(pendingMessages);
				allMessages.addAll(savedMessages.getContent());
				// 시간 역순 정렬
				allMessages.sort(Comparator.comparing(ChatMessage::getTimestamp).reversed());

				// 페이지 크기에 맞게 자르기
				int toIndex = Math.min(size, allMessages.size());
				List<ChatMessage> firstPageMessages = allMessages.subList(0, toIndex);

				// 새로운 Page 객체 생성
				return new ApiResponse<>(true, "chat successfully", new PageImpl<>(firstPageMessages, pageRequest, savedMessages.getTotalElements()));
			}
		}

		return new ApiResponse<>(true, "chat successfully", savedMessages);
	}

	@GetMapping("/online/{videoId}")
	public ApiResponse<Map<String, Object>> getChatOnline(@PathVariable String videoId) {
		log.info("Getting online users for video: {}", videoId);

		try {
			Map<String, String> onlineUsers = chatService.getOnlineUsers(videoId);
			log.info("Found {} online users for video {}", onlineUsers.size(), videoId);
			Map<String, Object> responseData = new HashMap<>();
			responseData.put("data", onlineUsers);
			return new ApiResponse<>(true, "온라인 사용자 목록을 조회했습니다.", responseData);
		} catch (Exception e) {
			log.error("Failed to get online users for video {}: {}", videoId, e.getMessage());

			// 에러 시에도 동일한 구조 유지
			Map<String, Object> errorData = new HashMap<>();
			errorData.put("data", new HashMap<>());
			return new ApiResponse<>(false, "온라인 사용자 목록 조회 실패: " + e.getMessage(), errorData);
		}
	}

}
