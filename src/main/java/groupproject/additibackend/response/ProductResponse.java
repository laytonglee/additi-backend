package groupproject.additibackend.response;

import java.math.BigDecimal;
import java.util.List;

public record ProductResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        String brand,
        Boolean isActive,
        CategoryResponse category,
        List<VariantResponse> variants
) {}
