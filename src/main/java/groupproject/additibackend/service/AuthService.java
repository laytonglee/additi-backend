package groupproject.additibackend.service;

import groupproject.additibackend.request.AuthLoginRequest;
import groupproject.additibackend.response.AuthResponse;
import groupproject.additibackend.response.MeResponse;
import org.springframework.security.core.Authentication;

public interface AuthService {
    AuthResponse login(AuthLoginRequest request);
    MeResponse me(Authentication authentication);
}
