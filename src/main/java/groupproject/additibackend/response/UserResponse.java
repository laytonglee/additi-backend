package groupproject.additibackend.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String phoneNumber;
    private String address;
    private String photo;
    private String bio;
    private boolean enable;
    private Set<String> roles;

}
