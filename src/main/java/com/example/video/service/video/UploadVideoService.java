package com.example.video.service.video;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.global.annotation.RequiresServiceAuthentication;
import com.example.global.config.FileStorageProperties;
import com.example.user.entity.User;
import com.example.video.dto.video.CreateVideoRequestDto;
import com.example.video.dto.video.VideoInfo;
import com.example.video.entity.video.Thumbnail;
import com.example.video.entity.video.Video;
import com.example.video.exception.FileUploadException;
import com.example.video.repository.video.ThumbnailRepository;
import com.example.video.repository.video.VideoRepository;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class UploadVideoService {

	private FileStorageProperties fileStorageProperties;
	private VideoService videoService;
	private ThumbnailRepository thumbnailRepository;
	private VideoRepository videoRepository;

	/**
	 * 비디오 파일 업로드 (기존 방식 - 단일 요청으로 업로드)
	 */
	@Transactional
	@RequiresServiceAuthentication
	public VideoInfo uploadVideo(MultipartFile file, String title, User user) {
		validateFile(file);
		String fileName = storeFile(file);
		return createAndStoreVideo(file, fileName, title, user);
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

	private VideoInfo createAndStoreVideo(MultipartFile file, String fileName, String title, User user) {
		try {
			// 비디오 정보 생성
			CreateVideoRequestDto createVideoRequestDto = CreateVideoRequestDto.builder()
				.type("upload")
				.title(title)
				.src(fileStorageProperties.getUploadDir())
				.fileName(fileName)
				.build();

			// 비디오 정보 저장
			VideoInfo videoInfo = videoService.createVideo(createVideoRequestDto, user);

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

	/**
	 * 청크 업로드 완료 후 비디오 정보 저장
	 */
	@Transactional
	@RequiresServiceAuthentication
	public VideoInfo saveVideoFromChunks(String originalFileName, String title, User user) {
		try {
			// 비디오 정보 생성
			CreateVideoRequestDto createVideoRequestDto = CreateVideoRequestDto.builder()
				.type("upload")
				.title(title)
				.src(fileStorageProperties.getUploadDir())
				.fileName(originalFileName)
				.build();

			// 비디오 정보 저장
			VideoInfo videoInfo = videoService.createVideo(createVideoRequestDto, user);

			log.info("청크 업로드 완료: videoId={}, fileName={}", videoInfo.getVideoId(), originalFileName);

			return videoInfo;
		} catch (Exception e) {
			// 파일 저장 중 에러 발생 시 업로드된 파일 삭제
			deleteFileIfExists(originalFileName);
			throw new FileUploadException("비디오 정보 생성에 실패했습니다.", e);
		}
	}

	/**
	 * 임시 디렉토리 생성
	 */
	public Path createTempDirectoryIfNotExists() throws IOException {
		Path tempPath = Paths.get(fileStorageProperties.getUploadDir(), "temp");
		if (!Files.exists(tempPath)) {
			Files.createDirectories(tempPath);
		}
		return tempPath;
	}

	/**
	 * 임시 디렉토리 초기화 (애플리케이션 시작 시 호출)
	 */
	public void initTempDirectory() {
		try {
			// 임시 디렉토리 경로
			Path tempDir = createTempDirectoryIfNotExists();

			// 기존 임시 파일 정리
			Files.list(tempDir)
				.filter(path -> path.toString().endsWith(".part"))
				.forEach(path -> {
					try {
						Files.delete(path);
						log.info("임시 파일 삭제: {}", path);
					} catch (IOException e) {
						log.error("임시 파일 삭제 실패: {}", path, e);
					}
				});

			log.info("임시 디렉토리 초기화 완료");
		} catch (IOException e) {
			log.error("임시 디렉토리 초기화 중 오류 발생", e);
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