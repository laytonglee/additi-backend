package groupproject.additibackend.model;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name="products", indexes = {
        @Index(name="idx_product_name",columnList = "name"),
        @Index(name="idx_product_category",columnList = "category_id"),
        @Index(name="idx_product_active" , columnList = "is_active")
})
public class Product {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;
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

    @NotNull(message = "Active status is required")
    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Column(updatable = false)
    private LocalDateTime updatedAt;

    @NotNull(message = "Category is required")
    @ManyToOne
    @JoinColumn(name ="category_id",nullable = false)
    private Category category;

    @Valid
    @Size(min = 1, message = "Product must have at least one variant")
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductVariant> productVariants = new ArrayList<>();

    public void addVariant(ProductVariant variant) {
        productVariants.add(variant);
        variant.setProduct(this);
    }

    public void removeVariant(ProductVariant variant) {
        productVariants.remove(variant);
        variant.setProduct(null);
    }


}
