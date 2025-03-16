package com.example.video.dto.video;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class VideoResponse {
	private String videoId;
	private String title;
	private String src;
	private String description;
	private String fileName;
	private long cnt;
	private String type;
	private String channelTitle;
	private LocalDateTime createTime;
	private LocalDateTime updateTime;
	private String thumbnailUrl;
	@JsonProperty("isFavorite")
	private boolean isFavorite;
}
