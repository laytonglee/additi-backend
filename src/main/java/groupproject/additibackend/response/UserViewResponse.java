package groupproject.additibackend.response;

import groupproject.additibackend.model.Role;
import lombok.Data;

import java.util.Set;

@Data
public class UserViewResponse {

    private Long id;
    private String email;
    private String username;
    private Set<String> role;
}
