package com.example.video.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Data
public class VideoResponse {
	private Long id;
	private String title;
	private String src;
	private String description;
	private long cnt;
	private String channelTitle;
}
