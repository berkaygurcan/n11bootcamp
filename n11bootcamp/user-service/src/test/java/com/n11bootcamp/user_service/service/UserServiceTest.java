package com.n11bootcamp.user_service.service;

import com.n11bootcamp.user_service.entity.User;
import com.n11bootcamp.user_service.exception.BadRequestException;
import com.n11bootcamp.user_service.exception.ResourceNotFoundException;
import com.n11bootcamp.user_service.repository.UserRepository;
import com.n11bootcamp.user_service.request.SignupRequest;
import com.n11bootcamp.user_service.request.UpdateUserRequest;
import com.n11bootcamp.user_service.response.MessageResponse;
import com.n11bootcamp.user_service.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RestTemplate restTemplate;

    private UserServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UserServiceImpl();
        ReflectionTestUtils.setField(service, "userRepository", userRepository);
        ReflectionTestUtils.setField(service, "restTemplate", restTemplate);
    }

    @Test
    void registerUserShouldSaveCustomerWhenUsernameAndEmailAreAvailable() {
        SignupRequest request = signupRequest();

        when(userRepository.existsByUsername("demo")).thenReturn(false);
        when(userRepository.existsByEmail("demo@test.com")).thenReturn(false);

        ResponseEntity<?> response = service.registerUser(request);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(message(response)).isEqualTo("User registered successfully!");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUserShouldRejectDuplicateUsername() {
        SignupRequest request = signupRequest();

        when(userRepository.existsByUsername("demo")).thenReturn(true);

        assertThatThrownBy(() -> service.registerUser(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Username is already taken!");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUserShouldRejectDuplicateEmail() {
        SignupRequest request = signupRequest();

        when(userRepository.existsByUsername("demo")).thenReturn(false);
        when(userRepository.existsByEmail("demo@test.com")).thenReturn(true);

        assertThatThrownBy(() -> service.registerUser(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Email is already in use!");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUserShouldUpdateEmailAndPassword() {
        User user = new User("demo", "old@test.com", "old-password", "Customer");
        UpdateUserRequest request = new UpdateUserRequest();
        request.setEmail("new@test.com");
        request.setPassword("new-password");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);

        ResponseEntity<?> response = service.updateUser(1L, request);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(message(response)).isEqualTo("User account updated successfully!");
        assertThat(user.getEmail()).isEqualTo("new@test.com");
        assertThat(user.getPassword()).isNotEqualTo("old-password");
        verify(userRepository).save(user);
    }

    @Test
    void updateUserShouldRejectDuplicateEmail() {
        User user = new User("demo", "old@test.com", "old-password", "Customer");
        UpdateUserRequest request = new UpdateUserRequest();
        request.setEmail("new@test.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("new@test.com")).thenReturn(true);

        assertThatThrownBy(() -> service.updateUser(1L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Email is already in use!");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUserShouldReturnBadRequestWhenUserNotFound() {
        UpdateUserRequest request = new UpdateUserRequest();

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateUser(1L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found!");
    }

    private SignupRequest signupRequest() {
        SignupRequest request = new SignupRequest();
        request.setUsername("demo");
        request.setEmail("demo@test.com");
        request.setPassword("password");
        return request;
    }

    private String message(ResponseEntity<?> response) {
        return ((MessageResponse) response.getBody()).getMessage();
    }
}
