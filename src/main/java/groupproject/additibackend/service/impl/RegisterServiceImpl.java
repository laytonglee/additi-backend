package groupproject.additibackend.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import groupproject.additibackend.model.User;
import groupproject.additibackend.repository.UserRepository;
import groupproject.additibackend.request.RegisterRequest;
import groupproject.additibackend.response.RegisterResponse;
import groupproject.additibackend.service.RegisterService;

@Service
public class RegisterServiceImpl implements RegisterService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public RegisterResponse registerUser(RegisterRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setAddress(request.getAddress());
        user.setBio(request.getBio());
        user.setPhoto(request.getPhoto());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);

        RegisterResponse response = new RegisterResponse();
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setMessage("Registered successfully");

        return response;
    }

}
