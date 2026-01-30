package groupproject.additibackend.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AuthResponse {

    @JsonProperty("user")
    private UserViewResponse user;


}
