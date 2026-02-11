package groupproject.additibackend.controller;

import groupproject.additibackend.request.RegisterRequest;
import groupproject.additibackend.request.UpdateMeRequest;
import groupproject.additibackend.response.MeResponse;
import groupproject.additibackend.response.RegisterResponse;
import groupproject.additibackend.response.UpdateMeResponse;
import groupproject.additibackend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public RegisterResponse registerUser(
            @ModelAttribute RegisterRequest request,
            @RequestPart(value = "photo", required = false) MultipartFile photo
    ) throws IOException {
        return userService.registerUser(request, photo);
    }

    @PutMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UpdateMeResponse updateMe(
            Authentication authentication,
            @ModelAttribute UpdateMeRequest request,
            @RequestPart(value = "photo", required = false) MultipartFile photo
    ) throws IOException {
        return userService.updateMe(authentication, request, photo);
    }


}
