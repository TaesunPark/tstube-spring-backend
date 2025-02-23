package com.example.video.dto.video;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UploadVideoResponse {
	private final String fileName;
	private final String videoId;
}
