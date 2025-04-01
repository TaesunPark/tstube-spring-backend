package com.example.video.exception;

import java.io.IOException;

import org.apache.catalina.connector.ClientAbortException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import com.example.video.response.ApiResponse;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	// 비디오 스트리밍 예외를 위한 핸들러
	@ExceptionHandler({ClientAbortException.class, IOException.class})
	public ResponseEntity<byte[]> handleVideoStreamingException(Exception e, HttpServletRequest request) {
		// 비디오 스트리밍 오류 로깅
		log.debug("비디오 스트리밍 중 예외 발생: {}", e.getMessage());

		// Content-Type 판단
		String requestURI = request.getRequestURI();
		String fileName = request.getParameter("fileName");

		// 비디오 요청인 경우
		if (requestURI.contains("/api/v1/upload") &&
			fileName != null &&
			fileName.endsWith(".mp4")) {

			return ResponseEntity.status(HttpStatus.NO_CONTENT)
				.contentType(MediaType.APPLICATION_JSON)
				.body(null);
		}

		// 기본 응답
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.contentType(MediaType.APPLICATION_JSON)
			.body(null);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<String>> handleException(Exception e) {
		ApiResponse<String> response = new ApiResponse<>(false, e.getMessage(), null);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	}

	@ExceptionHandler(NotFoundException.class)
	public ResponseEntity<ApiResponse<String>> handleNotFoundException(NotFoundException e) {
		ApiResponse<String> response = new ApiResponse<>(false, e.getMessage(), null);
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<String>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
		ApiResponse<String> response = new ApiResponse<>(false, e.getMessage(), null);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<ApiResponse<String>> handleRuntimeException(RuntimeException e) {
		ApiResponse<String> response = new ApiResponse<>(false, e.getMessage(), null);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	}

	@ExceptionHandler(FileUploadException.class)
	public ResponseEntity<ApiResponse<String>> handleFileUploadException(FileUploadException e) {
		ApiResponse<String> response = new ApiResponse<>(false, e.getMessage(), null);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	@ExceptionHandler(MissingServletRequestPartException.class)
	public ResponseEntity<ApiResponse<String>> handleMissingPart(MissingServletRequestPartException e) {
		ApiResponse<String> response = new ApiResponse<>(false, "필수 파일이 누락되었습니다.", null);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	@ExceptionHandler(MultipartException.class)
	public ResponseEntity<ApiResponse<String>> handleMultipartException(MultipartException e) {
		ApiResponse<String> response = new ApiResponse<>(false, "파일 업로드 요청이 잘못되었습니다.", null);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	@ExceptionHandler(UnauthenticatedUserException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public ResponseEntity<ApiResponse<String>> handleUnauthenticatedUserException(UnauthenticatedUserException e) {
		ApiResponse<String> response = new ApiResponse<>(false, e.getMessage(), null);
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
	}

	@ExceptionHandler(AlreadyExistsException.class)
	@ResponseStatus(HttpStatus.CONFLICT)
	public ResponseEntity<ApiResponse<String>> handleAlreadyExistsException(AlreadyExistsException e) {
		ApiResponse<String> response = new ApiResponse<>(false, e.getMessage(), null);
		return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
	}

}
