package com.example.video.service.favorite;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.user.entity.User;
import com.example.video.dto.video.VideoInfo;
import com.example.video.entity.favorite.Favorite;
import com.example.video.entity.video.Video;
import com.example.video.exception.AlreadyExistsException;
import com.example.video.exception.NotFoundException;
import com.example.video.repository.favorite.FavoriteRepository;
import com.example.video.repository.video.VideoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FavoriteService {
	private final FavoriteRepository favoriteRepository;
	private final VideoRepository videoRepository;

	@Transactional
	public void addFavorite(String videoId, User user) {
		Video video = videoRepository.findByVideoId(videoId)
			.orElseThrow(() -> new NotFoundException(String.format("Video not found with Id %s", videoId)));

		// 이미 즐겨찾기에 있는지 확인
		if (favoriteRepository.existsByUserAndVideo(user, video)){
			throw new AlreadyExistsException("Video alreay in favorites");
		}

		Favorite favorite = Favorite.builder()
			.user(user)
			.video(video)
			.build();

		favoriteRepository.save(favorite);
	}

	@Transactional
	public void removeFavorite(String videoId, User user) {
		Video video = videoRepository.findByVideoId(videoId)
			.orElseThrow(() -> new NotFoundException(String.format("Video not found with Id %s", videoId)));

		Favorite favorite = favoriteRepository.findByUserAndVideo(user, video)
			.orElseThrow(() -> new NotFoundException(String.format("Favorite not found with User %s", user)));

		favoriteRepository.delete(favorite);
	}

	@Transactional(readOnly = true)
	public List<VideoInfo> getUserFavorites(User user) {
		return favoriteRepository.findByUserOrderByCreatedAtDesc(user).stream()
			.map(Favorite::getVideo)
			.filter(Objects::nonNull)  // null인 비디오가 있을 경우 필터링
			.map(VideoInfo::new)       // 새 생성자를 사용하여 VideoInfo 객체 생성
			.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public boolean existsByUserFavorite(User user, Video video) {
		return favoriteRepository.existsByUserAndVideo(user, video);
	}


}
