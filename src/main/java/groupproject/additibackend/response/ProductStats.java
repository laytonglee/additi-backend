package groupproject.additibackend.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductStats {
    private Long totalProducts;
    private Long activeProducts;
    private Long inactiveProducts;
    private Long totalCategories;
}
