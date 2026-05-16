package com.nextalk.auth.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.nextalk.auth.dto.AuthResponse;
import com.nextalk.auth.dto.LoginRequest;
import com.nextalk.auth.dto.RegisterRequest;
import com.nextalk.auth.dto.UserResponse;
import com.nextalk.auth.entity.AppUser;
import com.nextalk.auth.entity.AuthProvider;
import com.nextalk.auth.exception.ApiException;
import com.nextalk.auth.mapper.UserMapper;
import com.nextalk.auth.repository.AppUserRepository;
import com.nextalk.auth.security.JwtService;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AppUserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;
    @Mock
    private UserMapper userMapper;
    @Mock
    private GoogleTokenService googleTokenService;

    @InjectMocks
    private AuthService authService;

    private AppUser mockUser;
    private UserResponse mockUserResponse;

    @BeforeEach
    void setUp() {
        mockUser = new AppUser();
        mockUser.setId(UUID.randomUUID());
        mockUser.setEmail("test@test.com");
        mockUser.setName("Test User");
        mockUser.setProvider(AuthProvider.LOCAL);

        mockUserResponse = new UserResponse(
            mockUser.getId(), mockUser.getName(), mockUser.getEmail(), 
            null, mockUser.getProvider(), null, null
        );
    }

    @Test
    void register_Success() {
        RegisterRequest request = new RegisterRequest("Test User", "test@test.com", "password123");
        
        when(userRepository.existsByEmail("test@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded_pass");
        when(userRepository.save(any(AppUser.class))).thenReturn(mockUser);
        when(jwtService.generateToken(mockUser)).thenReturn("mock_jwt_token");
        when(userMapper.toResponse(mockUser)).thenReturn(mockUserResponse);

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("Bearer", response.tokenType());
        assertEquals("mock_jwt_token", response.accessToken());
        assertEquals("test@test.com", response.user().email());
        
        verify(userRepository).save(any(AppUser.class));
    }

    @Test
    void register_EmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest("Test User", "test@test.com", "password123");
        
        when(userRepository.existsByEmail("test@test.com")).thenReturn(true);

        assertThrows(ApiException.class, () -> authService.register(request));
        
        verify(userRepository, never()).save(any(AppUser.class));
    }

    @Test
    void login_Success() {
        LoginRequest request = new LoginRequest("test@test.com", "password123");
        
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));
        when(jwtService.generateToken(mockUser)).thenReturn("mock_jwt_token");
        when(userMapper.toResponse(mockUser)).thenReturn(mockUserResponse);

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("mock_jwt_token", response.accessToken());
        
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void login_InvalidEmail() {
        LoginRequest request = new LoginRequest("wrong@test.com", "password123");
        
        when(userRepository.findByEmail("wrong@test.com")).thenReturn(Optional.empty());

        assertThrows(ApiException.class, () -> authService.login(request));
    }
}
