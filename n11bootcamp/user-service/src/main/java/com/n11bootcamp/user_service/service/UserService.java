package com.n11bootcamp.user_service.service;

import com.n11bootcamp.user_service.request.LoginRequest;
import com.n11bootcamp.user_service.request.SignupRequest;
import com.n11bootcamp.user_service.request.UpdateUserRequest;
import org.springframework.http.ResponseEntity;
public interface UserService {
    ResponseEntity<?> authenticateUser(LoginRequest loginRequest);
    ResponseEntity<?> registerUser(SignupRequest signUpRequest);
    ResponseEntity<?> deleteUser(Long userId);
    ResponseEntity<?> updateUser(Long userId, UpdateUserRequest updateUserRequest);
}
