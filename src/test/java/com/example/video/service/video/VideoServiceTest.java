package com.example.video.service.video;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.video.dto.video.CreateVideoRequestDto;
import com.example.video.dto.video.VideoInfo;
import com.example.video.entity.video.Thumbnail;
import com.example.video.entity.video.Video;
import com.example.video.exception.NotFoundException;
import com.example.video.repository.video.VideoRepository;

@ExtendWith(MockitoExtension.class)
class VideoServiceTest {
	@InjectMocks
	private VideoService videoService;

	@Mock
	private VideoRepository videoRepository;

	private Video sampleVideo;
	private CreateVideoRequestDto createVideoRequestDto;
	private Thumbnail sampleThumbnail;

	@BeforeEach
	void setUp() {
		sampleThumbnail = Thumbnail.builder()
			.id(UUID.randomUUID())  // 랜덤 UUID 생성
			.url("https://example.com/thumbnail.jpg")
			.createdAt(LocalDateTime.now())  // 현재 시간
			.updatedAt(LocalDateTime.now())
			.build();

		sampleVideo = Video.builder()
			.videoId("video123")
			.title("Sample Video Title")
			.src("https://example.com/videos/sample.mp4")
			.description("This is a sample video description")
			.cnt(100L)
			.channelTitle("Test Channel")
			.createTime(LocalDateTime.now())
			.updateTime(LocalDateTime.now())
			.type("iframe")
			.fileName("sample.mp4")
			.thumbnail(sampleThumbnail)
			.build();

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
	@DisplayName("모든 비디오를 성공적으로 조회한다")  // 테스트 설명
	void getAllVideo_ShouldReturnAllVideos() {
		// given - 테스트 준비
		given(videoRepository.findAll()).willReturn(List.of(sampleVideo));
		// videoRepository.findAll() 호출시 sampleVideo 리스트 반환하도록 설정

		// when - 테스트 실행
		List<VideoInfo> result = videoService.getAllVideo();
		// 실제 메소드 호출

		// then - 결과 검증
		assertThat(result).hasSize(1);  // 결과 리스트 크기가 1인지 확인
		assertThat(result.get(0).getVideoId()).isEqualTo("video123");  // videoId 확인
		assertThat(result.get(0).getTitle()).isEqualTo("Sample Video Title");  // title 확인
		verify(videoRepository).findAll();  // findAll() 메소드가 실제로 호출됐는지 확인
	}

	@Test
	@DisplayName("새로운 비디오를 성공적으로 생성한다")
	void createVideo_ShouldCreateNewVideo() {
		// given
		given(videoRepository.save(any(Video.class))).willReturn(sampleVideo);

		// when
		VideoInfo result = videoService.createVideo(createVideoRequestDto);

		// then
		assertThat(result).isNotNull();
		assertThat(result.getTitle()).isEqualTo("Sample Video Title");
		assertThat(result.getVideoId()).isEqualTo("video123");
		verify(videoRepository).save(any(Video.class));
	}

	@Test
	@DisplayName("업로드 타입 비디오의 파일명이 올바르게 설정된다")
	void createVideo_WithUploadType_ShouldSetCorrectFileName() {
		// given
		CreateVideoRequestDto uploadRequest = createVideoRequestDto.toBuilder()
			.type("upload")
			.build();

		given(videoRepository.save(any(Video.class))).willReturn(sampleVideo);

		// when
		VideoInfo result = videoService.createVideo(uploadRequest);

		// then
		assertThat(result).isNotNull();
		assertThat(result.getFileName()).endsWith(".mp4");
		verify(videoRepository).save(any(Video.class));
	}

	@Test
	@DisplayName("비디오 ID로 특정 비디오를 성공적으로 조회한다")
	void getVideoByVideoId_ShouldReturnVideo() {
		// given
		String videoId = "video123";
		given(videoRepository.findByVideoId(videoId)).willReturn(Optional.of(sampleVideo));

		// when
		VideoInfo result = videoService.getVideoByVideoId(videoId);

		// then
		assertThat(result).isNotNull();
		assertThat(result.getVideoId()).isEqualTo(videoId);
		verify(videoRepository).findByVideoId(videoId);
	}

	@Test
	@DisplayName("존재하지 않는 비디오 ID로 조회 시 예외가 발생한다")
	void getVideoByVideoId_WithNonExistentId_ShouldThrowException() {
		// given
		String nonExistentId = "nonexistent123";
		given(videoRepository.findByVideoId(nonExistentId)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> videoService.getVideoByVideoId(nonExistentId))
			.isInstanceOf(NotFoundException.class)
			.hasMessageContaining(nonExistentId);
	}

	@Test
	@DisplayName("생성된 UUID가 유니크한지 확인한다")
	void createUUID_ShouldGenerateUniqueValues() {
		// when
		String uuid1 = videoService.createUUID();
		String uuid2 = videoService.createUUID();

		// then
		assertThat(uuid1).isNotEqualTo(uuid2);
	}

	@Test
	@DisplayName("잘못된 type으로 비디오 생성 시 실패한다")
	void createVideo_WithInvalidType_ShouldFail() {
		// given
		CreateVideoRequestDto invalidRequest = createVideoRequestDto.toBuilder()
			.type("invalid_type")
			.build();

		// when & then
		assertThatThrownBy(() -> videoService.createVideo(invalidRequest))
			.isInstanceOf(IllegalArgumentException.class);
	}

}