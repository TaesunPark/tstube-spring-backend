package com.example.video.service.video;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.global.config.FileStorageProperties;
import com.example.video.dto.video.CreateVideoRequestDto;
import com.example.video.dto.video.VideoInfo;
import com.example.video.entity.video.Thumbnail;
import com.example.video.entity.video.Video;
import com.example.video.exception.FileUploadException;
import com.example.video.repository.video.ThumbnailRepository;
import com.example.video.repository.video.VideoRepository;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UploadVideoService {

	private FileStorageProperties fileStorageProperties;
	private VideoService videoService;
	private ThumbnailRepository thumbnailRepository;
	private VideoRepository videoRepository;

	@Transactional
	public VideoInfo uploadVideo(MultipartFile file, String title) {
		validateFile(file);
		String fileName = storeFile(file);
		return createAndStoreVideo(file, fileName, title);
	}

	private void validateFile(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new FileUploadException("파일이 비어있습니다.");
		}
		if (file.getOriginalFilename() == null) {
			throw new FileUploadException("파일 이름이 없습니다.");
		}
	}

	private String storeFile(MultipartFile file) {
		try {
			createUploadDirectoryIfNotExists();
			String fileName = file.getOriginalFilename();
			return fileName != null ? fileName : "unknown";
		} catch (IOException e) {
			throw new FileUploadException("파일 저장에 실패했습니다.", e);
		}
	}

	private Path createUploadDirectoryIfNotExists() throws IOException {
		Path uploadPath = Paths.get(fileStorageProperties.getUploadDir());
		if (!Files.exists(uploadPath)) {
			Files.createDirectories(uploadPath);
		}
		return uploadPath;
	}

	private VideoInfo createAndStoreVideo(MultipartFile file, String fileName, String title) {
		try {
			// 비디오 정보 생성
			CreateVideoRequestDto createVideoRequestDto = CreateVideoRequestDto.builder()
				.type("upload")
				.title(title)
				.src(fileStorageProperties.getUploadDir())
				.fileName(fileName)
				.build();

			// 비디오 정보 저장
			VideoInfo videoInfo = videoService.createVideo(createVideoRequestDto);

			// 실제 파일 저장
			Path uploadPath = createUploadDirectoryIfNotExists();
			Path filePath = uploadPath.resolve(videoInfo.getFileName());
			file.transferTo(filePath);

			return videoInfo;
		} catch (IOException e) {
			throw new FileUploadException("비디오 파일 저장에 실패했습니다.", e);
		} catch (Exception e) {
			// 파일 저장 중 에러 발생 시 업로드된 파일 삭제
			deleteFileIfExists(fileName);
			throw new FileUploadException("비디오 정보 생성에 실패했습니다.", e);
		}
	}

	private void deleteFileIfExists(String fileName) {
		try {
			Path filePath = Paths.get(fileStorageProperties.getUploadDir(), fileName);
			Files.deleteIfExists(filePath);
		} catch (IOException e) {
			throw new RuntimeException("파일 삭제 실패");
		}
	}

	@Transactional
	public String uploadImage(MultipartFile file, String videoId) {
		try {
			Path uploadPath = createImageUploadDirectoryIfNotExists();
			String fileName = generateImageFileName(file, videoId);
			Path filePath = storeImageFile(file, uploadPath, fileName);
			return saveImageInfo(filePath, videoId);
		} catch (IOException e) {
			throw new FileUploadException("이미지 업로드에 실패했습니다.", e);
		}
	}

	private Path createImageUploadDirectoryIfNotExists() throws IOException {
		Path uploadPath = Paths.get(fileStorageProperties.getUploadImageDir());
		if (!Files.exists(uploadPath)) {
			Files.createDirectories(uploadPath);
		}
		return uploadPath;
	}

	private String generateImageFileName(MultipartFile file, String videoId) {
		String originalFileName = file.getOriginalFilename();
		return originalFileName != null ? videoId.concat(originalFileName) : videoId.concat("unknown");
	}

	private Path storeImageFile(MultipartFile file, Path uploadPath, String fileName) throws IOException {
		Path filePath = uploadPath.resolve(fileName);
		file.transferTo(filePath);
		return filePath;
	}

	private String saveImageInfo(Path filePath, String videoId) {
		Video video = videoRepository.findByVideoId(videoId)
			.orElseThrow(() -> new FileUploadException(String.format("Video not found: %s", videoId)));

		Thumbnail thumbnail = thumbnailRepository.save(Thumbnail.builder()
			.url(filePath.toString())
			.video(video)
			.createdAt(LocalDateTime.now())
			.updatedAt(LocalDateTime.now())
			.build());

		video.setThumbnail(thumbnail);
		videoRepository.save(video);

		return filePath.toString();
	}

}
