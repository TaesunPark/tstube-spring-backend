package com.example.video.dto.video;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.example.video.entity.video.Thumbnail;
import com.example.video.entity.video.Video;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class VideoMapper {

	public VideoResponse toResponse(VideoInfo videoInfo) {
		return VideoResponse.builder()
			.videoId(videoInfo.getVideoId())
			.title(videoInfo.getTitle())
			.src(videoInfo.getSrc())
			.description(videoInfo.getDescription())
			.fileName(videoInfo.getFileName())
			.cnt(videoInfo.getCnt())
			.type(videoInfo.getType())
			.channelTitle(videoInfo.getChannelTitle())
			.createTime(videoInfo.getCreateTime())
			.updateTime(videoInfo.getUpdateTime())
			.thumbnailUrl(Optional.ofNullable(videoInfo.getThumbnail())
				.map(Thumbnail::getUrl)
				.orElse(""))
			.build();
	}

	public List<VideoResponse> toResponseList(List<VideoInfo> videoInfos) {
		return videoInfos.stream()
			.map(this::toResponse)
			.toList();
	}
}
