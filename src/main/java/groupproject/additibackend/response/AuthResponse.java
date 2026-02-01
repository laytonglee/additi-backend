package groupproject.additibackend.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AuthResponse {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("type")
    private String type = "Bearer";

    @JsonProperty("expires_in")
    private String expiresIn;

    @JsonProperty("user")
    private UserViewResponse user;

}
