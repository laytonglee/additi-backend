package groupproject.additibackend.response;

import groupproject.additibackend.model.User;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MeResponse {
    @Nullable
    private String username;

    @Nullable
    private String email;

    @Nullable
    private String photo;

    @Nullable
    private String phoneNumber;

    @Nullable
    private String address;


    public static MeResponse from(User user) {
        return new MeResponse(
                user.getRealUsername(),
                user.getEmail(),
                user.getPhoto(),
                user.getPhoneNumber(),
                user.getAddress()
        );
    }
}
