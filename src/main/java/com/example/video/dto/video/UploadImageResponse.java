package com.example.video.dto.video;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UploadImageResponse {
	private final String fileName;
}
