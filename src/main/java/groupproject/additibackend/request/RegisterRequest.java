package groupproject.additibackend.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Getter
@Setter
@RequiredArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    @NotBlank
    private String username;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String phoneNumber;

    private String address;

    private String bio;

    private String photo;

    @NotBlank
    @Size(min=6)
    private String password;

    @NotBlank
    private String confirmPassword;

}
