package com.example.video.controller.video;

import com.example.video.dto.video.CreateVideoRequestDto;
import com.example.video.dto.video.VideoInfo;
import com.example.video.dto.video.VideoResponse;
import com.example.video.response.ApiResponse;
import com.example.video.service.video.VideoService;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import lombok.RequiredArgsConstructor;

@OpenAPIDefinition(info = @Info(title = "Video API", version = "1.0", description = "API for managing videos"))
@RestController
@RequiredArgsConstructor
public class VideoController {

    // private static final Logger log = LoggerFactory.getLogger(VideoController.class);
    private final VideoService videoService;
    // 1. video 전체 가져오는 api
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, value = "/videos")
    public ApiResponse<List<VideoResponse>> getVideos() {
        // 데이터베이스에서 전체 조회를 한다음 가져올거야
        // 응답 형식
        List<VideoInfo> videoInfos = videoService.getAllVideo();

        List<VideoResponse> videoResponses = videoInfos.stream()
            .map(videoInfo -> new VideoResponse(
                videoInfo.getVideoId(),
                videoInfo.getTitle(),
                videoInfo.getSrc(),
                videoInfo.getDescription(),
                videoInfo.getCnt(),
                videoInfo.getChannelTitle(),
                videoInfo.getCreateTime(),
                videoInfo.getUpdateTime()
            )).toList();
        return new ApiResponse<>(true, "Videos retrieved successfully", videoResponses);
    }

    // 2. 무한 스크롤 api
    // 3. 비디오를 하나 선택했을 때 api
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, value = "/video")
    public ApiResponse<VideoResponse> getVideoById(@RequestParam(value = "v", required = false) String videoId) {
        // videoId로 영상 가져오기
        VideoInfo videoInfo = videoService.getVideoByVideoId(videoId);
        VideoResponse videoResponse = new VideoResponse(videoInfo.getVideoId(), videoInfo.getTitle(), videoInfo.getSrc(), videoInfo.getDescription(), videoInfo.getCnt(), videoInfo.getChannelTitle(), videoInfo.getCreateTime(), videoInfo.getUpdateTime());
        return new ApiResponse<>(true, videoId, videoResponse);
    }

    // 4. 비디오 등록 api
    @PostMapping(value = "/video")
    public ResponseEntity<ApiResponse<VideoInfo>> createVideo(@RequestBody CreateVideoRequestDto videoRequest) {
        VideoInfo createdVideo = videoService.createVideo(videoRequest);
        // 성공적인 응답 생성
        ApiResponse<VideoInfo> response = new ApiResponse<>(true, "Video created successfully", createdVideo);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    // 5. 비디오 삭제 api
    // 6. 비디오 정보 수정 api
}