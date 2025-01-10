package com.example.video.service.video;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.global.config.FileStorageProperties;
import com.example.video.dto.video.CreateVideoRequestDto;
import com.example.video.dto.video.UploadVideoInfo;
import com.example.video.dto.video.VideoInfo;

import lombok.RequiredArgsConstructor;
import lombok.Value;

@Service
public class UploadVideoService {

	private FileStorageProperties fileStorageProperties;
	private VideoService videoService;

	public UploadVideoService(FileStorageProperties fileStorageProperties, VideoService videoService) {
		this.fileStorageProperties = fileStorageProperties;
		this.videoService = videoService;
	}

	public String uploadVideo(MultipartFile file, String title) throws IOException {
		Path uploadPath = Paths.get(fileStorageProperties.getUploadDir());
		if (!Files.exists(uploadPath)) {
			Files.createDirectories(uploadPath);
		}
		// Store the file
		String fileName = file.getOriginalFilename();
		// 여기서 video 추가 세팅
		CreateVideoRequestDto createVideoRequestDto = new CreateVideoRequestDto();
		createVideoRequestDto.setType("upload");
		createVideoRequestDto.setTitle(title);
		createVideoRequestDto.setSrc(fileStorageProperties.getUploadDir());
		createVideoRequestDto.setFileName(fileName);
		VideoInfo videoInfo = videoService.createVideo(createVideoRequestDto);
		Path filePath = uploadPath.resolve(fileName != null ? videoInfo.getFileName() : "?");
		file.transferTo(filePath);
		return fileName; // Return the file name or path for future use
	}

	public File getVideo(String fileName) throws IOException {
		Path filePath = Paths.get(fileStorageProperties.getUploadDir()).resolve(fileName);
		if (!Files.exists(filePath)) {
			throw new IOException("File not found: ".concat(fileName));
		}
		// UploadVideoInfo uploadVideoInfo = new UploadVideoInfo();
		// uploadVideoInfo.setVideoFile(filePath.toFile());
		// uploadVideoInfo.setFileName(fileName);
		return filePath.toFile();
	}

}
