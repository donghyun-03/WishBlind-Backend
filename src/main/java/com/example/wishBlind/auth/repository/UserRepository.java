package com.example.wishBlind.auth.repository;

import com.example.wishBlind.auth.domain.User;
import com.example.wishBlind.auth.domain.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailAndStatus(String email, UserStatus status);

    boolean existsByEmailAndStatus(String email, UserStatus status);
}
