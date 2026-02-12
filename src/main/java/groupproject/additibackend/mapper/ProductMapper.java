package groupproject.additibackend.mapper;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import groupproject.additibackend.model.User;
import org.springframework.stereotype.Component;

import groupproject.additibackend.model.Category;
import groupproject.additibackend.model.Product;
import groupproject.additibackend.model.ProductStatus;
import groupproject.additibackend.request.ProductCreateRequest;
import groupproject.additibackend.response.ProductResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProductMapper {
    private  final ProductVariantMapper productVariantMapper;
    private final  CategoryMapper categoryMapper;

    public Product toProductEntity(ProductCreateRequest request, Category category) {
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setBrand(request.getBrand());
        product.setCategory(category);
        product.setIsActive(true);
        product.setIsFeatured(false);
        product.setStatus(ProductStatus.ACTIVE);
        product.setSalesCount(0);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        if (request.getVariants() != null) {
            product.setProductVariants(
                    request.getVariants().stream()
                            .map(ProductVariantMapper::toProductVariantEntity)
                            .collect(Collectors.toList())
            );
        }

        return product;


    }

    public ProductResponse toResponse(Product product) {
        ProductResponse res = ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .brand(product.getBrand())
                .isActive(product.getIsActive())
                .isFeatured(product.getIsFeatured())
                .featuredOrder(product.getFeaturedOrder())
                .status(product.getStatus())
                .availableDate(product.getAvailableDate())
                .salesCount(product.getSalesCount())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .category(categoryMapper.toCategoryResponse(product.getCategory()))
                .variants(product.getProductVariants().stream()
                        .map(productVariantMapper::toResponse)
                        .collect(Collectors.toList()))
                .build();

        if (product.getCreatedBy() != null) {
            User u = product.getCreatedBy();
            res.setCreatedById(u.getId());
            res.setCreatedByEmail(u.getEmail());
            res.setCreatedByUsername(u.getRealUsername());
            res.setCreatedByPhoto(u.getPhoto());
        }

        return res;
    }




}
