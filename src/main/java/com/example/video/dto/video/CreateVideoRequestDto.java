package com.example.video.dto.video;

import lombok.Data;

// 비디오 생성할 때 요청하는 DTO 입니다.
@Data
public class CreateVideoRequestDto {
	private String title;
	private String src;
	private String description;
	private String channelTitle;
}
