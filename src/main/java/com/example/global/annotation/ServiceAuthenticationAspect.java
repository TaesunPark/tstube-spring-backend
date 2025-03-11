package com.example.global.annotation;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import com.example.user.entity.User;
import com.example.video.exception.UnauthenticatedUserException;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Aspect
@Component
public class ServiceAuthenticationAspect {
	@Before("@annotation(com.example.global.annotation.RequiresServiceAuthentication)")
	public void checkAuthentication(JoinPoint joinPoint) {
		Object[] args = joinPoint.getArgs();
		boolean hasAuthenticatedUser = false;
		log.info("RequiresAuthentication AOP 실행: {}", joinPoint.getSignature().toShortString());

		// 	파라미터 중에서 User 타입이면서 null이 아닌 객체가 있는지 확인
		for(Object arg : args) {
			if (arg instanceof User) {
				User user = (User) arg;
				if (user != null) {
					hasAuthenticatedUser = true;
					break;
				}
			}
		}

		if (!hasAuthenticatedUser) {
			log.warn("인증된 사용자 없음: {}", joinPoint.getSignature().toShortString());
			throw new UnauthenticatedUserException("인증된 사용자면 이 기능을 사용할 수 있습니다.");
		}
		log.info("인증 확인 완료: {}", joinPoint.getSignature().toShortString());
	}
}
