package com.example.video.service.video;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import com.example.global.config.FileStorageProperties;
import com.example.video.dto.video.CreateVideoRequestDto;
import com.example.video.dto.video.VideoInfo;
import com.example.video.entity.video.Thumbnail;
import com.example.video.entity.video.Video;
import com.example.video.exception.FileUploadException;
import com.example.video.repository.video.ThumbnailRepository;
import com.example.video.repository.video.VideoRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UploadVideoServiceTest {

	@InjectMocks
	private UploadVideoService uploadVideoService;

	@Mock
	private FileStorageProperties fileStorageProperties;

	@Mock
	private VideoService videoService;

	@Mock
	private ThumbnailRepository thumbnailRepository;

	@Mock
	private VideoRepository videoRepository;

	private MockMultipartFile videoFile;
	private MockMultipartFile imageFile;
	private String testVideoId;
	private Path tempVideoDir;
	private Path tempImageDir;

	@BeforeEach
	void setUp() throws Exception {
		// 임시 디렉토리 생성
		tempVideoDir = Files.createTempDirectory("upload-files-");
		tempImageDir = Files.createTempDirectory("upload-images-");
		lenient().when(fileStorageProperties.getUploadDir()).thenReturn(tempVideoDir.toString());
		lenient().when(fileStorageProperties.getUploadImageDir()).thenReturn(tempImageDir.toString());

		videoFile = new MockMultipartFile(
			"file",
			"test-video.mp4",
			"video/mp4",
			"test video content".getBytes()
		);

		imageFile = new MockMultipartFile(
			"file",
			"test-thumbnail.jpg",
			"image/jpeg",
			"test image content".getBytes()
		);

		testVideoId = "test-video-id";
	}

	@Test
	@DisplayName("비디오 업로드 성공")
	void uploadVideo_Success() throws IOException {
		// given
		VideoInfo expectedVideoInfo = VideoInfo.builder()
			.videoId(testVideoId)
			.fileName("test-video.mp4")
			.title("Test Video")
			.build();

		given(videoService.createVideo(any(CreateVideoRequestDto.class)))
			.willReturn(expectedVideoInfo);

		// when
		VideoInfo result = uploadVideoService.uploadVideo(videoFile, "Test Video");

		// then
		assertThat(result).isNotNull();
		assertThat(result.getVideoId()).isEqualTo(testVideoId);
		assertThat(result.getFileName()).isEqualTo("test-video.mp4");
		verify(videoService).createVideo(any(CreateVideoRequestDto.class));
	}

	@Test
	@DisplayName("빈 파일 업로드 시 예외 발생")
	void uploadVideo_EmptyFile_ThrowsException() {
		// given
		MockMultipartFile emptyFile = new MockMultipartFile(
			"file",
			"empty.mp4",
			"video/mp4",
			new byte[0]
		);

		// when & then
		assertThatThrownBy(() -> uploadVideoService.uploadVideo(emptyFile, "Test Video"))
			.isInstanceOf(FileUploadException.class)
			.hasMessage("파일이 비어있습니다.");
	}

	@Test
	@DisplayName("썸네일 업로드 성공")
	void uploadImage_Success() throws IOException {
		// given
		Video video = Video.builder()
			.videoId(testVideoId)
			.title("Test Video")
			.build();

		Thumbnail thumbnail = Thumbnail.builder()
			.url(tempImageDir.resolve("test-thumbnail.jpg").toString())
			.video(video)
			.createdAt(LocalDateTime.now())
			.updatedAt(LocalDateTime.now())
			.build();

		given(videoRepository.findByVideoId(testVideoId))
			.willReturn(Optional.of(video));
		given(thumbnailRepository.save(any(Thumbnail.class)))
			.willReturn(thumbnail);

		// when
		String result = uploadVideoService.uploadImage(imageFile, testVideoId);

		// then
		assertThat(result).isNotNull();
		assertThat(result).contains("test-thumbnail.jpg");
		verify(thumbnailRepository).save(any(Thumbnail.class));
		verify(videoRepository).save(any(Video.class));
	}

	@Test
	@DisplayName("존재하지 않는 비디오에 대한 썸네일 업로드 시 예외 발생")
	void uploadImage_VideoNotFound_ThrowsException() {
		// given
		given(videoRepository.findByVideoId(testVideoId))
			.willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> uploadVideoService.uploadImage(imageFile, testVideoId))
			.isInstanceOf(FileUploadException.class)
			.hasMessageContaining("Video not found");
	}

	@Test
	@DisplayName("파일 이름이 null인 경우 unknown으로 처리")
	void uploadVideo_NullFileName_UsesUnknown() throws IOException {
		// given
		MockMultipartFile fileWithNullName = new MockMultipartFile(
			"file",
			null,
			"video/mp4",
			"test content".getBytes()
		);

		VideoInfo expectedVideoInfo = VideoInfo.builder()
			.videoId(testVideoId)
			.fileName("unknown")
			.title("Test Video")
			.build();

		given(videoService.createVideo(any(CreateVideoRequestDto.class)))
			.willReturn(expectedVideoInfo);

		// when
		VideoInfo result = uploadVideoService.uploadVideo(fileWithNullName, "Test Video");

		// then
		assertThat(result).isNotNull();
		assertThat(result.getFileName()).isEqualTo("unknown");
	}

	@Test
	@DisplayName("비디오 정보 생성 실패 시 파일 삭제")
	void uploadVideo_FailedVideoInfo_DeletesFile() {
		// given
		given(videoService.createVideo(any(CreateVideoRequestDto.class)))
			.willThrow(new RuntimeException("비디오 정보 생성 실패"));

		// when & then
		assertThatThrownBy(() -> uploadVideoService.uploadVideo(videoFile, "Test Video"))
			.isInstanceOf(FileUploadException.class)
			.hasMessage("비디오 정보 생성에 실패했습니다.");

		assertThat(Files.exists(tempVideoDir.resolve("test-video.mp4"))).isFalse();
	}

}