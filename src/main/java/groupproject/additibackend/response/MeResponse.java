package groupproject.additibackend.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MeResponse {
    private Long id;
    private String username;
    private String email;
    private String photo;
//    private String role; // optional
}
