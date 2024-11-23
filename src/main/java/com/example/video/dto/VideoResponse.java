package com.example.video.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class VideoResponse {
	private String videoId;
	private String title;
	private String src;
	private String description;
	private long cnt;
	private String channelTitle;
}
