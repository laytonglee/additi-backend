package groupproject.additibackend.request;


import groupproject.additibackend.model.ProductImage;
import jakarta.persistence.Column;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductVariantRequest {

    @NotBlank(message = "Size is required")
    @Size(min = 1, max = 50, message = "Size must be between 1 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9\\s./-]+$", message = "Size contains invalid characters")
    @Column(nullable = false, length = 50)
    private String size;

    @NotBlank(message = "Color is required")
    @Size(min = 2, max = 50, message = "Color must be between 2 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9\\s#()-]+$", message = "Color contains invalid characters")
    @Column(nullable = false, length = 50)
    private String color;

    @NotBlank(message = "SKU is required")
    @Size(min = 3, max = 100, message = "SKU must be between 3 and 100 characters")
    @Pattern(regexp = "^[A-Z0-9-_]+$", message = "SKU must contain only uppercase letters, numbers, hyphens, and underscores")
    @Column(unique = true, nullable = false, length = 100)
    private String sku;

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    @Max(value = 999999, message = "Stock quantity cannot exceed 999,999")
    @Column(nullable = false)
    private Integer stockQuantity = 0;

    @NotNull(message = "Price adjustment is required")
    @DecimalMin(value = "-99999.99", message = "Price adjustment cannot be less than -99,999.99")
    @DecimalMax(value = "99999.99", message = "Price adjustment cannot exceed 99,999.99")
    @Digits(integer = 5, fraction = 2, message = "Price adjustment must have at most 5 digits and 2 decimal places")
    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal priceAdjustment = BigDecimal.ZERO;

    private List<ProductImageRequest>  images;

}
