package com.orioljt.taskmanager.service;

import com.orioljt.taskmanager.dto.CreateUserRequest;
import com.orioljt.taskmanager.dto.UpdateUserPasswordRequest;
import com.orioljt.taskmanager.dto.UserResponse;
import com.orioljt.taskmanager.entity.User;
import com.orioljt.taskmanager.exception.NotFoundException;
import com.orioljt.taskmanager.repository.UserRepository;
import com.orioljt.taskmanager.security.CurrentUserProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserProvider currentUserProvider;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       CurrentUserProvider currentUserProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.currentUserProvider = currentUserProvider;
    }

    public UserResponse register(CreateUserRequest createUserRequest) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.markNew();
        user.setEmail(createUserRequest.email());
        user.setPassword(passwordEncoder.encode(createUserRequest.password()));
        User savedUser = userRepository.save(user);
        return toResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        UUID userId = currentUserProvider.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return toResponse(user);
    }

    public void updateMyPassword(UpdateUserPasswordRequest updateUserPasswordRequest) {
        UUID userId = currentUserProvider.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        user.setPassword(passwordEncoder.encode(updateUserPasswordRequest.newPassword()));
        userRepository.save(user);
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getCreatedAt());
    }
}
