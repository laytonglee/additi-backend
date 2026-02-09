package groupproject.additibackend.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageResponse{

    private Long id;
    private String imageUrl;
    private String imageKey;
    private LocalDateTime uploadedAt;

}
