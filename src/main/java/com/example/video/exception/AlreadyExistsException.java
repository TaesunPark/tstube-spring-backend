package com.example.video.exception;

/**
 * 이미 존재하는 리소스에 대한 중복 생성 시도를 나타내는 예외
 */
public class AlreadyExistsException extends RuntimeException {

	public AlreadyExistsException(String message) {
		super(message);
	}

	public AlreadyExistsException(String message, Throwable cause) {
		super(message, cause);
	}
}
