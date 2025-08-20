package com.orioljt.taskmanager.security;

import com.orioljt.taskmanager.entity.User;
import com.orioljt.taskmanager.entity.UserRole;
import com.orioljt.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class SecurityUserDetailsTest {

    @Test
    void loadsUserDetailsWithRole() {
        UserRepository repo = mock(UserRepository.class);
        User u = new User();
        u.setEmail("u@e");
        u.setPassword("enc");
        u.setRole(UserRole.ADMIN);
        when(repo.findByEmail("u@e")).thenReturn(Optional.of(u));

        SecurityUserDetails sud = new SecurityUserDetails();
        UserDetailsService uds = sud.userDetailsService(repo);
        UserDetails details = uds.loadUserByUsername("u@e");
        assertThat(details.getUsername()).isEqualTo("u@e");
        assertThat(details.getPassword()).isEqualTo("enc");
        assertThat(details.getAuthorities().stream().map(GrantedAuthority::getAuthority)).contains("ROLE_ADMIN");
    }

    @Test
    void throwsWhenUserNotFound() {
        UserRepository repo = mock(UserRepository.class);
        when(repo.findByEmail("missing@e")).thenReturn(Optional.empty());
        SecurityUserDetails sud = new SecurityUserDetails();
        UserDetailsService uds = sud.userDetailsService(repo);
        assertThatThrownBy(() -> uds.loadUserByUsername("missing@e"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}

