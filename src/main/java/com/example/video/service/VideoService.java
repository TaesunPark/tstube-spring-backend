package com.example.video.service;

import com.example.video.dto.VideoInfo;
import com.example.video.dto.VideoRequest;
import com.example.video.entity.Video;
import com.example.video.repository.VideoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class VideoService {

    private final VideoRepository videoRepository;

    // 데이터베이스에서 전체 조회하는 기능
    @Transactional
    public List<VideoInfo> getAllVideo() {
        List<Video> videos = videoRepository.findAll();
        return videos.stream()
                .map(video -> new VideoInfo(video.getId(), video.getTitle(), video.getSrc(), video.getDescription(), video.getCnt(), video.getChannelTitle()))
                .collect(Collectors.toList());
    }

    public VideoInfo createVideo(VideoRequest videoRequest) {
        Video video = Video.builder()
            .title(videoRequest.getTitle())
            .src(videoRequest.getSrc())
            .description(videoRequest.getDescription())
            .channelTitle(videoRequest.getChannelTitle())
            .build();

        Video savedVideo = videoRepository.save(video);

        return new VideoInfo(savedVideo.getId(), savedVideo.getTitle(), savedVideo.getSrc(), savedVideo.getDescription(), savedVideo.getCnt(), savedVideo.getChannelTitle());
    }
}