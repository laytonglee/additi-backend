package groupproject.additibackend.repository;

import groupproject.additibackend.model.ProductImage;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductImageRepository {

    List<ProductImage> findByVariantId(Long variantId);

    void deleteByVariantId(Long variantId);

    Optional<ProductImage> findByImageKey(String imageKey);
}
