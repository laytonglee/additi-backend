package groupproject.additibackend.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryStats {
    private String categoryName;
    private String categorySlug;
    private Long productCount;
    private BigDecimal totalValue;
    private BigDecimal avgPrice;
}
