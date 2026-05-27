package com.urlshortener.service;

import com.urlshortener.dto.auth.AuthResponse;
import com.urlshortener.dto.auth.LoginRequest;
import com.urlshortener.dto.auth.RefreshTokenRequest;
import com.urlshortener.dto.auth.RegisterRequest;
import com.urlshortener.entity.Role;
import com.urlshortener.entity.User;
import com.urlshortener.exception.BadRequestException;
import com.urlshortener.exception.UnauthorizedException;
import com.urlshortener.repository.UserRepository;
import com.urlshortener.security.JwtTokenProvider;
import com.urlshortener.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();
        userRepository.save(user);

        UserPrincipal principal = new UserPrincipal(user);
        return buildAuthResponse(principal);
    }

    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsernameOrEmail(),
                            request.getPassword()));
        } catch (Exception e) {
            throw new UnauthorizedException("Invalid credentials");
        }

        User user = userRepository.findByUsername(request.getUsernameOrEmail())
                .or(() -> userRepository.findByEmail(request.getUsernameOrEmail()))
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        return buildAuthResponse(new UserPrincipal(user));
    }

    public AuthResponse refresh(RefreshTokenRequest request) {
        String token = request.getRefreshToken();
        if (!jwtTokenProvider.isRefreshToken(token)) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        String username = jwtTokenProvider.extractUsername(token);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        UserPrincipal principal = new UserPrincipal(user);
        if (!jwtTokenProvider.isTokenValid(token, principal)) {
            throw new UnauthorizedException("Refresh token expired");
        }

        return buildAuthResponse(principal);
    }

    private AuthResponse buildAuthResponse(UserDetails userDetails) {
        UserPrincipal principal = (UserPrincipal) userDetails;
        return AuthResponse.builder()
                .accessToken(jwtTokenProvider.generateAccessToken(userDetails))
                .refreshToken(jwtTokenProvider.generateRefreshToken(userDetails))
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessExpirationMs() / 1000)
                .user(AuthResponse.UserSummary.builder()
                        .id(principal.getId())
                        .username(principal.getUsername())
                        .email(principal.getEmail())
                        .role(principal.getRole().name())
                        .build())
                .build();
    }
}
