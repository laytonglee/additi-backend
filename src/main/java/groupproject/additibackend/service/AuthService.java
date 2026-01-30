package groupproject.additibackend.service;

import groupproject.additibackend.request.AuthLoginRequest;
import groupproject.additibackend.response.AuthResponse;

public interface AuthService {
    public AuthResponse login(AuthLoginRequest request);
}
