package com.example.video.controller.video;

import com.example.global.config.FileStorageProperties;
import com.example.video.dto.video.UploadVideoResponse;
import com.example.video.dto.video.VideoInfo;
import com.example.video.dto.video.VideoMapper;
import com.example.video.exception.FileUploadException;
import com.example.video.service.video.UploadVideoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UploadVideoController.class)
class UploadVideoControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private UploadVideoService uploadVideoService;

	@MockBean
	private FileStorageProperties fileStorageProperties;

	@MockBean
	private VideoMapper videoMapper;

	private MockMultipartFile videoFile;
	private MockMultipartFile imageFile;
	private VideoInfo videoInfo;
	private UploadVideoResponse uploadVideoResponse;

	@BeforeEach
	void setUp() {
		// 테스트용 비디오 파일
		videoFile = new MockMultipartFile(
			"file",
			"test.mp4",
			"video/mp4",
			"test video content".getBytes()
		);

		// 테스트용 이미지 파일
		imageFile = new MockMultipartFile(
			"file",
			"thumbnail.jpeg",
			"image/jpeg",
			"test image content".getBytes()
		);

		// 테스트용 VideoInfo
		videoInfo = VideoInfo.builder()
			.videoId("test-video-id")
			.fileName("test.mp4")
			.title("Test Video")
			.build();

		// 테스트용 UploadVideoResponse
		uploadVideoResponse = UploadVideoResponse.builder()
			.videoId("test-video-id")
			.fileName("test.mp4")
			.build();
	}

	@Test
	@DisplayName("비디오 업로드 성공")
	void createVideo_Success() throws Exception {
		// given
		given(uploadVideoService.uploadVideo(any(), eq("Test Video")))
			.willReturn(videoInfo);
		given(videoMapper.toSimpleResponse(any(VideoInfo.class)))
			.willReturn(uploadVideoResponse);

		// when & then
		mockMvc.perform(multipart("/api/v1/upload/videos")
				.file(videoFile)
				.param("title", "Test Video"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.message").value("비디오 등록 성공"))
			.andExpect(jsonPath("$.data.videoId").value("test-video-id"))
			.andExpect(jsonPath("$.data.fileName").value("test.mp4"))
			.andDo(print());
	}

	@Test
	@DisplayName("썸네일 업로드 성공")
	void createImage_Success() throws Exception {
		// given
		given(uploadVideoService.uploadImage(any(), eq("test-video-id")))
			.willReturn("thumbnail.jpeg");

		// when & then
		mockMvc.perform(multipart("/api/v1/upload/thumbnails")
				.file(imageFile)
				.param("videoId", "test-video-id"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.message").value("썸네일 등록 성공"))
			.andExpect(jsonPath("$.data.fileName").value("thumbnail.jpeg"))
			.andDo(print());
	}

	@Test
	@DisplayName("파일 업로드 실패 - 파일 누락")
	void createVideo_Failure_NoFile() throws Exception {
		mockMvc.perform(multipart("/api/v1/upload/videos")
				.param("title", "Test Video"))
			.andExpect(status().isBadRequest())
			.andDo(print());
	}

	@Test
	@DisplayName("파일 업로드 실패 - 서비스 예외")
	void createVideo_Failure_ServiceException() throws Exception {
		// given
		given(uploadVideoService.uploadVideo(any(), any()))
			.willThrow(new FileUploadException("파일 업로드 실패"));

		// when & then
		mockMvc.perform(multipart("/api/v1/upload/videos")
				.file(videoFile)
				.param("title", "Test Video"))
			.andExpect(status().isBadRequest())
			.andDo(print());
	}

	@Test
	@DisplayName("존재하지 않는 파일 조회")
	void getVideo_FileNotFound() throws Exception {
		// given
		given(fileStorageProperties.getUploadDir())
			.willReturn("upload-dir");

		// when & then
		mockMvc.perform(get("/api/v1/upload")
				.param("fileName", "nonexistent.mp4"))
			.andExpect(status().isNotFound())
			.andDo(print());
	}
}