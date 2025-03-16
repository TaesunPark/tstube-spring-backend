package com.example.video.controller.favorite;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.global.annotation.RequiresControllerAuthentication;
import com.example.user.entity.User;
import com.example.video.dto.video.VideoInfo;
import com.example.video.dto.video.VideoMapper;
import com.example.video.dto.video.VideoResponse;
import com.example.video.response.ApiResponse;
import com.example.video.service.favorite.FavoriteService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/favorites")
@RequiredArgsConstructor
@Tag(name = "Favorite", description = "Favorite Videos Management API")
public class FavoriteController {

	private final FavoriteService favoriteService;
	private final VideoMapper videoMapper;

	@Operation(summary = "Get user's favorite videos", description = "사용자의 즐겨찾기 비디오 목록 조회")
	@GetMapping
	@RequiresControllerAuthentication
	public ApiResponse<List<VideoResponse>> getFavorites(@AuthenticationPrincipal User currentUser) {
		List<VideoInfo> favorites = favoriteService.getUserFavorites(currentUser);
		List<VideoResponse> videoResponses = videoMapper.toResponseList(favorites);
		return new ApiResponse<>(true, "Favorites retrieved successfully", videoResponses);
	}

	@Operation(summary = "Add video to favorites", description = "비디오를 즐겨찾기에 추가")
	@PostMapping("/{videoId}")
	@RequiresControllerAuthentication
	public ApiResponse<Void> addFavorite(
		@PathVariable String videoId,
		@AuthenticationPrincipal User currentUser
	) {
		favoriteService.addFavorite(videoId, currentUser);
		return new ApiResponse<>(true, "Video added to favorites successfully", null);
	}

	@Operation(summary = "Remove video from favorites", description = "비디오를 즐겨찾기에서 제거")
	@DeleteMapping("/{videoId}")
	@RequiresControllerAuthentication
	public ApiResponse<Void> removeFavorite(
		@PathVariable String videoId,
		@AuthenticationPrincipal User currentUser
	) {
		favoriteService.removeFavorite(videoId, currentUser);
		return new ApiResponse<>(true, "Video removed from favorites successfully", null);
	}

}