package groupproject.additibackend.service.impl;

import groupproject.additibackend.model.User;
import groupproject.additibackend.repository.UserRepository;
import groupproject.additibackend.request.RegisterRequest;
import groupproject.additibackend.request.UpdateMeRequest;
import groupproject.additibackend.response.MeResponse;
import groupproject.additibackend.response.RegisterResponse;
import groupproject.additibackend.service.R2StorageService;
import groupproject.additibackend.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private  final R2StorageService r2StorageService;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, R2StorageService r2StorageService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.r2StorageService = r2StorageService;
    }

//    @Override
//    public RegisterResponse registerUser(RegisterRequest request) {
//        User user = new User();
//        user.setUsername(request.getUsername());
//        user.setEmail(request.getEmail());
//        user.setPhoneNumber(request.getPhoneNumber());
//        user.setAddress(request.getAddress());
//        user.setBio(request.getBio());
//        user.setPassword(passwordEncoder.encode(request.getPassword()));
//
//        userRepository.save(user);
//
//        RegisterResponse response = new RegisterResponse();
//        response.setUsername(user.getUsername());
//        response.setEmail(user.getEmail());
//        response.setMessage("Registered successfully");
//
//        return response;
//    }

    @Override
    public RegisterResponse registerUser(RegisterRequest request, MultipartFile photo) throws IOException {

        String photoKey = null;
        String photoUrl = null;

        if (photo != null && !photo.isEmpty()) {
            photoKey = r2StorageService.uploadFile(photo, "users/avatar");
            photoUrl = r2StorageService.getPublicUrl(photoKey);
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setAddress(request.getAddress());
        user.setBio(request.getBio());

        user.setPhoto(photoUrl); // ✅ URL stored
        user.setPhotoKey(photoKey);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);

        RegisterResponse response = new RegisterResponse();
        response.setUsername(user.getRealUsername());
        response.setEmail(user.getEmail());
        response.setMessage("Registered successfully");

        return response;
    }


    @Override
    public MeResponse updateMe(Authentication authentication, UpdateMeRequest request) {
        // Because getUsername() returns email
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setAddress(request.getAddress());
        user.setPhoto(request.getPhoto());

        userRepository.save(user);

        return MeResponse.from(user);
    }

}
