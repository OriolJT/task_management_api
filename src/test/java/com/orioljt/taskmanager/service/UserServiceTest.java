package com.orioljt.taskmanager.service;

import com.orioljt.taskmanager.dto.*;
import com.orioljt.taskmanager.entity.User;
import com.orioljt.taskmanager.exception.NotFoundException;
import com.orioljt.taskmanager.repository.UserRepository;
import com.orioljt.taskmanager.security.CurrentUserProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private CurrentUserProvider currentUserProvider;

    private UserService service;

    @BeforeEach
    void setUp() {
        service = new UserService(userRepository, passwordEncoder, currentUserProvider, new com.orioljt.taskmanager.mapper.UserMapper());
        when(passwordEncoder.encode(any())).thenAnswer(inv -> "enc-" + inv.getArgument(0));
    }

    @Test
    void register_shouldCreateUserWithEncodedPassword() {
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(any())).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });
    CreateUserRequest req = new CreateUserRequest("a@b.com", "Password1");
        UserResponse res = service.register(req);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getEmail()).isEqualTo("a@b.com");
        assertThat(saved.getPassword()).startsWith("enc-");
        assertThat(res.email()).isEqualTo("a@b.com");
    }

    @Test
    void getCurrentUser_shouldReturnFromRepo() {
        UUID id = UUID.randomUUID();
        when(currentUserProvider.getCurrentUserId()).thenReturn(id);
        User u = new User();
        u.setId(id);
        u.setEmail("me");
        when(userRepository.findById(id)).thenReturn(Optional.of(u));
        UserResponse res = service.getCurrentUser();
        assertThat(res.id()).isEqualTo(id);
    }

    @Test
    void getCurrentUser_shouldThrowIfMissing() {
        UUID id = UUID.randomUUID();
        when(currentUserProvider.getCurrentUserId()).thenReturn(id);
        when(userRepository.findById(id)).thenReturn(Optional.empty());
        assertThatThrownBy(service::getCurrentUser).isInstanceOf(NotFoundException.class);
    }

    @Test
    void getUser_shouldReturn() {
        UUID id = UUID.randomUUID();
        User u = new User();
        u.setId(id);
        when(userRepository.findById(id)).thenReturn(Optional.of(u));
        assertThat(service.getUser(id).id()).isEqualTo(id);
    }

    @Test
    void getUser_shouldThrowIfMissing() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getUser(id)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void updateMyPassword_shouldEncodeAndSave() {
        UUID id = UUID.randomUUID();
        when(currentUserProvider.getCurrentUserId()).thenReturn(id);
        User u = new User();
        u.setId(id);
        u.setPassword("old");
        when(userRepository.findById(id)).thenReturn(Optional.of(u));

    service.updateMyPassword(new UpdateUserPasswordRequest("NewPassword1"));
        verify(passwordEncoder).encode("NewPassword1");
        verify(userRepository).save(u);
    }

    @Test
    void updateMyPassword_shouldThrowIfUserMissing() {
        UUID id = UUID.randomUUID();
        when(currentUserProvider.getCurrentUserId()).thenReturn(id);
        when(userRepository.findById(id)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.updateMyPassword(new UpdateUserPasswordRequest("Password1")))
                .isInstanceOf(NotFoundException.class);
    }
}
