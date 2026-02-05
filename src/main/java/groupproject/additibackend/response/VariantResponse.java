package groupproject.additibackend.response;

import java.math.BigDecimal;
import java.util.List;

public record VariantResponse(
        Long id,
        String size,
        String color,
        String sku,
        Integer stockQuantity,
        BigDecimal priceAdjustment,
        Boolean isAvailable,
        BigDecimal finalPrice,
        List<ImageResponse> images
) {}

