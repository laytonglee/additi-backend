package groupproject.additibackend.service.impl;

import java.io.IOException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import groupproject.additibackend.model.User;
import groupproject.additibackend.repository.UserRepository;
import groupproject.additibackend.request.RegisterRequest;
import groupproject.additibackend.response.RegisterResponse;
import groupproject.additibackend.service.R2StorageService;
import groupproject.additibackend.service.RegisterService;

@Service
public class RegisterServiceImpl implements RegisterService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final R2StorageService r2StorageService;

    public RegisterServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, R2StorageService r2StorageService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.r2StorageService = r2StorageService;
    }

    @Override
    public RegisterResponse registerUser(RegisterRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setAddress(request.getAddress());
        user.setBio(request.getBio());
        
        // Upload photo if provided
        if (request.getPhoto() != null && !request.getPhoto().isEmpty()) {
            try {
                String photoUrl = r2StorageService.uploadFile(request.getPhoto(), "users");
                user.setPhoto(photoUrl);
            } catch (IOException e) {
                throw new RuntimeException("Failed to upload photo", e);
            }
        }
        
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);

        RegisterResponse response = new RegisterResponse();
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setMessage("Registered successfully");

        return response;
    }

}
