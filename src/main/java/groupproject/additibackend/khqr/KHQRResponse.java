package groupproject.additibackend.khqr;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KHQRResponse<T> {
    private KHQRStatus khqrStatus;
    private T data;

    public static <T> KHQRResponse<T> success(T data) {
        return new KHQRResponse<>(KHQRStatus.success(), data);
    }

    public static <T> KHQRResponse<T> error(String message) {
        return new KHQRResponse<>(KHQRStatus.error(message), null);
    }

    public KHQRStatus getKHQRStatus() {
        return khqrStatus;
    }
}
