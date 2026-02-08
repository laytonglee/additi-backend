package groupproject.additibackend.service.impl;

import groupproject.additibackend.model.RefreshToken;
import groupproject.additibackend.model.User;
import groupproject.additibackend.repository.RefreshTokenRepository;
import groupproject.additibackend.repository.UserRepository;
import groupproject.additibackend.request.AuthLoginRequest;
import groupproject.additibackend.response.AuthResponse;
import groupproject.additibackend.response.MeResponse;
import groupproject.additibackend.response.UserViewResponse;
import groupproject.additibackend.service.AuthService;
import groupproject.additibackend.service.JwtService;
import groupproject.additibackend.service.RefreshTokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;

    public AuthServiceImpl(AuthenticationManager authenticationManager, UserRepository userRepository, JwtService jwtService, RefreshTokenService refreshTokenService, RefreshTokenRepository refreshTokenRepository) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Override
    public AuthResponse login(@NonNull AuthLoginRequest request) {

        // 1️ Authenticate credentials
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        // 2 Load user safely
        User user = userRepository.findByEmail(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 3️ Generate ACCESS token only
        String accessToken = jwtService.generateAccessToken(user);
        String getRefreshToken = jwtService.getRefreshToken(user);

        RefreshToken refreshTokenEntity = new RefreshToken();
        refreshTokenEntity.setToken(getRefreshToken);
        refreshTokenEntity.setUser(user);
        refreshTokenEntity.setExpiresAt(Instant.now().plusSeconds(3600)); // or Instant.now().plusSeconds(x)
        refreshTokenRepository.save(refreshTokenEntity);

        // 4️ Build response
        AuthResponse authResponse = new AuthResponse();
        authResponse.setAccessToken(accessToken);
        authResponse.setRefreshToken(getRefreshToken);
        authResponse.setType("Bearer");




        // 5️ User view
        UserViewResponse userView = new UserViewResponse();
        userView.setId(user.getId());
        userView.setEmail(user.getEmail());
        userView.setRole(
                user.getRoles()
                        .stream()
                        .map(role -> role.getName())
                        .collect(Collectors.toSet())
        );

        authResponse.setUser(userView);
        return authResponse;
    }

    @Override
    public MeResponse me(Authentication authentication) {

        // Because getUsername() returns email
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        MeResponse res = new MeResponse();
        res.setId(user.getId());
        res.setEmail(user.getEmail());
        res.setUsername(user.getUsername());

        return res;
    }

    @Override
    public void logout(String refreshToken, HttpServletResponse response) {

        if (refreshToken != null) {
            refreshTokenService.revoke(refreshToken);
        }

        Cookie cookie = new Cookie("refresh_token", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0); // 🔥 DELETE cookie
        response.addCookie(cookie);
    }
}
