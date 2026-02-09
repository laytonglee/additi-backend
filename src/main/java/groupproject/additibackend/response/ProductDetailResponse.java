package groupproject.additibackend.response;

import java.util.List;

public record ProductDetailResponse(
            ProductResponse product,
            List<ProductResponse> relatedProducts
    ) {
    
}
