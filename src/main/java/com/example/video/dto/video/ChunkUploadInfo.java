package com.example.video.dto.video;

import lombok.Data;
import com.example.user.entity.User;

/**
 * 청크 업로드 정보를 저장하는 내부 클래스
 */
@Data
public class ChunkUploadInfo {
	private final String fileName;
	private final long fileSize;
	private final int totalChunks;
	private final String title;
	private final User user;
	private int receivedChunks = 0;

	public ChunkUploadInfo(String fileName, long fileSize, int totalChunks, String title, User user) {
		this.fileName = fileName;
		this.fileSize = fileSize;
		this.totalChunks = totalChunks;
		this.title = title;
		this.user = user;
	}
}
