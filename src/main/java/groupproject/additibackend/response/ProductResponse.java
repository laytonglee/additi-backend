package groupproject.additibackend.response;
import groupproject.additibackend.model.ProductStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String thumbnailImage;
    private String brand;
    private Boolean isActive;
    private Boolean isFeatured;
    private Integer featuredOrder;
    private ProductStatus status;
    private LocalDateTime availableDate;
    private Integer salesCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdById;
    private String createdByEmail;
    private String createdByUsername;
    private String createdByPhoto;
    private CategoryResponse category;
    private List<ProductVariantResponse> variants;
}

