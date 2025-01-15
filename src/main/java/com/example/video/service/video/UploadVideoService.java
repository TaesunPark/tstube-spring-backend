package com.example.video.service.video;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.global.config.FileStorageProperties;
import com.example.video.dto.video.CreateVideoRequestDto;
import com.example.video.dto.video.UploadVideoInfo;
import com.example.video.dto.video.VideoInfo;
import com.example.video.entity.video.Thumbnail;
import com.example.video.entity.video.Video;
import com.example.video.repository.video.ThumbnailRepository;
import com.example.video.repository.video.VideoRepository;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Service
@AllArgsConstructor
public class UploadVideoService {

	private FileStorageProperties fileStorageProperties;
	private VideoService videoService;
	private ThumbnailRepository thumbnailRepository;
	private VideoRepository videoRepository;

	@Transactional
	public String uploadVideo(MultipartFile file, String title) throws IOException {
		Path uploadPath = Paths.get(fileStorageProperties.getUploadDir());
		if (!Files.exists(uploadPath)) {
			Files.createDirectories(uploadPath);
		}
		// Store the file
		String fileName = file.getOriginalFilename();
		// 여기서 video 추가 세팅
		CreateVideoRequestDto createVideoRequestDto = new CreateVideoRequestDto();
		createVideoRequestDto.setType("upload");
		createVideoRequestDto.setTitle(title);
		createVideoRequestDto.setSrc(fileStorageProperties.getUploadDir());
		createVideoRequestDto.setFileName(fileName);
		VideoInfo videoInfo = videoService.createVideo(createVideoRequestDto);
		Path filePath = uploadPath.resolve(fileName != null ? videoInfo.getFileName() : "?");
		file.transferTo(filePath);
		return fileName; // Return the file name or path for future use
	}

	public File getVideo(String fileName) throws IOException {
		Path filePath = Paths.get(fileStorageProperties.getUploadDir()).resolve(fileName);
		if (!Files.exists(filePath)) {
			throw new IOException("File not found: ".concat(fileName));
		}
		// UploadVideoInfo uploadVideoInfo = new UploadVideoInfo();
		// uploadVideoInfo.setVideoFile(filePath.toFile());
		// uploadVideoInfo.setFileName(fileName);
		return filePath.toFile();
	}

	@Transactional
	public String uploadImage(MultipartFile file, String videoId) throws IOException {
		Path uploadPath = Paths.get(fileStorageProperties.getUploadImageDir());
		if (!Files.exists(uploadPath)) {
			Files.createDirectories(uploadPath);
		}
		// 파일 저장
		String fileName = file.getOriginalFilename();
		Path filePath = uploadPath.resolve(fileName != null ? videoId.concat(fileName) : "?");
		file.transferTo(filePath);
		// 비디오와 썸네일 연결
		Optional<Video> video = videoRepository.findByVideoId(videoId);

		if (video.isEmpty()) {
			assert fileName != null;
			throw new IOException("Video not found: ".concat(fileName));
		}

		// 썸네일 데이터 추가
		Thumbnail thumbnail = thumbnailRepository.save(Thumbnail.builder()
				.url(filePath.toString())
				.video(video.get())
				.createdAt(LocalDateTime.now())
				.updatedAt(LocalDateTime.now())
				.build()
		);

		video.get().setThumbnail(thumbnail);
		// 비디오 업데이트
		videoRepository.save(video.get());

		return filePath.toString(); // Return the file name or path for future use
	}

}
