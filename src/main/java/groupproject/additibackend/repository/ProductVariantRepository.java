package groupproject.additibackend.repository;

import groupproject.additibackend.model.ProductVariant;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository {

    List<ProductVariant> findByProductId(Long productId);

    Optional<ProductVariant> findBySku(String sku);


}
