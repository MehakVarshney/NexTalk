package com.nextalk.auth.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.nextalk.auth.dto.AuthResponse;
import com.nextalk.auth.dto.GoogleLoginRequest;
import com.nextalk.auth.dto.LoginRequest;
import com.nextalk.auth.dto.RegisterRequest;
import com.nextalk.auth.entity.AppUser;
import com.nextalk.auth.entity.AuthProvider;
import com.nextalk.auth.entity.UserStatus;
import com.nextalk.auth.exception.ApiException;
import com.nextalk.auth.mapper.UserMapper;
import com.nextalk.auth.repository.AppUserRepository;
import com.nextalk.auth.security.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final GoogleTokenService googleTokenService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "Email is already registered");
        }

        AppUser user = AppUser.builder()
                .name(request.name().trim())
                .email(email)
                .password(passwordEncoder.encode(request.password()))
                .provider(AuthProvider.LOCAL)
                .status(UserStatus.ONLINE)
                .enabled(true)
                .build();

        AppUser savedUser = userRepository.save(user);
        return createAuthResponse(savedUser);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String email = request.email().toLowerCase();
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.password()));

        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        user.setStatus(UserStatus.ONLINE);
        return createAuthResponse(user);
    }

    @Transactional
    public AuthResponse googleLogin(GoogleLoginRequest request) {
        GoogleIdToken.Payload payload = googleTokenService.verify(request.idToken());
        String googleSubject = payload.getSubject();
        String email = String.valueOf(payload.getEmail()).toLowerCase();
        String name = String.valueOf(payload.get("name"));
        String avatarUrl = String.valueOf(payload.get("picture"));

        AppUser user = userRepository.findByGoogleSubject(googleSubject)
                .or(() -> userRepository.findByEmail(email))
                .map(existingUser -> updateGoogleProfile(existingUser, googleSubject, name, avatarUrl))
                .orElseGet(() -> AppUser.builder()
                        .name(name)
                        .email(email)
                        .googleSubject(googleSubject)
                        .avatarUrl(avatarUrl)
                        .provider(AuthProvider.GOOGLE)
                        .status(UserStatus.ONLINE)
                        .enabled(true)
                        .build());

        AppUser savedUser = userRepository.save(user);
        return createAuthResponse(savedUser);
    }

    private AppUser updateGoogleProfile(AppUser user, String googleSubject, String name, String avatarUrl) {
        user.setGoogleSubject(googleSubject);
        user.setProvider(AuthProvider.GOOGLE);
        user.setName(name);
        user.setAvatarUrl(avatarUrl);
        user.setStatus(UserStatus.ONLINE);
        return user;
    }

    private AuthResponse createAuthResponse(AppUser user) {
        return new AuthResponse("Bearer", jwtService.generateToken(user), userMapper.toResponse(user));
    }
}
