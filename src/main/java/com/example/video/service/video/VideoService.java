package com.example.video.service.video;

import com.example.global.annotation.RequiresServiceAuthentication;
import com.example.user.entity.User;
import com.example.video.dto.video.CreateVideoRequestDto;
import com.example.video.dto.video.VideoInfo;
import com.example.video.dto.video.VideoMapper;
import com.example.video.entity.video.Video;
import com.example.video.exception.NotFoundException;
import com.example.video.repository.video.VideoRepository;
import com.example.video.service.favorite.FavoriteService;

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
    private final FavoriteService favoriteService;
    private final VideoMapper videoMapper;
    // 데이터베이스에서 전체 조회하는 기능
    @Transactional
    public List<VideoInfo> getAllVideo() {
        return videoRepository.findAll().stream()
            .map(VideoInfo::new)  // VideoInfo 생성자를 사용
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

        return new VideoInfo(savedVideo);
    }

    @Transactional
    public VideoInfo getVideoByVideoId(String videoId, User user) {
        Video video = videoRepository.findByVideoId(videoId)
            .orElseThrow(() -> new NotFoundException(String.format("Video not found with videoId: %s", videoId)));
        boolean isFavorite = false;

        if (user != null) {
            // 즐겨찾기 id인지 확인
            isFavorite = favoriteService.existsByUserFavorite(user, video);
        }
        return new VideoInfo(video, isFavorite);
    }

    public String createUUID() {
        return UUID.randomUUID().toString();
    }
}