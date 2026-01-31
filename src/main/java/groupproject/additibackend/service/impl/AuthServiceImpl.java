package groupproject.additibackend.service.impl;

import groupproject.additibackend.model.User;
import groupproject.additibackend.repository.UserRepository;
import groupproject.additibackend.request.AuthLoginRequest;
import groupproject.additibackend.response.AuthResponse;
import groupproject.additibackend.response.UserViewResponse;
import groupproject.additibackend.service.AuthService;
import groupproject.additibackend.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

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
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(), request.getPassword()));

        User user = (User) authentication.getPrincipal();

        String jwtToken = jwtService.generateAccessToken(user);

        UserViewResponse userViewResponse = new UserViewResponse();
        userViewResponse.setId(user.getId());
        userViewResponse.setUsername(user.getUsername());
//        userViewResponse.setRoles(user.getRoles());
        userViewResponse.setToken(jwtToken);

        AuthResponse authResponse = new AuthResponse();
        authResponse.setUser(userViewResponse);

        return authResponse;

    }
}
