package groupproject.additibackend.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Data
public class RegisterRequest {

    @NotBlank
    private String username;

    @Email
    @NotBlank
    private String email;

    private String phoneNumber;

    private String address;

    private String bio;

    private MultipartFile photo;

    @NotBlank
    @Size(min=6)
    private String password;

    @NotBlank
    private String confirmPassword;
}
