package groupproject.additibackend.request;

import jakarta.validation.constraints.Email;
import lombok.Data;
import org.jspecify.annotations.Nullable;

@Data
public class UpdateMeRequest {
    private String username;

    @Nullable
    @Email
    private String email;

    private String phoneNumber;
    private String address;
    private String bio;

    private String password;
    private String confirmPassword;
}

