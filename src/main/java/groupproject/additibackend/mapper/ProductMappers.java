package groupproject.additibackend.mapper;

import groupproject.additibackend.model.Product;
import groupproject.additibackend.model.ProductVariant;
import groupproject.additibackend.response.CategoryResponse;
import groupproject.additibackend.response.ImageResponse;
import groupproject.additibackend.response.ProductResponse;
import groupproject.additibackend.response.VariantResponse;

import java.util.List;

public class ProductMappers {

    public static ProductResponse toDetail(Product p) {
        return new ProductResponse(
                p.getId(),
                p.getName(),
                p.getDescription(),
                p.getPrice(),
                p.getBrand(),
                p.getIsActive(),
                new CategoryResponse(
                        p.getCategory().getId(),
                        p.getCategory().getName(),
                        p.getCategory().getSlug()
                ),
                p.getProductVariants().stream().map(ProductMappers::toVariant).toList()
        );
    }

    private static VariantResponse toVariant(ProductVariant v) {
        List<ImageResponse> images = v.getImages().stream()
                .map(i -> new ImageResponse(i.getId(), i.getImageUrl(), i.getImageKey()))
                .toList();

        return new VariantResponse(
                v.getId(),
                v.getSize(),
                v.getColor(),
                v.getSku(),
                v.getStockQuantity(),
                v.getPriceAdjustment(),
                v.getIsAvailable(),
                v.getFinalPrice(),
                images
        );
    }
}
