package com.orioljt.taskmanager.config;

import com.orioljt.taskmanager.entity.User;
import com.orioljt.taskmanager.entity.UserRole;
import com.orioljt.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class DataLoaderTest {

    @Test
    void seedRunner_runsCreateIfMissingWhenEnabled() throws Exception {
        UserRepository repo = mock(UserRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        when(encoder.encode(any())).thenReturn("enc");
        DataLoader dl = new DataLoader();
        CommandLineRunner runner = dl.seedDefaultUser(repo, encoder, true, "admin@example.com", "pw");
        runner.run();
        verify(repo).findByEmail("admin@example.com");
    }

    @Test
    void createIfMissing_savesAdminWhenNotExists() {
        UserRepository repo = mock(UserRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        when(repo.findByEmail("admin@example.com")).thenReturn(Optional.empty());
        when(encoder.encode("pw")).thenReturn("enc");
        DataLoader dl = new DataLoader();
        dl.createIfMissing(repo, encoder, "admin@example.com", "pw");
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(repo).save(captor.capture());
        User u = captor.getValue();
        assertThat(u.getEmail()).isEqualTo("admin@example.com");
        assertThat(u.getPassword()).isEqualTo("enc");
        assertThat(u.getRole()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    void createIfMissing_skipsWhenExists() {
        UserRepository repo = mock(UserRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        when(repo.findByEmail("admin@example.com")).thenReturn(Optional.of(new User()));
        DataLoader dl = new DataLoader();
        dl.createIfMissing(repo, encoder, "admin@example.com", "pw");
        verify(repo, never()).save(any());
    }
}

