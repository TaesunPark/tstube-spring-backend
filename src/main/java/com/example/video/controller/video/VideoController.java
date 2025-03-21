package com.example.video.controller.video;

import com.example.global.annotation.RequiresControllerAuthentication;
import com.example.user.entity.User;
import com.example.video.dto.video.CreateVideoRequestDto;
import com.example.video.dto.video.VideoMapper;
import com.example.video.dto.video.VideoResponse;
import com.example.video.response.ApiResponse;
import com.example.video.service.video.VideoService;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/videos")
@RequiredArgsConstructor
@Tag(name = "Video", description = "Video Management API")
public class VideoController {

    private final VideoMapper videoMapper; // DTO <-> Entity 변환하는 매퍼
    private final VideoService videoService;

    @Operation(summary = "Get all videos", description = "모든 비디오 조회")
    @GetMapping
    public ApiResponse<List<VideoResponse>> getVideos(@AuthenticationPrincipal User user) {
        return new ApiResponse<>(true, "Videos retrieved successfully", videoMapper.toResponseList(videoService.getAllVideo()));
    }

    @Operation(summary = "Get video by ID")
    @GetMapping("/{videoId}")
    public ApiResponse<VideoResponse> getVideo(@PathVariable String videoId, @AuthenticationPrincipal User user) {
        return new ApiResponse<>(true, videoId, videoMapper.toResponse(videoService.getVideoByVideoId(videoId, user)));
    }

    @Operation(summary = "Create new video")
    @PostMapping
    @RequiresControllerAuthentication
    public ApiResponse<VideoResponse> createVideo(@RequestBody @Valid CreateVideoRequestDto videoRequest, @AuthenticationPrincipal User currentUser) {
        return new ApiResponse<>(true, "video 생성 성공", videoMapper.toResponse(videoService.createVideo(videoRequest, currentUser)));
    }
    // 5. 비디오 삭제 api
    // 6. 비디오 정보 수정 api
}