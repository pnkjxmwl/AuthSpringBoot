package com.project.auth_server.service;

import com.project.auth_server.dto.request.LoginRequest;
import com.project.auth_server.dto.request.RegisterRequest;
import com.project.auth_server.dto.response.AuthResponse;
import com.project.auth_server.entity.AuthProvider;
import com.project.auth_server.entity.Role;
import com.project.auth_server.entity.User;
import com.project.auth_server.exception.InvalidTokenException;
import com.project.auth_server.exception.UserAlreadyExistsException;
import com.project.auth_server.repository.UserRepository;
import com.project.auth_server.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;

    // ─── Register ──────────────────────────────────────────────────────────────

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException(
                "Email is already registered: " + request.getEmail()
            );
        }

        User user = User.builder()
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .role(Role.ROLE_USER)
            .provider(AuthProvider.LOCAL)
            .build();

        userRepository.save(user);
        log.info("New user registered: {}", user.getEmail());

        return buildAuthResponse(user);
    }

    // ─── Login ─────────────────────────────────────────────────────────────────

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getEmail(),
                request.getPassword()
            )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() ->
                new UsernameNotFoundException("User not found: " + request.getEmail()));

        log.info("User logged in: {}", user.getEmail());
        return buildAuthResponse(user);
    }

    // ─── Refresh ───────────────────────────────────────────────────────────────

    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        String email = refreshTokenService.getEmailByRefreshToken(refreshToken)
            .orElseThrow(() ->
                new InvalidTokenException("Invalid or expired refresh token"));

        // Token rotation: delete old, issue new
        refreshTokenService.deleteRefreshToken(refreshToken);

        User user = userRepository.findByEmail(email)
            .orElseThrow(() ->
                new UsernameNotFoundException("User not found: " + email));

        log.info("Token refreshed for: {}", email);
        return buildAuthResponse(user);
    }

    // ─── Logout ────────────────────────────────────────────────────────────────

    public void logout(String refreshToken) {
        refreshTokenService.deleteRefreshToken(refreshToken);
        SecurityContextHolder.clearContext();
        log.info("User logged out — refresh token invalidated");
    }

    // ─── Private ───────────────────────────────────────────────────────────────

    private AuthResponse buildAuthResponse(User user) {
        String accessToken  = jwtTokenProvider.generateToken(user.getEmail());
        String refreshToken = refreshTokenService.createRefreshToken(user.getEmail());

        return AuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .email(user.getEmail())
            .role(user.getRole().name())
            .build();
    }
}