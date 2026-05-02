package com.powerstock.service;

import com.powerstock.common.BusinessException;
import com.powerstock.dto.request.LoginRequest;
import com.powerstock.dto.request.RegisterRequest;
import com.powerstock.dto.response.AuthResponse;
import com.powerstock.model.entity.Location;
import com.powerstock.model.entity.User;
import com.powerstock.repository.LocationRepository;
import com.powerstock.repository.UserRepository;
import com.powerstock.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock private UserRepository userRepository;
    @Mock private LocationRepository locationRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @InjectMocks private AuthService authService;

    private RegisterRequest registerRequest;
    private Location testLocation;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setLocationId(1L);
        testLocation = Location.builder().name("Main Warehouse").build();
        testLocation.setId(1L);
    }

    @Test
    void shouldRegisterUserSuccessfully() {
        when(locationRepository.findById(1L)).thenReturn(Optional.of(testLocation));
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed_password");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> { User u = inv.getArgument(0); u.setId(1L); return u; });
        when(jwtTokenProvider.generateToken(any(User.class))).thenReturn("access_token");
        when(jwtTokenProvider.generateRefreshToken(any(User.class))).thenReturn("refresh_token");

        AuthResponse response = authService.register(registerRequest);
        assertThat(response.getAccessToken()).isEqualTo("access_token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh_token");
        assertThat(response.getUsername()).isEqualTo("testuser");
    }

    @Test
    void shouldThrowWhenRegisteringWithDuplicateUsername() {
        when(locationRepository.findById(1L)).thenReturn(Optional.of(testLocation));
        when(userRepository.existsByUsername("testuser")).thenReturn(true);
        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(BusinessException.class).hasMessage("Username already exists");
    }

    @Test
    void shouldThrowWhenRegisteringWithDuplicateEmail() {
        when(locationRepository.findById(1L)).thenReturn(Optional.of(testLocation));
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);
        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(BusinessException.class).hasMessage("Email already exists");
    }

    @Test
    void shouldLoginSuccessfully() {
        User user = User.builder().username("testuser").email("test@example.com").passwordHash("hashed_password").build();
        user.setId(1L);
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashed_password")).thenReturn(true);
        when(jwtTokenProvider.generateToken(user)).thenReturn("access_token");
        when(jwtTokenProvider.generateRefreshToken(user)).thenReturn("refresh_token");

        AuthResponse response = authService.login(loginRequest);
        assertThat(response.getAccessToken()).isEqualTo("access_token");
    }

    @Test
    void shouldThrowWhenLoginWithInvalidCredentials() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("wrongpassword");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BusinessException.class).hasMessage("Invalid username or password");
    }
}
