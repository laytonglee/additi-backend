package groupproject.additibackend.khqr;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KHQRStatus {
    private int code;
    private String message;

    public static KHQRStatus success() {
        return new KHQRStatus(0, "Success");
    }

    public static KHQRStatus error(String message) {
        return new KHQRStatus(1, message);
    }
}
