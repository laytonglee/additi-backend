package groupproject.additibackend.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductVariantResponse {

    private Long id;
    private String size;
    private String color;
    private String sku;
    private Integer stockQuantity;
    private BigDecimal priceAdjustment;
    private BigDecimal finalPrice;
    private Boolean isAvailable;
    private LocalDateTime createdAt;
    private List<ProductImageResponse> images;

}

