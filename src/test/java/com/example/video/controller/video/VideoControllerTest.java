package com.example.video.controller.video;

import static org.mockito.BDDMockito.*;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.hibernate.service.spi.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.global.config.FileStorageProperties;
import com.example.video.dto.video.CreateVideoRequestDto;
import com.example.video.dto.video.VideoInfo;
import com.example.video.dto.video.VideoMapper;
import com.example.video.dto.video.VideoResponse;
import com.example.video.entity.video.Thumbnail;
import com.example.video.exception.NotFoundException;
import com.example.video.service.video.VideoService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(VideoController.class)
@AutoConfigureMockMvc
class VideoControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private VideoService videoService;

	@MockBean
	private VideoMapper videoMapper;

	@MockBean
	private FileStorageProperties fileStorageProperties;

	@Autowired
	private ObjectMapper objectMapper;

	private VideoInfo sampleVideoInfo;
	private VideoResponse sampleVideoResponse;
	private CreateVideoRequestDto createVideoRequestDto;
	private Thumbnail sampleThumbnail;

	@BeforeEach
	void setUp() {
		// 테스트용 썸네일 데이터 생성
		sampleThumbnail = Thumbnail.builder()
			.id(UUID.randomUUID())
			.url("https://example.com/thumbnail.jpg")
			.createdAt(LocalDateTime.now())
			.updatedAt(LocalDateTime.now())
			.build();

		// 테스트용 VideoInfo 데이터 생성
		sampleVideoInfo = VideoInfo.builder()
			.videoId("video123")
			.title("Sample Video Title")
			.src("https://example.com/videos/sample.mp4")
			.description("This is a sample video description")
			.cnt(100)
			.channelTitle("Test Channel")
			.createTime(LocalDateTime.now())
			.updateTime(LocalDateTime.now())
			.type("iframe")
			.fileName("sample.mp4")
			.thumbnail(sampleThumbnail)
			.build();

		// 테스트용 VideoResponse 데이터 생성
		sampleVideoResponse = VideoResponse.builder()
			.videoId("video123")
			.title("Sample Video Title")
			.src("https://example.com/videos/sample.mp4")
			.description("This is a sample video description")
			.cnt(100)
			.channelTitle("Test Channel")
			.createTime(sampleVideoInfo.getCreateTime())
			.updateTime(sampleVideoInfo.getUpdateTime())
			.type("iframe")
			.fileName("sample.mp4")
			.thumbnailUrl(sampleThumbnail.getUrl())
			.build();

		// 테스트용 CreateVideoRequestDto 데이터 생성
		createVideoRequestDto = CreateVideoRequestDto.builder()
			.title("New Video Title")
			.description("New video description")
			.src("https://example.com/videos/new.mp4")
			.channelTitle("New Channel")
			.type("iframe")
			.fileName("new.mp4")
			.build();
	}

	@Test
	@DisplayName("새로운 비디오를 성공적으로 생성한다")
	void createVideo_ShouldCreateNewVideo() throws Exception {
		// given
		given(videoService.createVideo(any(CreateVideoRequestDto.class))).willReturn(sampleVideoInfo);
		given(videoMapper.toResponse(sampleVideoInfo)).willReturn(sampleVideoResponse);

		// when & then
		mockMvc.perform(post("/api/v1/videos")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createVideoRequestDto)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.message").value("video 생성 성공"))
			.andExpect(jsonPath("$.data.videoId").exists())
			.andExpect(jsonPath("$.data.title").value("Sample Video Title"))
			.andExpect(jsonPath("$.data.src").value("https://example.com/videos/sample.mp4"))
			.andExpect(jsonPath("$.data.description").value("This is a sample video description"))
			.andExpect(jsonPath("$.data.channelTitle").value("Test Channel"))
			.andExpect(jsonPath("$.data.type").value("iframe"))
			.andExpect(jsonPath("$.data.fileName").value("sample.mp4"));
	}

	@Test
	@DisplayName("비디오 ID로 특정 비디오를 성공적으로 조회한다")
	void getVideo_ShouldReturnSpecificVideo() throws Exception {
		// given
		String videoId = "video123";
		given(videoService.getVideoByVideoId(videoId)).willReturn(sampleVideoInfo);
		given(videoMapper.toResponse(sampleVideoInfo)).willReturn(sampleVideoResponse);

		// when & then
		mockMvc.perform(get("/api/v1/videos/{videoId}", videoId)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.message").value(videoId))
			.andExpect(jsonPath("$.data.videoId").value("video123"))
			.andExpect(jsonPath("$.data.title").value("Sample Video Title"))
			.andExpect(jsonPath("$.data.src").value("https://example.com/videos/sample.mp4"))
			.andExpect(jsonPath("$.data.description").value("This is a sample video description"))
			.andExpect(jsonPath("$.data.fileName").value("sample.mp4"))
			.andExpect(jsonPath("$.data.cnt").value(100))
			.andExpect(jsonPath("$.data.type").value("iframe"))
			.andExpect(jsonPath("$.data.channelTitle").value("Test Channel"))
			.andExpect(jsonPath("$.data.createTime").exists())
			.andExpect(jsonPath("$.data.updateTime").exists())
			.andExpect(jsonPath("$.data.thumbnailUrl").value("https://example.com/thumbnail.jpg"));
	}

	@Test
	@DisplayName("모든 비디오 목록을 성공적으로 조회힌다.")
	void getVideos_ShouldReturnAllVideos() throws Exception {
		// given
		List<VideoInfo> videoInfoList = List.of(sampleVideoInfo);
		List<VideoResponse> videoResponseList = List.of(sampleVideoResponse);

		given(videoService.getAllVideo()).willReturn(videoInfoList);
		given(videoMapper.toResponseList(videoInfoList)).willReturn(videoResponseList);

		// when & then
		mockMvc.perform(get("/api/v1/videos")
			.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.message").value("Videos retrieved successfully"))
			.andExpect(jsonPath("$.data[0].videoId").value("video123"))
			.andExpect(jsonPath("$.data[0].title").value("Sample Video Title"))
			.andExpect(jsonPath("$.data[0].channelTitle").value("Test Channel"))
			.andExpect(jsonPath("$.data[0].type").value("iframe"))
			.andExpect(jsonPath("$.data[0].fileName").value("sample.mp4"))
			.andExpect(jsonPath("$.data[0].thumbnailUrl").value("https://example.com/thumbnail.jpg"));
	}

	@Test
	@DisplayName("필수 필드가 누락된 비디오 생성 요청은 실패한다 400 발생, Valid에서 오류 발생")
	void createVideo_WithMissingRequiredFields_ShouldFail() throws Exception {
		// given
		CreateVideoRequestDto invalidRequest = CreateVideoRequestDto.builder()
			.title("")  // 빈 제목
			.src(null)  // src 누락
			.type("")   // 빈 타입
			.build();

		// when & then
		mockMvc.perform(post("/api/v1/videos")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(invalidRequest)))
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("존재하지 않는 비디오 ID로 조회 시 404 응답을 반환한다")
	void getVideo_WithNonExistentId_ShouldReturn404() throws Exception {
		// given
		String nonExistentId = "nonExistentId";
		given(videoService.getVideoByVideoId(nonExistentId))
			.willThrow(new NotFoundException("Video not found with id: ".concat(nonExistentId)));

		// when & then
		mockMvc.perform(get("/api/v1/videos/{videoId}", nonExistentId)
			.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.message").value("Video not found with id: ".concat(nonExistentId)));
	}

	@Test
	@DisplayName("유효하지 않은 비디오 타입으로 생성 요청 시 실패한다")
	void createVideo_WithInvalidType_ShouldFail() throws Exception {
		// given
		CreateVideoRequestDto invalidRequest = createVideoRequestDto.toBuilder()
			.type("INVALID_TYPE")  // 허용되지 않는 타입
			.build();

		// when & then
		mockMvc.perform(post("/api/v1/videos")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(invalidRequest)))
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("서비스 레이어에서 예외 발생 시 500 응답을 반환한다")
	void createVideo_WhenServiceThrowsException_ShouldFail() throws Exception {
		// given
		given(videoService.createVideo(any(CreateVideoRequestDto.class)))
			.willThrow(new RuntimeException("Internal Server Error"));

		// when & then
		mockMvc.perform(post("/api/v1/videos")
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(createVideoRequestDto)))
			.andExpect(status().isInternalServerError())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.message").value("Internal Server Error"));
	}

	@Test
	@DisplayName("서비스 비동기 처리 중 예외 발생 시 500 응답을 반환한다")
	void createVideo_WhenAsyncProcessingFails_ShouldReturn500() throws Exception {
		// given
		given(videoService.createVideo(any(CreateVideoRequestDto.class)))
			.willThrow(new ServiceException("Async processing failed"));

		// when & then
		mockMvc.perform(post("/api/v1/videos")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createVideoRequestDto)))
			.andExpect(status().isInternalServerError())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.message").value("Async processing failed"));
	}

}