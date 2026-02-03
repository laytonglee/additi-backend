package groupproject.additibackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Entity
@Table(name="product_images")
public class ProductImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String imageUrl;
    private String imageKey;
    private String color;
    private Integer displayOrder;

    @Temporal(TemporalType.TIMESTAMP)
    private Date uploadedAt;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
}

