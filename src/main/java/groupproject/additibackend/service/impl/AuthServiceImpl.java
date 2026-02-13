package groupproject.additibackend.service.impl;

import groupproject.additibackend.config.JwtProperties;
import groupproject.additibackend.model.RefreshToken;
import groupproject.additibackend.model.Role;
import groupproject.additibackend.model.User;
import groupproject.additibackend.repository.RefreshTokenRepository;
import groupproject.additibackend.repository.UserRepository;
import groupproject.additibackend.request.AuthLoginRequest;
import groupproject.additibackend.response.AuthResponse;
import groupproject.additibackend.response.MeResponse;
import groupproject.additibackend.service.AuthService;
import groupproject.additibackend.service.JwtService;
import groupproject.additibackend.service.RefreshTokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserDetailsService userDetailsService;
    private final JwtProperties jwtProperties;

    public AuthServiceImpl(AuthenticationManager authenticationManager, UserRepository userRepository, JwtService jwtService, RefreshTokenService refreshTokenService, RefreshTokenRepository refreshTokenRepository, UserDetailsService userDetailsService, JwtProperties jwtProperties) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userDetailsService = userDetailsService;
        this.jwtProperties = jwtProperties;
    }

    @Override
    public AuthResponse login(AuthLoginRequest request,
                              HttpServletResponse response) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.getRefreshToken(user);

        RefreshToken tokenEntity = new RefreshToken();
        tokenEntity.setToken(refreshToken);
        tokenEntity.setUser(user);
        tokenEntity.setRevoked(false);
        tokenEntity.setExpiresAt(Instant.now().plusMillis(jwtProperties.getRefreshExpiration()));

        refreshTokenRepository.save(tokenEntity);


        Cookie accessCookie = new Cookie("accessToken", accessToken);
        accessCookie.setHttpOnly(true);
        accessCookie.setSecure(true); // true in production (HTTPS)
        accessCookie.setPath("/");
        accessCookie.setMaxAge((int) (jwtProperties.getExpiration() / 1000)); // 10 minute

        Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true); // true in production
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge((int) (jwtProperties.getRefreshExpiration() / 1000)); // 2 hours

//        response.addCookie(accessCookie);
//        response.addCookie(refreshCookie);
        response.addHeader("Set-Cookie",
                "accessToken=" + accessToken +
                        "; HttpOnly; Secure; Path=/; SameSite=None; Max-Age=" +
                        (jwtProperties.getExpiration() / 1000));

        response.addHeader("Set-Cookie",
                "refreshToken=" + refreshToken +
                        "; HttpOnly; Secure; Path=/; SameSite=None; Max-Age=" +
                        (jwtProperties.getRefreshExpiration() / 1000));




        AuthResponse authResponse = new AuthResponse();
        authResponse.setType("Bearer");
        authResponse.setAccessToken(accessToken);
        authResponse.setRefreshToken(refreshToken);
        authResponse.setRoles(user.getRoles()
                .stream()
                .map(Role::getName)
                .collect(Collectors.toSet()));

        return authResponse;
    }


    @Override
    public MeResponse me(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }


        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        MeResponse res = new MeResponse();
        res.setEmail(user.getEmail());
        res.setUsername(user.getRealUsername());
        res.setPhoto(user.getPhoto());
        res.setAddress(user.getAddress());
        res.setBio(user.getBio());
        res.setPhoneNumber(user.getPhoneNumber());
        res.setRoles(
                user.getRoles()
                        .stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet()));

        return res;
    }


    @Override
    public void logout(String refreshToken, HttpServletResponse response) {

        if (refreshToken != null) {
            refreshTokenService.revoke(refreshToken);
        }

        response.addHeader("Set-Cookie",
                "accessToken=; HttpOnly; Secure; Path=/; SameSite=None; Max-Age=0");

        response.addHeader("Set-Cookie",
                "refreshToken=; HttpOnly; Secure; Path=/; SameSite=None; Max-Age=0");
    }


    private void deleteCookie(String name, HttpServletResponse response) {
        Cookie cookie = new Cookie(name, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }


    @Override
    public ResponseEntity<?> refresh(String refreshToken, HttpServletResponse response) {
        if (refreshToken == null ||
                !jwtService.validateRefreshToken(refreshToken)) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid refresh token"));
        }

        String username = jwtService.extractUserName(refreshToken);

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String newAccessToken = jwtService.generateAccessToken(user);

        // ✅ IMPORTANT: Update access cookie
        Cookie accessCookie = new Cookie("accessToken", newAccessToken);
        accessCookie.setHttpOnly(true);
        accessCookie.setSecure(true); // true in production
        accessCookie.setPath("/");
        accessCookie.setMaxAge((int) (jwtProperties.getExpiration() / 1000));

        response.addCookie(accessCookie);

        return ResponseEntity.ok(Map.of("message", "Token refreshed"));
    }
}
