package groupproject.additibackend.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentResponse {
    private Long id;
    private Long orderId;
    private String method;
    private String status;
    private BigDecimal amount;
    private String transactionId;
    private String khqrCode;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
}
