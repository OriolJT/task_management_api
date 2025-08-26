package com.orioljt.taskmanager.service;

import com.orioljt.taskmanager.dto.CreateUserRequest;
import com.orioljt.taskmanager.dto.UpdateUserPasswordRequest;
import com.orioljt.taskmanager.dto.UpdateUserRequest;
import com.orioljt.taskmanager.dto.UserResponse;
import com.orioljt.taskmanager.entity.User;
import com.orioljt.taskmanager.exception.NotFoundException;
import com.orioljt.taskmanager.mapper.UserMapper;
import com.orioljt.taskmanager.repository.UserRepository;
import com.orioljt.taskmanager.security.CurrentUserProvider;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final CurrentUserProvider currentUserProvider;
  private final UserMapper userMapper;

  public UserService(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      CurrentUserProvider currentUserProvider,
      UserMapper userMapper) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.currentUserProvider = currentUserProvider;
    this.userMapper = userMapper;
  }

  public UserResponse register(CreateUserRequest createUserRequest) {
    User user = new User();
    user.setId(UUID.randomUUID());
    user.markNew();
    user.setEmail(createUserRequest.email());
    user.setPassword(passwordEncoder.encode(createUserRequest.password()));
    User savedUser = userRepository.save(user);
    return userMapper.toResponse(savedUser);
  }

  @Transactional(readOnly = true)
  public UserResponse getCurrentUser() {
    UUID userId = currentUserProvider.getCurrentUserId();
    User user =
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
    return userMapper.toResponse(user);
  }

  @Transactional(readOnly = true)
  public UserResponse getUser(UUID userId) {
    User user =
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
    return userMapper.toResponse(user);
  }

  public void updateMyPassword(UpdateUserPasswordRequest updateUserPasswordRequest) {
    UUID userId = currentUserProvider.getCurrentUserId();
    User user =
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));

    user.setPassword(passwordEncoder.encode(updateUserPasswordRequest.newPassword()));
    userRepository.save(user);
  }

  public UserResponse updateMyAccount(UpdateUserRequest request) {
    UUID userId = currentUserProvider.getCurrentUserId();
    User user =
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
    userMapper.update(user, request);
    if (request.password() != null) {
      user.setPassword(passwordEncoder.encode(request.password()));
    }
    User saved = userRepository.save(user);
    return userMapper.toResponse(saved);
  }

  public UserResponse adminUpdateUser(UUID userId, UpdateUserRequest request) {
    User user =
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
    userMapper.update(user, request);
    if (request.password() != null) {
      user.setPassword(passwordEncoder.encode(request.password()));
    }
    User saved = userRepository.save(user);
    return userMapper.toResponse(saved);
  }
}
