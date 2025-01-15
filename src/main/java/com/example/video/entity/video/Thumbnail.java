package com.example.video.entity.video;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "thumbnail")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Thumbnail {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false)
	private String url; // 썸네일 URL

	@OneToOne(mappedBy = "thumbnail", cascade = CascadeType.ALL)
	private Video video;

	private LocalDateTime createdAt;

	private LocalDateTime updatedAt;
}