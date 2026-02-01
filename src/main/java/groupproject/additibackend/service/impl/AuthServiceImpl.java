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
    public AuthResponse login(AuthLoginRequest request){
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        User user = userRepository.findByEmail(request.getUsername()).orElse(null);

        AuthResponse authResponse = new AuthResponse();
        authResponse.setAccessToken(jwtService.getRefreshToken(user));
        authResponse.setRefreshToken(jwtService.getRefreshToken(user));
        authResponse.setType("Bearer");

        UserViewResponse userViewResponse = new UserViewResponse();
        userViewResponse.setId(user.getId());
        userViewResponse.setEmail(user.getEmail());
        userViewResponse.setRole(user.getRoles().stream().map(role -> role.getName()).collect(Collectors.toSet()));

        authResponse.setUser(userViewResponse);
        return authResponse;

    }
}
