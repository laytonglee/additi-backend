package groupproject.additibackend.controller;

import groupproject.additibackend.request.RegisterRequest;
import groupproject.additibackend.request.UpdateMeRequest;
import groupproject.additibackend.response.MeResponse;
import groupproject.additibackend.response.RegisterResponse;
import groupproject.additibackend.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> registerUser(@Validated @RequestBody RegisterRequest request) {
        RegisterResponse response = userService.registerUser(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/me")
    public ResponseEntity<MeResponse> editUser(Authentication authentication,@RequestBody UpdateMeRequest request){
        MeResponse response = userService.updateMe(authentication, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
