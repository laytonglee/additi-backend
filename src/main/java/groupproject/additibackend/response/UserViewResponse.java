package groupproject.additibackend.response;

import java.util.Set;

import lombok.Data;

@Data
public class UserViewResponse {

    private Long id;
    private String email;
    private String username;
    private Set<String> role;
}
