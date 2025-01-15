package com.example.video.dto.video;

import java.time.LocalDateTime;
import java.util.Optional;

import com.example.video.entity.video.Thumbnail;
import com.example.video.entity.video.Video;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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

    public VideoInfo (Optional<Video> video){
        this.videoId = video.get().getVideoId();
        this.title = video.get().getTitle();
        this.src = video.get().getSrc();
        this.description = video.get().getDescription();
        this.cnt = video.get().getCnt();
        this.channelTitle = video.get().getChannelTitle();
        this.createTime = video.get().getCreateTime();
        this.updateTime = video.get().getUpdateTime();
        this.type = video.get().getType();
        this.fileName = video.get().getFileName();
        this.thumbnail = video.get().getThumbnail();
    }

}
