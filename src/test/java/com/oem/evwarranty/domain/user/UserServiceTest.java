package com.oem.evwarranty.domain.user;



import com.oem.evwarranty.domain.user.RoleRepository;
import com.oem.evwarranty.domain.user.UserService;
import com.oem.evwarranty.domain.user.User;
import com.oem.evwarranty.domain.user.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setActive(true);
    }

    @Test
    void createUser_Success() {
        when(userRepository.existsByUsername(user.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(user.getEmail())).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        User created = userService.createUser(user, Set.of("ADMIN"));

        assertNotNull(created);
        assertEquals("encodedPassword", user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    void createUser_DuplicateUsername_ThrowsException() {
        when(userRepository.existsByUsername(user.getUsername())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(user, Set.of("ADMIN"));
        });
    }

    @Test
    void toggleUserStatus_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.toggleUserStatus(1L);

        assertFalse(user.getActive());
        verify(userRepository).save(user);
    }

    @Test
    void updatePassword_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");

        userService.updatePassword(1L, "newPassword");

        assertEquals("encodedNewPassword", user.getPassword());
        verify(userRepository).save(user);
    }
}
