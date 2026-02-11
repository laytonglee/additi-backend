package groupproject.additibackend.service;

import org.springframework.security.core.Authentication;

import groupproject.additibackend.request.AuthLoginRequest;
import groupproject.additibackend.response.AuthResponse;
import groupproject.additibackend.response.MeResponse;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    AuthResponse login(AuthLoginRequest request);
    MeResponse me(Authentication authentication);
    void logout(String refreshToken, HttpServletResponse response);
}
