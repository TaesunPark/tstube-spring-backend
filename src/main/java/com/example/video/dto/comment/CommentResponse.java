package com.example.video.dto.comment;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommentResponse {
	private String videoId;
	private String comment;
	private LocalDateTime createTime;
}
