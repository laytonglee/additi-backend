package groupproject.additibackend.response;

import lombok.Data;


@Data
public class UpdateMeResponse {

    private String username;
    private String email;
    private String phoneNumber;
    private String address;
    private String bio;

    private String photo; // URL or filename
}

