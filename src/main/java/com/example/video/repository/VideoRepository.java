package com.example.video.repository;

import java.util.Optional;
import java.util.UUID;

import com.example.video.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoRepository extends JpaRepository<Video, UUID> {
	Optional<Video> findByVideoId(String id);
}
