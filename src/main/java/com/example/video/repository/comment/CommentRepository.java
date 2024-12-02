package com.example.video.repository.comment;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.video.entity.comment.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {
	List<Comment> findCommentsByVideoId (String videoId);
}
