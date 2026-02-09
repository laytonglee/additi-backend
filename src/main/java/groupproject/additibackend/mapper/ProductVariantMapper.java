package groupproject.additibackend.mapper;

import groupproject.additibackend.model.ProductVariant;
import groupproject.additibackend.request.ProductVariantRequest;
import groupproject.additibackend.response.ProductVariantResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
public class ProductVariantMapper {
    private final ProductImageMapper imageMapper;

    public static ProductVariant toProductVariantEntity(ProductVariantRequest request) {
        ProductVariant variant = new ProductVariant();
        variant.setSize(request.getSize());
        variant.setColor(request.getColor());
        variant.setSku(request.getSku());
        variant.setStockQuantity(request.getStockQuantity());
        variant.setPriceAdjustment(request.getPriceAdjustment());
        variant.setIsAvailable(true);
        variant.setCreatedAt(LocalDateTime.now());
        variant.setUpdatedAt(LocalDateTime.now());
        variant.setImages(new HashSet<>());
        return variant;
    }

    public ProductVariantResponse toResponse(ProductVariant variant) {
        return ProductVariantResponse.builder()
                .id(variant.getId())
                .size(variant.getSize())
                .color(variant.getColor())
                .sku(variant.getSku())
                .stockQuantity(variant.getStockQuantity())
                .priceAdjustment(variant.getPriceAdjustment())
                .finalPrice(variant.getFinalPrice())
                .isAvailable(variant.getIsAvailable())
                .createdAt(variant.getCreatedAt())
                .images(variant.getImages().stream()
                        .map(imageMapper::toProductImageResponse)
                        .collect(Collectors.toList()))
                .build();
    }


}
