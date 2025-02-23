package com.example.video.dto.comment;

import java.util.List;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CommentMapper {
	public CommentResponse toResponse(CommentInfo commentInfo) {
		return CommentResponse.builder()
			.videoId(commentInfo.getVideoId())
			.comment(commentInfo.getComment())
			.createTime(commentInfo.getCreateTime())
			.build();
	}

	public List<CommentResponse> toResponseList(List<CommentInfo> commentInfoList) {
		return commentInfoList.stream()
			.map(this::toResponse)
			.toList();
	}
}
