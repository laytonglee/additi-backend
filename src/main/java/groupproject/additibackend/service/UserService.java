package groupproject.additibackend.service;

import groupproject.additibackend.request.RegisterRequest;
import groupproject.additibackend.request.UpdateMeRequest;
import groupproject.additibackend.request.UserRequest;
import groupproject.additibackend.response.*;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface UserService {
    RegisterResponse registerUser(RegisterRequest request, MultipartFile photo) throws IOException;
    UpdateMeResponse updateMe(Authentication authentication, UpdateMeRequest request, MultipartFile photo) throws IOException;


    UserResponse createUser(UserRequest requestDTO);

    UserResponse getUserById(Long id);

    PageResponse<UserResponse> getAllUsers(Pageable pageable);
}
