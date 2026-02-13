package groupproject.additibackend.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponse {
    private Long id;
    private Long userId;
    private String userEmail;
    private String username;
    private List<OrderItemResponse> items;
    private BigDecimal totalAmount;
    private String status;
    private String shippingAddress;
    private String phoneNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    public static class OrderItemResponse {
        private Long id;
        private Long productId;
        private String productName;
        private Long productVariantId;
        private Integer quantity;
        private BigDecimal price;
        private BigDecimal subtotal;
    }
}
