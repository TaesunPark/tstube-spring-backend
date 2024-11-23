package com.example.video.controller;

import com.example.video.dto.VideoInfo;
import com.example.video.dto.VideoRequest;
import com.example.video.dto.VideoResponse;
import com.example.video.response.ApiResponse;
import com.example.video.service.VideoService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import lombok.RequiredArgsConstructor;

@OpenAPIDefinition(info = @Info(title = "Video API", version = "1.0", description = "API for managing videos"))
@RestController
@RequiredArgsConstructor
@RequestMapping("/video")
public class VideoController {

    private static final Logger log = LoggerFactory.getLogger(VideoController.class);
    private final VideoService videoService;
    // 1. video 전체 가져오는 api
    @GetMapping
    public ApiResponse<List<VideoResponse>> getVideos(){
        // 데이터베이스에서 전체 조회를 한다음 가져올거야
        // 응답 형식
        List<VideoInfo> videoInfos = videoService.getAllVideo();

        List<VideoResponse> videoResponses = videoInfos.stream()
            .map(videoInfo -> new VideoResponse(
                videoInfo.getId(),
                videoInfo.getTitle(),
                videoInfo.getSrc(),
                videoInfo.getDescription(),
                videoInfo.getCnt(),
                videoInfo.getChannelTitle()
            )).toList();
        return new ApiResponse<>(true, "Videos retrieved successfully", videoResponses);
    }
    // 2. 무한 스크롤 api
    // 3. 비디오를 하나 선택했을 때 api
    // 4. 비디오 등록 api
    @PostMapping
    public ResponseEntity<ApiResponse<VideoInfo>> createVideo(@RequestBody VideoRequest videoRequest){
        VideoInfo createdVideo = videoService.createVideo(videoRequest);
        // 성공적인 응답 생성
        ApiResponse<VideoInfo> response = new ApiResponse<>(true, "Video created successfully", createdVideo);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    // 5. 비디오 삭제 api
    // 6. 비디오 정보 수정 api
}
