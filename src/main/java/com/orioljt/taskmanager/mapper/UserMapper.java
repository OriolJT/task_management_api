package com.orioljt.taskmanager.mapper;

import com.orioljt.taskmanager.dto.UpdateUserRequest;
import com.orioljt.taskmanager.dto.UserResponse;
import com.orioljt.taskmanager.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getCreatedAt());
    }

    public void update(User user, UpdateUserRequest request) {
        if (request.email() != null) user.setEmail(request.email());
        if (request.role() != null) user.setRole(request.role());
    }
}
