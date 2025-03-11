package com.example.global.annotation;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import com.example.user.entity.User;
import com.example.video.response.ApiResponse;

@Aspect
@Component
public class ControllerAuthenticationAspect {
	@Around("@annotation(com.example.global.annotation.RequiresControllerAuthentication)")
	public Object checkAuthentication(ProceedingJoinPoint joinPoint) throws Throwable {
		Object[] args = joinPoint.getArgs();

		// 파라미터에서 User 객체 찾기
		User user = null;
		for (Object arg : args) {
			if (arg instanceof User) {
				user = (User) arg;
				break;
			}
		}
		if (user == null) {
			return new ApiResponse<>(false, "로그인이 필요합니다.", null);
		}

		return joinPoint.proceed();
	}
}
