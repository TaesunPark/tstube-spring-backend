package com.example.video.controller.comment;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.video.dto.comment.CommentMapper;
import com.example.video.dto.comment.CommentRequestDto;
import com.example.video.dto.comment.CommentResponse;
import com.example.video.response.ApiResponse;
import com.example.video.service.comment.CommentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/comments")
@Tag(name = "Comment", description = "Comment Management API")
@RequiredArgsConstructor
public class CommentController {

	private final CommentService commentService;
	private final CommentMapper commentMapper;

	@Operation(summary = "Get comments by video ID")
	@GetMapping
	public ApiResponse<List<CommentResponse>> getComments(@RequestParam(value = "v", required = false) String videoId) {
		return new ApiResponse<>(true, "Comments retrieved successfully", commentMapper.toResponseList(commentService.getCommentInfoList(videoId)));
	}

	@Operation(summary = "Create new comment")
	@PostMapping
	public ApiResponse<CommentResponse> createComment(@Valid @RequestBody CommentRequestDto request) {
		return new ApiResponse<>(true, "댓글 달기 성공", commentMapper.toResponse(commentService.createComment(request)));
	}

}