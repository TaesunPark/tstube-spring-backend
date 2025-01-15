package com.example.video.repository.video;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.video.entity.video.Thumbnail;
import com.example.video.entity.video.Video;

@Repository
public interface ThumbnailRepository extends JpaRepository<Thumbnail, UUID> {
}
