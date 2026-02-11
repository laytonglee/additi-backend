package groupproject.additibackend.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductUpdateRequest {

    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 255, message = "Product name must be between 2 and 255 characters")
    private String name;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @DecimalMax(value = "999999.99", message = "Price must be less than 1,000,000")
    @Digits(integer = 6, fraction = 2, message = "Price must have at most 6 digits and 2 decimal places")
    private BigDecimal price;

    @Size(max = 100, message = "Brand name must not exceed 100 characters")
    private String brand;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @Valid
    @NotEmpty(message = "Product must have at least one variant")
    private List<ProductVariantUpdateRequest> variants;

}
