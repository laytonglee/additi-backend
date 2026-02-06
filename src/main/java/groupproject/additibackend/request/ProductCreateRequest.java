package groupproject.additibackend.request;

import jakarta.persistence.Column;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreateRequest {

    @NotBlank(message = "Product name is required")
    @Size(min=2,max=255,message = "Product name must be between 2 and 255 characters")
    @Column(nullable = false,length = 255)
    private String name;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    @Column(length = 2000)
    private String description;


    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.00", inclusive = false, message = "Price must be greater than 0")
    @DecimalMax(value = "999999.99", message = "Price must be less than 1,000,000")
    @Digits(integer = 6, fraction = 2, message = "Price must have at most 6 digits and 2 decimal places")
    @Column(nullable = false,precision=10,scale=2)
    private BigDecimal price;

    @Size(max = 100, message = "Brand name must not exceed 100 characters")
    @Column(length = 100)
    private String brand;

    @NotNull(message = "Category Id is required")
    private  Long categoryId;

    @Valid
    @NotEmpty(message = "Product must have at least one variant")
    private List<ProductVariantRequest> variants;

}
