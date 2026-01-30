package groupproject.additibackend.service.impl;

import groupproject.additibackend.model.User;
import groupproject.additibackend.repository.UserRepository;
import groupproject.additibackend.request.AuthLoginRequest;
import groupproject.additibackend.response.AuthResponse;
import groupproject.additibackend.response.UserViewResponse;
import groupproject.additibackend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;

    public AuthServiceImpl(AuthenticationManager authenticationManager, UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
    }

    @Override
    public AuthResponse login(AuthLoginRequest request){
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(), request.getPassword()));

        User user = (User) authentication.getPrincipal();

        UserViewResponse userViewResponse = new UserViewResponse();
        userViewResponse.setId(user.getId());
        userViewResponse.setUsername(user.getUsername());
        userViewResponse.setRoles(user.getRoles());

        AuthResponse authResponse = new AuthResponse();
        authResponse.setUser(userViewResponse);

        return authResponse;

    }
}
