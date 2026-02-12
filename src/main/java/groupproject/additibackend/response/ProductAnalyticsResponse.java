package groupproject.additibackend.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductAnalyticsResponse {
    private ProductStats productStats;
    private PriceStats priceStats;
    private List<CategoryStats> categoryStats;
    private List<ProductResponse> recentProducts;
    private List<ProductResponse> topProducts;
}

