package com.example.video.dto;

import java.util.Optional;

import com.example.video.entity.Video;

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

    public VideoInfo (Optional<Video> video){
        this.videoId = video.get().getVideoId();
        this.title = video.get().getTitle();
        this.src = video.get().getSrc();
        this.description = video.get().getDescription();
        this.cnt = video.get().getCnt();
        this.channelTitle = video.get().getChannelTitle();
    }

}
