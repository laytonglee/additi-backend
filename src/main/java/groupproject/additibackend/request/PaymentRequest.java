package groupproject.additibackend.request;

import lombok.Data;

@Data
public class PaymentRequest {
    private String method; // KHQR or CASH_ON_DELIVERY
    private String khqrCode; // Optional, only for KHQR
}
