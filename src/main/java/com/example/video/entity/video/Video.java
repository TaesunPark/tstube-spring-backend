package com.example.video.entity.video;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.*;

@Entity@Table(name = "video", indexes = {
    @Index(name = "idx_video_id", columnList = "videoId")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Video {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    // 랜덤 값
    private String videoId;
    private String title;
    @Column(name = "src", nullable = false, length = 512)
    private String src;
    private String description;
    private long cnt;
    private String channelTitle;
    private String type;
    private String fileName;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
