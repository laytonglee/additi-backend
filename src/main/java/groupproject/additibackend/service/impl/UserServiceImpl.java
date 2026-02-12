package groupproject.additibackend.service.impl;

import groupproject.additibackend.mapper.UserMapper;
import groupproject.additibackend.model.Role;
import groupproject.additibackend.model.User;
import groupproject.additibackend.repository.RoleRepository;
import groupproject.additibackend.repository.UserRepository;
import groupproject.additibackend.request.RegisterRequest;
import groupproject.additibackend.request.UpdateMeRequest;
import groupproject.additibackend.request.UserRequest;
import groupproject.additibackend.response.*;
import groupproject.additibackend.service.R2StorageService;
import groupproject.additibackend.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private  final R2StorageService r2StorageService;
    private final RoleRepository roleRepository;
    private  final UserMapper userMapper;


    @Override
    public RegisterResponse registerUser(RegisterRequest request, MultipartFile photo) throws IOException {

        String photoKey = null;
        String photoUrl = null;

        if (photo != null && !photo.isEmpty()) {
            photoKey = r2StorageService.uploadFile(photo, "users/avatar");
            photoUrl = r2StorageService.getPublicUrl(photoKey);
        }

        // ✅ Fetch ROLE_USER
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Role USER not found"));
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setAddress(request.getAddress());
        user.setBio(request.getBio());

        user.setPhoto(photoUrl); // ✅ URL stored
        user.setPhotoKey(photoKey);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(Set.of(userRole));

        userRepository.save(user);

        RegisterResponse response = new RegisterResponse();
        response.setUsername(user.getRealUsername());
        response.setEmail(user.getEmail());
        response.setMessage("Registered successfully");

        return response;
    }


    @Override
    public UpdateMeResponse updateMe(
            Authentication authentication,
            UpdateMeRequest request,
            MultipartFile photo
    ) throws IOException {

        String currentEmail = authentication.getName();

        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 🔹 Update only if provided
        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            user.setUsername(request.getUsername());
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            user.setEmail(request.getEmail());
        }

        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

        if (request.getAddress() != null) {
            user.setAddress(request.getAddress());
        }

        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }

        // 🔐 Password (optional)
        String password = request.getPassword();
        String confirmPassword = request.getConfirmPassword();

        if (password != null && !password.isBlank()) {

            if (confirmPassword == null || confirmPassword.isBlank()) {
                throw new IllegalArgumentException("Confirm password is required");
            }

            if (!password.equals(confirmPassword)) {
                throw new IllegalArgumentException("Passwords do not match");
            }

            user.setPassword(passwordEncoder.encode(password));
        }

        // 🖼 Photo (optional)
        if (photo != null && !photo.isEmpty()) {

            if (user.getPhotoKey() != null) {
                r2StorageService.deleteFile(user.getPhotoKey());
            }

            String photoKey = r2StorageService.uploadFile(photo, "users/avatar");
            String photoUrl = r2StorageService.getPublicUrl(photoKey);

            user.setPhotoKey(photoKey);
            user.setPhoto(photoUrl);
        }

        userRepository.save(user);

        // ✅ Response
        UpdateMeResponse response = new UpdateMeResponse();
        response.setUsername(user.getRealUsername());
        response.setEmail(user.getEmail());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setAddress(user.getAddress());
        response.setBio(user.getBio());
        response.setPhoto(user.getPhoto());

        return response;
    }

    @Override
    public UserResponse createUser(UserRequest requestDTO) {
        return null;
    }

    @Override
    @Transactional
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return userMapper.toResponseDTO(user);
    }

    @Override
    public PageResponse<UserResponse> getAllUsers(Pageable pageable) {
        Page<User> userPage = userRepository.findAll(pageable);
        Page<UserResponse> userResponsePage = userPage.map(userMapper::toResponseDTO);
        return PageResponse.of(userResponsePage);
    }


}
