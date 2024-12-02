package com.example.video.dto.comment;

import lombok.Data;

@Data
public class CommentRequestDto {
	private String videoId;
	private String comment;
}