package groupproject.additibackend.khqr;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KHQRData {
    private String qr;
    private String md5;
}
