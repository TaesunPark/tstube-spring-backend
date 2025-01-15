package com.example.video.controller.video;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeTypeUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.global.config.FileStorageProperties;
import com.example.video.dto.video.UploadImageInfo;
import com.example.video.dto.video.UploadVideoInfo;
import com.example.video.response.ApiResponse;
import com.example.video.service.video.UploadVideoService;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import lombok.RequiredArgsConstructor;

@OpenAPIDefinition(info = @Info(title = "Video API", version = "1.0", description = "API for managing videos"))
@RestController
@RequiredArgsConstructor
public class UploadVideoController {

	private final UploadVideoService uploadVideoService;
	private final FileStorageProperties fileStorageProperties;

	// 4. 비디오 등록 api
	@PostMapping(value = "/upload")
	public ResponseEntity<ApiResponse<UploadVideoInfo>> createVideo(@RequestParam("file") MultipartFile file, @RequestParam("title") String title)  {
		try {
			String fileName = uploadVideoService.uploadVideo(file, title);
			UploadVideoInfo uploadVideoInfo = new UploadVideoInfo(fileName, null);
			ApiResponse<UploadVideoInfo> response = new ApiResponse<>(true, "upload created successfully", uploadVideoInfo);
			return ResponseEntity.status(HttpStatus.CREATED).body(response);
		} catch (IOException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	@PostMapping(value = "/upload/image")
	public ResponseEntity<ApiResponse<UploadImageInfo>> createImage(@RequestParam("file") MultipartFile file, @RequestParam("videoId") String videoId) {
		try {
			String fileName = uploadVideoService.uploadImage(file, videoId);
			UploadImageInfo uploadImageInfo = new UploadImageInfo(fileName, null);
			ApiResponse<UploadImageInfo> response = new ApiResponse<>(true, "upload created successfully", uploadImageInfo);
			return ResponseEntity.status(HttpStatus.CREATED).body(response);
		} catch (IOException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	@GetMapping(value = {"/upload", "/upload/images"})
	public ResponseEntity<Resource> getVideo(@RequestParam String fileName) {
		try {
			// 비디오 파일 경로 설정
			Path videoPath = Paths.get(fileStorageProperties.getUploadDir(), fileName);
			Resource resource = new UrlResource(videoPath.toUri());

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
			default -> MediaType.APPLICATION_OCTET_STREAM; // 기본 값
		};
	}
}
