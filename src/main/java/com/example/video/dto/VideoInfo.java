package com.example.video.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class VideoInfo {
    private Long id;
    private String title;
    private String src;
    private String description;
    private long cnt;
    private String channelTitle;
}
