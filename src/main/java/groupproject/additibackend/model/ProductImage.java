package groupproject.additibackend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDateTime;
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

    @NotBlank(message = "Image URL is required")
    @URL(message = "Image URL must be a valid URL")
    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    @Column(nullable = false, length = 500)
    private String imageUrl;


    @NotBlank(message = "Image key is required")
    @Size(max = 255, message = "Image key must not exceed 255 characters")
    @Pattern(regexp = "^[a-zA-Z0-9/_.-]+$", message = "Image key contains invalid characters")
    @Column(nullable = false, length = 255)
    private String imageKey;




    @Column(updatable = false)
    private LocalDateTime uploadedAt;

    @ManyToOne
    @JoinColumn(name = "variant_id")
    private ProductVariant variant;

}

