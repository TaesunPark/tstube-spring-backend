package com.example.video.dto.video;

import java.io.File;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UploadVideoInfo {
	private String fileName;
	private File videoFile;
	private String videoId;
}
