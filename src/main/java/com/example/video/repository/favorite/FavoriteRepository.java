package com.example.video.repository.favorite;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.user.entity.User;
import com.example.video.entity.favorite.Favorite;
import com.example.video.entity.video.Video;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
	boolean existsByUserAndVideo(User user, Video video);
	Optional<Favorite> findByUserAndVideo(User user, Video video);
	List<Favorite> findByUserOrderByCreatedAtDesc(User user);
	void deleteByUserAndVideo(User user, Video video);
	boolean existsByUserAndVideo_VideoId(User user, String video_videoId);
}
