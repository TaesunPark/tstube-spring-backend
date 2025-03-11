package com.example.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.user.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByProviderId(String providerId);
	boolean existsByProviderId(String providerId);
	Optional<User> findByEmail(String email);
}
