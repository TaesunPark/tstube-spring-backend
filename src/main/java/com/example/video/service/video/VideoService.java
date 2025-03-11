package com.example.video.service.video;

import com.example.global.annotation.RequiresServiceAuthentication;
import com.example.user.entity.User;
import com.example.video.dto.video.CreateVideoRequestDto;
import com.example.video.dto.video.VideoInfo;
import com.example.video.entity.video.Video;
import com.example.video.exception.NotFoundException;
import com.example.video.repository.video.VideoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RequiredArgsConstructor
@Service
public class VideoService {

    private final VideoRepository videoRepository;

    // 데이터베이스에서 전체 조회하는 기능
    @Transactional
    public List<VideoInfo> getAllVideo() {
        List<Video> videos = videoRepository.findAll();
        return videos.stream()
            .map(video -> new VideoInfo(video.getVideoId(), video.getTitle(), video.getSrc(), video.getDescription(), video.getCnt(), video.getChannelTitle(), video.getCreateTime(), video.getUpdateTime(), video.getType(), video.getFileName(), video.getThumbnail()))
            .collect(Collectors.toList());
    }

    @Transactional
    @RequiresServiceAuthentication
    public VideoInfo createVideo(CreateVideoRequestDto videoRequest, User user) {
        // 1. 비디오만의 UUID 가져오기.
        // 나중에 redis로 검증할거임 ㅋㅋ
        String videoId = createUUID();

        Video video = Video.builder()
            .title(videoRequest.getTitle())
            .src(videoRequest.getSrc())
            .description(videoRequest.getDescription())
            .channelTitle(videoRequest.getChannelTitle())
            .videoId(videoId)
            .createTime(LocalDateTime.now())
            .updateTime(LocalDateTime.now())
            .type(videoRequest.getType())
            .fileName(videoRequest.getFileName())
            .user(user)
            .build();

        if (videoRequest.getType().equals("upload")) {
            video.setFileName("%s_%s".formatted(videoId, ".mp4"));
        }

        Video savedVideo = videoRepository.save(video);
        log.debug(savedVideo.toString());

        return new VideoInfo(savedVideo.getVideoId(), savedVideo.getTitle(), savedVideo.getSrc(), savedVideo.getDescription(), savedVideo.getCnt(), savedVideo.getChannelTitle(), savedVideo.getCreateTime(), savedVideo.getUpdateTime(), savedVideo.getType(), savedVideo.getFileName(), savedVideo.getThumbnail());
    }

    @Transactional
    public VideoInfo getVideoByVideoId(String videoId) {
        Video video = videoRepository.findByVideoId(videoId)
            .orElseThrow(() -> new NotFoundException(String.format("Video not found with videoId: %s", videoId)));

        return new VideoInfo(Optional.ofNullable(video));
    }

    public String createUUID() {
        return UUID.randomUUID().toString();
    }
}