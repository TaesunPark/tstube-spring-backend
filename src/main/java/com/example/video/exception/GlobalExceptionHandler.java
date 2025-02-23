package com.example.video.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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


}
