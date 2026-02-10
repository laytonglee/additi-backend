package groupproject.additibackend.service;

import groupproject.additibackend.request.RegisterRequest;
import groupproject.additibackend.request.UpdateMeRequest;
import groupproject.additibackend.response.MeResponse;
import groupproject.additibackend.response.RegisterResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface UserService {
    RegisterResponse registerUser(RegisterRequest request, MultipartFile photo) throws IOException;
    MeResponse updateMe(Authentication authentication, UpdateMeRequest request);
}
