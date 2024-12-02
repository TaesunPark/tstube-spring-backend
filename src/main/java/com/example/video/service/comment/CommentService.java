package com.example.video.service.comment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.video.dto.comment.CommentInfo;
import com.example.video.dto.comment.CommentRequestDto;
import com.example.video.dto.video.CreateVideoRequestDto;
import com.example.video.entity.comment.Comment;
import com.example.video.repository.comment.CommentRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RequiredArgsConstructor
@Service
public class CommentService {
	private final CommentRepository commentRepository;

	@Transactional
	public List<CommentInfo> getCommentInfoList(String videoId) {
		List<Comment> comments = commentRepository.findCommentsByVideoId(videoId);
		return comments.stream().map(comment -> new CommentInfo(comment.getVideoId(), comment.getContent(), comment.getCreatedAt())).toList();
	}

	@Transactional
	public CommentInfo getCommentInfo(String videoId, String commentId) {
		return null;
	}

	@Transactional
	public CommentInfo createComment(CommentRequestDto commentRequestDto) {

		Comment comment = commentRepository.save(Comment.builder()
			.content(commentRequestDto.getComment())
			.videoId(commentRequestDto.getVideoId())
			.createdAt(LocalDateTime.now())
			.updatedAt(LocalDateTime.now())
			.build());

		return new CommentInfo(comment.getVideoId(), comment.getContent(), comment.getCreatedAt());
	}



}
