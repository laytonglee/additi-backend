package groupproject.additibackend.request;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class UpdateMeRequest {
    private String username;
    private String email;
    private String phoneNumber;
    private String address;
    private String photo;
}
