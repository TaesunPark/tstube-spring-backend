package com.example.video.repository.video;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.example.video.entity.video.Thumbnail;
import com.example.video.entity.video.Video;

@DataJpaTest
class VideoRepositoryTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private VideoRepository videoRepository;

	@Test
	@DisplayName("모든 필드가 포함된 비디오를 저장하고 조회할 수 있다")
	void saveAndFindCompleteVideo() {
		// given
		LocalDateTime now = LocalDateTime.now();
		Thumbnail thumbnail = Thumbnail.builder()
			.url("http://example.com/thumbnail.jpg")
			.build();

		Video video = Video.builder()
			.videoId("test-video-123")
			.title("Test Video Title")
			.src("http://example.com/video.mp4")
			.description("Test video description")
			.cnt(0L)
			.channelTitle("Test Channel")
			.type("MP4")
			.fileName("video.mp4")
			.createTime(now)
			.updateTime(now)
			.thumbnail(thumbnail)
			.build();

		// when
		Video savedVideo = videoRepository.save(video);
		entityManager.flush();
		entityManager.clear();

		Video foundVideo = videoRepository.findById(savedVideo.getId()).orElse(null);

		// then
		assertThat(foundVideo).isNotNull();
		assertThat(foundVideo)
			.satisfies(v -> {
				assertThat(v.getVideoId()).isEqualTo("test-video-123");
				assertThat(v.getTitle()).isEqualTo("Test Video Title");
				assertThat(v.getSrc()).isEqualTo("http://example.com/video.mp4");
				assertThat(v.getDescription()).isEqualTo("Test video description");
				assertThat(v.getCnt()).isEqualTo(0L);
				assertThat(v.getChannelTitle()).isEqualTo("Test Channel");
				assertThat(v.getType()).isEqualTo("MP4");
				assertThat(v.getFileName()).isEqualTo("video.mp4");
				assertThat(v.getCreateTime()).isEqualToIgnoringNanos(now);
				assertThat(v.getUpdateTime()).isEqualToIgnoringNanos(now);
				assertThat(v.getThumbnail()).isNotNull();
				assertThat(v.getThumbnail().getUrl()).isEqualTo("http://example.com/thumbnail.jpg");
			});
	}

	@Test
	@DisplayName("videoId로 비디오와 썸네일을 함께 조회할 수 있다")
	void findByVideoIdWithThumbnail() {
		// given
		Thumbnail thumbnail = Thumbnail.builder()
			.url("http://example.com/thumbnail.jpg")
			.build();

		Video video = Video.builder()
			.videoId("custom-video-id")
			.title("Test Video")
			.src("http://example.com/video.mp4")
			.thumbnail(thumbnail)
			.createTime(LocalDateTime.now())
			.updateTime(LocalDateTime.now())
			.build();

		entityManager.persist(video);
		entityManager.flush();
		entityManager.clear();

		// when
		Optional<Video> found = videoRepository.findByVideoId("custom-video-id");

		// then
		assertThat(found).isPresent();
		assertThat(found.get().getThumbnail()).isNotNull();
		assertThat(found.get().getThumbnail().getUrl()).isEqualTo("http://example.com/thumbnail.jpg");
	}

	@Test
	@DisplayName("비디오를 삭제하면 연관된 썸네일도 함께 삭제된다")
	void deleteVideoWithThumbnail() {
		// given
		Thumbnail thumbnail = Thumbnail.builder()
			.url("http://example.com/thumbnail.jpg")
			.build();

		Video video = Video.builder()
			.videoId("video-to-delete")
			.title("Test Video")
			.src("http://example.com/video.mp4")
			.thumbnail(thumbnail)
			.createTime(LocalDateTime.now())
			.updateTime(LocalDateTime.now())
			.build();

		Video savedVideo = videoRepository.save(video);
		UUID thumbnailId = savedVideo.getThumbnail().getId(); // Thumbnail 엔티티에 ID 필드가 있다고 가정

		// when
		videoRepository.deleteById(savedVideo.getId());
		entityManager.flush();
		entityManager.clear();

		// then
		assertThat(videoRepository.findById(savedVideo.getId())).isEmpty();
		assertThat(entityManager.find(Thumbnail.class, thumbnailId)).isNull();
	}

	@Test
	@DisplayName("조회수를 증가시킬 수 있다")
	void incrementViewCount() {
		// given
		Video video = Video.builder()
			.videoId("view-count-test")
			.title("Test Video")
			.src("http://example.com/video.mp4")
			.cnt(0L)
			.createTime(LocalDateTime.now())
			.updateTime(LocalDateTime.now())
			.build();

		Video savedVideo = videoRepository.save(video);

		// when
		savedVideo.setCnt(savedVideo.getCnt() + 1);
		videoRepository.save(savedVideo);
		entityManager.flush();
		entityManager.clear();

		// then
		Video foundVideo = videoRepository.findById(savedVideo.getId()).orElse(null);
		assertThat(foundVideo).isNotNull();
		assertThat(foundVideo.getCnt()).isEqualTo(1L);
	}

}