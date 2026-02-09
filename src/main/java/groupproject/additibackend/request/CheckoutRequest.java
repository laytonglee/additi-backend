package groupproject.additibackend.request;

import java.util.List;

import lombok.Data;

@Data
public class CheckoutRequest {
    private String shippingAddress;
    private String phoneNumber;
    private String paymentMethod;
    private List<CartItemRequest> items;

    @Data
    public static class CartItemRequest {
        private Long productId;
        private Long productVariantId;
        private Integer quantity;
    }
}
