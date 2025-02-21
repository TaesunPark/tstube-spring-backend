package com.example.video.controller.video;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.global.config.FileStorageProperties;
import com.example.video.dto.video.UploadImageResponse;
import com.example.video.dto.video.UploadVideoResponse;
import com.example.video.dto.video.VideoInfo;
import com.example.video.response.ApiResponse;
import com.example.video.service.video.UploadVideoService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/upload")
@RequiredArgsConstructor
public class UploadVideoController {

	private final UploadVideoService uploadVideoService;
	private final FileStorageProperties fileStorageProperties;

	@Operation(summary = "Upload video file")
	@PostMapping(value = "/videos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ApiResponse<UploadVideoResponse> createVideo(@RequestParam("file") MultipartFile file, @RequestParam("title") String title)  {
		try {
			VideoInfo videoInfo = uploadVideoService.uploadVideo(file, title);
			return new ApiResponse<>(true, "비디오 등록 성공", UploadVideoResponse.builder().videoId(videoInfo.getVideoId()).fileName(videoInfo.getFileName()).build());
		} catch (IOException e) {
			try {
				throw new FileUploadException("Failed to upload video file", e);
			} catch (FileUploadException ex) {
				throw new RuntimeException(ex);
			}
		}
	}

	@Operation(summary = "Upload thumbnail image")
	@PostMapping(value = "/thumbnails", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ApiResponse<UploadImageResponse> createImage(@RequestParam("file") MultipartFile file, @RequestParam("videoId") String videoId) {
		try {
			String fileName = uploadVideoService.uploadImage(file, videoId);
			return new ApiResponse<>(true, "썸네일 등록 성공", UploadImageResponse.builder().fileName(fileName).build());
		} catch (IOException e) {
			try {
				throw new FileUploadException("Failed to upload thumbnail file", e);
			} catch (FileUploadException ex) {
				throw new RuntimeException(ex);
			}
		}
	}

	@Operation(summary = "Get uploaded file")
	@GetMapping(value = {"", "/images"})
	public ResponseEntity<Resource> getVideo(@RequestParam String fileName) {
		try {
			// 비디오 파일 경로 설정
			Path videoPath = Paths.get(fileStorageProperties.getUploadDir(), fileName);
			Path imagePath = Paths.get(fileName);
			Resource resource = new UrlResource(videoPath.toUri());
			// 이미지 파일 경우
			if (!resource.isReadable()) {
				resource = new UrlResource(imagePath.toUri());
			}

			if (resource.exists() && resource.isReadable()) {
				// MIME 타입 자동 감지
				MediaType contentType = setMediaType(fileName);

				// 응답 헤더 설정
				HttpHeaders headers = new HttpHeaders();
				headers.setContentDisposition(ContentDisposition.inline().filename(fileName).build());
				headers.setContentType(contentType);

				return ResponseEntity.ok()
					.headers(headers)
					.body(resource);
			} else {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
			}
		} catch (IOException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	/**
	 * 파일 이름에 따라 적절한 MediaType 반환
	 */
	private MediaType setMediaType(String fileName) {
		String extension = StringUtils.getFilenameExtension(fileName);

		assert extension != null;

		return switch (extension) {
			case "mp4" -> MediaType.valueOf("video/mp4");
			case "avi" -> MediaType.valueOf("video/x-msvideo");
			case "mp3" -> MediaType.valueOf("audio/mpeg");
			case "ogg" -> MediaType.valueOf("audio/ogg");
			case "wav" -> MediaType.valueOf("audio/wav");
			case "jpeg" -> MediaType.valueOf("image/jpeg");
			default -> MediaType.APPLICATION_OCTET_STREAM; // 기본 값
		};
	}
}
