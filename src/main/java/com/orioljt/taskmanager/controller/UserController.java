package com.orioljt.taskmanager.controller;

import com.orioljt.taskmanager.dto.CreateUserRequest;
import com.orioljt.taskmanager.dto.UpdateUserPasswordRequest;
import com.orioljt.taskmanager.dto.UpdateUserRequest;
import com.orioljt.taskmanager.dto.UserResponse;
import com.orioljt.taskmanager.service.UserService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@Validated
@RequestMapping("/api")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/users")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody CreateUserRequest createUserRequest) {
        UserResponse userResponse = userService.register(createUserRequest);
        return ResponseEntity.created(URI.create("/api/users/" + userResponse.id()))
                .body(userResponse);
    }

    @GetMapping("/account")
    public UserResponse getAccount() {
        return userService.getCurrentUser();
    }

    @PatchMapping("/account/password")
    public ResponseEntity<Void> updateAccountPassword(
            @Valid @RequestBody UpdateUserPasswordRequest updateUserPasswordRequest) {
        userService.updateMyPassword(updateUserPasswordRequest);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/admin/users/{id}")
    public UserResponse getUser(@PathVariable UUID id) {
        return userService.getUser(id);
    }

    @PatchMapping("/account")
    public UserResponse updateMyAccount(@Valid @RequestBody UpdateUserRequest request) {
        return userService.updateMyAccount(request);
    }

    @PatchMapping("/admin/users/{id}")
    public UserResponse adminUpdateUser(@PathVariable UUID id, @Valid @RequestBody UpdateUserRequest request) {
        return userService.adminUpdateUser(id, request);
    }
}
