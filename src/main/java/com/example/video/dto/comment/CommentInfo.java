package com.example.video.dto.comment;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CommentInfo {
	private String videoId;
	private String comment;
	private LocalDateTime createTime;
}
