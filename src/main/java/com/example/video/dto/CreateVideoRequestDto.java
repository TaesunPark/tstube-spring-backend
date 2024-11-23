package com.example.video.dto;

import lombok.Data;

@Data
public class VideoCreatedRequestDto {
	private String title;
	private String src;
	private String description;
	private String channelTitle;
}
