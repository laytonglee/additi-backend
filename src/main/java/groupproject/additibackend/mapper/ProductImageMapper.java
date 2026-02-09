package groupproject.additibackend.mapper;

import groupproject.additibackend.model.ProductImage;
import groupproject.additibackend.response.ProductImageResponse;
import org.springframework.stereotype.Component;

@Component
public class ProductImageMapper {

    public ProductImageResponse toProductImageResponse(ProductImage productImage) {
        return ProductImageResponse.builder()
                .id(productImage.getId())
                .imageUrl(productImage.getImageUrl())
                .imageKey(productImage.getImageKey())
                .uploadedAt(productImage.getUploadedAt())
                .build();
    }

}
