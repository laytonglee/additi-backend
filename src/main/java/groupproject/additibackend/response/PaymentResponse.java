package groupproject.additibackend.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class PaymentResponse {
    private Long id;
    private Long orderId;
    private String method;
    private String status;
    private BigDecimal amount;
    private String transactionId;
    private String khqrCode;
    private String md5Hash; // Used to verify payment with Bakong
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
}
