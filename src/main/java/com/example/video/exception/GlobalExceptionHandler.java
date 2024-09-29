package com.example.video.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;

import com.example.video.response.ApiResponse;

@ControllerAdvice
public class GlobalExceptionHandler {

	public ResponseEntity<ApiResponse<String>> handleException(Exception e) {
		ApiResponse<String> response = new ApiResponse<>(false, e.getMessage(), null);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	}
}
