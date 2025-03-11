package com.example.video.exception;

public class UnauthenticatedUserException extends RuntimeException {

	public UnauthenticatedUserException() {
		super("인증된 사용자만 이 기능을 사용할 수 있습니다.");
	}

	public UnauthenticatedUserException(String message) {
		super(message);
	}
}
