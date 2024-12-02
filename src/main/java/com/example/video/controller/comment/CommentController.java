package com.example.video.controller.comment;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.video.dto.comment.CommentInfo;
import com.example.video.dto.comment.CommentRequestDto;
import com.example.video.dto.comment.CommentResponse;
import com.example.video.dto.video.CreateVideoRequestDto;
import com.example.video.dto.video.VideoInfo;
import com.example.video.response.ApiResponse;
import com.example.video.service.comment.CommentService;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import lombok.RequiredArgsConstructor;

@OpenAPIDefinition(info = @Info(title = "Video API", version = "1.0", description = "API for managing videos"))
@RestController
@RequiredArgsConstructor
public class CommentController {

	private final CommentService commentService;

	// Comments
	// content, createTime
	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, value = "/comments")
	public ApiResponse<List<CommentResponse>> getComments(@RequestParam(value = "v", required = false) String videoId) {
		List<CommentInfo> commentInfos = commentService.getCommentInfoList(videoId);

		List<CommentResponse> commentResponses = commentInfos.stream().map(
			commentInfo -> new CommentResponse(commentInfo.getVideoId(), commentInfo.getComment(), commentInfo.getCreateTime())
		).toList();

		return new ApiResponse<>(true, "Comments retrieved successfully", commentResponses);
	}

	@PostMapping(value = "/comment")
	public ResponseEntity<ApiResponse<CommentInfo>> createComment(@RequestBody CommentRequestDto commentRequestDto) {
		CommentInfo commentInfo = commentService.createComment(commentRequestDto);
		// 성공적인 응답 생성
		ApiResponse<CommentInfo> response = new ApiResponse<>(true, "Video created successfully", commentInfo);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

}