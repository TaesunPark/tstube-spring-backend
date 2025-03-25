package com.example.video.controller.video;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.global.annotation.RequiresControllerAuthentication;
import com.example.global.config.FileStorageProperties;
import com.example.user.entity.User;
import com.example.video.dto.video.ChunkUploadInfo;
import com.example.video.dto.video.UploadImageResponse;
import com.example.video.dto.video.UploadVideoResponse;
import com.example.video.dto.video.VideoInfo;
import com.example.video.dto.video.VideoMapper;
import com.example.video.response.ApiResponse;
import com.example.video.service.video.UploadVideoService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RestController
@RequestMapping("/api/v1/upload")
@RequiredArgsConstructor
public class UploadVideoController {

	private final UploadVideoService uploadVideoService;
	private final FileStorageProperties fileStorageProperties;
	private final VideoMapper videoMapper;
	// 청크 업로드 정보 저장을 위한 맵 (실제 서비스에서는 Redis나 DB 사용 권장)
	private static final Map<String, ChunkUploadInfo> UPLOAD_INFO_MAP = new ConcurrentHashMap<>();

	@Operation(summary = "Upload video file")
	@PostMapping(value = "/videos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@RequiresControllerAuthentication
	public ApiResponse<UploadVideoResponse> createVideo(@RequestParam("file") MultipartFile file, @RequestParam("title") String title, @AuthenticationPrincipal User user)  {
		return new ApiResponse<>(true, "비디오 등록 성공", videoMapper.toSimpleResponse(uploadVideoService.uploadVideo(file, title, user)));
	}

	@Operation(summary = "Upload video chunk")
	@PostMapping(value = "/videos/chunk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@RequiresControllerAuthentication
	public ApiResponse<?> uploadChunk(
		@RequestParam("file") MultipartFile file,
		@RequestParam("chunkIndex") int chunkIndex,
		@RequestParam("totalChunks") int totalChunks,
		@RequestParam("fileId") String fileId,
		@RequestParam("title") String title,
		@RequestParam("fileName") String fileName,
		@RequestParam("fileSize") long fileSize,
		@AuthenticationPrincipal User user) throws IOException {

	log.info("청크 업로드: index={}, total={}, fileId={}", chunkIndex, totalChunks, fileId);

	// 첫 번재 청크이고 fileId가 없으면 새로 생성
	if (chunkIndex == 0 && (fileId == null || fileId.isEmpty())) {
		fileId = UUID.randomUUID().toString();

		// 임시 디렉토리 생성
		String tempDir = fileStorageProperties.getUploadDir() + "/temp";
		File tempDirectory = new File(tempDir);
		if (!tempDirectory.exists()) {
			tempDirectory.mkdir();
		}

		// 업로드 정보 저장
		UPLOAD_INFO_MAP.put(fileId, new ChunkUploadInfo(
				fileName,
				fileSize,
				totalChunks,
				title,
				user
		));
	}

	// 업로드 정보 확인
	ChunkUploadInfo uploadInfo = UPLOAD_INFO_MAP.get(fileId);
	if (uploadInfo == null) {
		return new ApiResponse<>(false, "유효하지 않은 업로드 ID입니다.", null);
	}

	// 임시 파일에 청크 쓰기
		String tempDir = fileStorageProperties.getUploadDir() + "/temp/";
		String tempFilePath = tempDir + File.separator + fileId + ".part";

		try (RandomAccessFile raf = new RandomAccessFile(tempFilePath, "rw")) {
			// 청크 크기 계산 (클라이언트와 동일한 값 사용)
			long chunkSize = 10 * 1024 * 1024; // 10MB (프론트엔드와 일치시킴)

			// 파일 포인터를 청크 위치로 정확히 이동
			long position = chunkIndex * chunkSize;
			raf.seek(position);

			// 청크 데이터 쓰기
			byte[] bytes = file.getBytes();
			raf.write(bytes);

			// 로그 추가 (디버깅용)
			System.out.println("청크 " + chunkIndex + " 기록 완료: 위치=" + position + ", 크기=" + bytes.length);

			// 업로드 정보 업데이트
			uploadInfo.setReceivedChunks(uploadInfo.getReceivedChunks() + 1);
		}

		return new ApiResponse<>(true, "청크 업로드 성공", Map.of(
				"fileId", fileId,
				"progress", (int)((double) uploadInfo.getReceivedChunks() / (double) totalChunks* 100)
		));
	}

	// 청크 업로드 완료 처리 API
	@Operation(summary = "Complete chunked upload")
	@PostMapping("/videos/complete")
	@RequiresControllerAuthentication
	public ApiResponse<UploadVideoResponse> completeUpload(
		@RequestBody Map<String, String> request,
		@AuthenticationPrincipal User user
	) throws IOException {
		String fileId = request.get("fileId");
		String title = request.get("title");

		// 업로드 정보 확인
		ChunkUploadInfo uploadInfo = UPLOAD_INFO_MAP.get(fileId);
		if (uploadInfo == null) {
			return new ApiResponse<>(false, "유효하지 않은 업로드 ID입니다.", null);
		}

		// 모든 청크가 수신되었는지 확인
		if (uploadInfo.getReceivedChunks() != uploadInfo.getTotalChunks()) {
			return new ApiResponse<>(false, "모든 청크가 수신되지 않았습니다.", null);
		}

		// 임시 파일 경로
		String tempDir = fileStorageProperties.getUploadDir() + "/temp/";
		String tempFilePath = tempDir + File.separator + fileId + ".part";

		// 비디오 정보 저장
		VideoInfo videoInfo = uploadVideoService.saveVideoFromChunks(
			uploadInfo.getFileName(),
			title,
			uploadInfo.getUser()
		);
		// 최종 파일 경로
		String fileFullPath = fileStorageProperties.getUploadDir() + File.separator + videoInfo.getFileName();
		// 임시 파일을 취종 위치로 이동
		Files.move(Paths.get(tempFilePath), Paths.get(fileFullPath));

		UPLOAD_INFO_MAP.remove(fileId);

		return new ApiResponse<>(true, "비디오 등록 성공", videoMapper.toSimpleResponse(videoInfo));
	}


	@Operation(summary = "Upload thumbnail image")
	@PostMapping(value = "/thumbnails", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ApiResponse<UploadImageResponse> createImage(@RequestParam("file") MultipartFile file, @RequestParam("videoId") String videoId) {
		String fileName = uploadVideoService.uploadImage(file, videoId);
		return new ApiResponse<>(true, "썸네일 등록 성공", UploadImageResponse.builder().fileName(fileName).build());
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

				// 파일명 인코딩 처리
				String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
					.replace("+", "%20");  // 공백 처리

				// 응답 헤더 설정
				HttpHeaders headers = new HttpHeaders();
				headers.setContentDisposition(ContentDisposition.builder("inline")
					.filename(encodedFileName, StandardCharsets.UTF_8)  // UTF-8 인코딩 적용
					.build());
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
