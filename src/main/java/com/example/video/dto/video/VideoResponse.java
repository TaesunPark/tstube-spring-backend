package com.example.video.dto.video;

import java.time.LocalDateTime;

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

	public VideoResponse (VideoInfo videoInfo) {
		videoId = videoInfo.getVideoId();
		title = videoInfo.getTitle();
		src = videoInfo.getSrc();
		description = videoInfo.getDescription();
		fileName = videoInfo.getFileName();
		cnt = videoInfo.getCnt();
		type = videoInfo.getType();
		channelTitle = videoInfo.getChannelTitle();
		createTime = videoInfo.getCreateTime();
		updateTime = videoInfo.getUpdateTime();
		thumbnailUrl = videoInfo.getThumbnail() == null ? "" : videoInfo.getThumbnail().getUrl();
	}
}
