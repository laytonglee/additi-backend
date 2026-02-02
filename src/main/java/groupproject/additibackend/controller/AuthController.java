package groupproject.additibackend.controller;

import groupproject.additibackend.model.User;
import groupproject.additibackend.request.AuthLoginRequest;
import groupproject.additibackend.response.AuthResponse;

import groupproject.additibackend.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login (@RequestBody AuthLoginRequest loginRequest){
        return new ResponseEntity<>(authService.login(loginRequest), HttpStatus.OK);
    }
}
