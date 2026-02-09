package groupproject.additibackend.service;

import groupproject.additibackend.request.RegisterRequest;
import groupproject.additibackend.request.UpdateMeRequest;
import groupproject.additibackend.response.MeResponse;
import groupproject.additibackend.response.RegisterResponse;
import org.springframework.security.core.Authentication;

public interface UserService {
    RegisterResponse registerUser(RegisterRequest request);
    MeResponse updateMe(Authentication authentication, UpdateMeRequest request);
}
