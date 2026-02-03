package groupproject.additibackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Entity
@Table(name="product_variants")
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String size;
    private String color;
    private String Sku;
    private Integer stockQuantity;
    private BigDecimal priceAdjustment;

    @ManyToOne
    @JoinColumn(name="product_id")
    private Product product;

}
