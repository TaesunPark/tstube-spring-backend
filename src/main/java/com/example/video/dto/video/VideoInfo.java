package com.example.video.dto.video;

import java.time.LocalDateTime;
import java.util.Optional;

import com.example.video.entity.video.Thumbnail;
import com.example.video.entity.video.Video;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class VideoInfo {
    private String videoId;
    private String title;
    private String src;
    private String description;
    private long cnt;
    private String channelTitle;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String type;
    private String fileName;
    private Thumbnail thumbnail;
    private boolean isFavorite;

    public VideoInfo (Video video){
        this.videoId = video.getVideoId();
        this.title = video.getTitle();
        this.src = video.getSrc();
        this.description = video.getDescription();
        this.cnt = video.getCnt();
        this.channelTitle = video.getChannelTitle();
        this.createTime = video.getCreateTime();
        this.updateTime = video.getUpdateTime();
        this.type = video.getType();
        this.fileName = video.getFileName();
        this.thumbnail = video.getThumbnail();
    }

    public VideoInfo (Video video, boolean isFavorite){
        this.videoId = video.getVideoId();
        this.title = video.getTitle();
        this.src = video.getSrc();
        this.description = video.getDescription();
        this.cnt = video.getCnt();
        this.channelTitle = video.getChannelTitle();
        this.createTime = video.getCreateTime();
        this.updateTime = video.getUpdateTime();
        this.type = video.getType();
        this.fileName = video.getFileName();
        this.thumbnail = video.getThumbnail();
        this.isFavorite = isFavorite;
    }


}
