package groupproject.additibackend.service;

import groupproject.additibackend.request.AuthLoginRequest;
import groupproject.additibackend.request.UpdateProfileRequest;
import groupproject.additibackend.response.AuthResponse;
import groupproject.additibackend.response.MeResponse;
import groupproject.additibackend.response.UserProfileResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;

public interface AuthService {
    AuthResponse login(AuthLoginRequest request, HttpServletResponse response);
    MeResponse me(Authentication authentication);
    void logout(HttpServletRequest request, HttpServletResponse response);

    UserProfileResponse getProfile (Authentication authentication);
    UserProfileResponse updateProfile (Authentication authentication, UpdateProfileRequest request);

    AuthResponse refreshToken(HttpServletRequest request, HttpServletResponse response);
}
