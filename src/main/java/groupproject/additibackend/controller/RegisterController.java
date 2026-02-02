package groupproject.additibackend.controller;

import groupproject.additibackend.request.RegisterRequest;
import groupproject.additibackend.response.RegisterResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reister")
public class RegisterController {

    public ResponseEntity<RegisterResponse> registerUser(@RequestBody RegisterRequest request) {
        return null;
    }
}
