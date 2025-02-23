package com.example.video.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import com.example.video.response.ApiResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

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

}
