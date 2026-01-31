package groupproject.additibackend.response;

import groupproject.additibackend.model.Role;
import lombok.Data;

import java.util.Set;

@Data
public class UserViewResponse {

    private Long id;
    private String username;
//    private Set<Role> roles;
    private String token;
}
