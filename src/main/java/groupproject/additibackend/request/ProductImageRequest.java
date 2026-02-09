package groupproject.additibackend.request;


import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductImageRequest {

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

}
