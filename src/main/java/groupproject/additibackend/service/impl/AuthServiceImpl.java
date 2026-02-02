package groupproject.additibackend.service.impl;

import groupproject.additibackend.model.User;
import groupproject.additibackend.repository.UserRepository;
import groupproject.additibackend.request.AuthLoginRequest;
import groupproject.additibackend.response.AuthResponse;
import groupproject.additibackend.response.UserViewResponse;
import groupproject.additibackend.service.AuthService;
import groupproject.additibackend.service.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public AuthServiceImpl(AuthenticationManager authenticationManager, UserRepository userRepository, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @Override
    public AuthResponse login(AuthLoginRequest request) {

        // 1️⃣ Authenticate credentials
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // 2️⃣ Load user safely
        User user = userRepository.findByEmail(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 3️⃣ Generate ACCESS token only
        String accessToken = jwtService.getAccessToken(user);

        // 4️⃣ Build response
        AuthResponse authResponse = new AuthResponse();
        authResponse.setAccessToken(accessToken);
        authResponse.setType("Bearer");

        // 5️⃣ User view
        UserViewResponse userView = new UserViewResponse();
        userView.setId(user.getId());
        userView.setEmail(user.getEmail());
        userView.setRole(
                user.getRoles()
                        .stream()
                        .map(role -> role.getName())
                        .collect(Collectors.toSet())
        );

        authResponse.setUser(userView);
        return authResponse;
    }

}
