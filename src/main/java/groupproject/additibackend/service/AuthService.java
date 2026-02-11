package groupproject.additibackend.service;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import groupproject.additibackend.request.AuthLoginRequest;
import groupproject.additibackend.response.AuthResponse;
import groupproject.additibackend.response.MeResponse;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    AuthResponse login(AuthLoginRequest request, HttpServletResponse response);
    void logout(String refreshToken, HttpServletResponse response);
    MeResponse me(Authentication authentication);
    ResponseEntity<?> refresh(String refreshToken, HttpServletResponse response);
}
