package groupproject.additibackend.repository;

import groupproject.additibackend.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query(
            value = """
SELECT DISTINCT p.*
FROM products p
JOIN categories c ON c.id = p.category_id
LEFT JOIN product_variants pv
  ON pv.product_id = p.id
 AND (:size IS NULL OR pv.size = :size)
 AND (:color IS NULL OR pv.color = :color)
WHERE (
    :search IS NULL
    OR p.name ILIKE '%' || :search || '%'
    OR p.brand ILIKE '%' || :search || '%'
)
AND (:categorySlug IS NULL OR c.slug = :categorySlug)
AND (:minPrice IS NULL OR p.price >= :minPrice)
AND (:maxPrice IS NULL OR p.price <= :maxPrice)
AND (:startDate IS NULL OR p.created_at >= :startDate)
AND (:endDate IS NULL OR p.created_at < :endDate + INTERVAL '1 day')
AND (:createdById IS NULL OR p.created_by_user_id = :createdById)
""",
            countQuery = """
SELECT COUNT(DISTINCT p.id)
FROM products p
JOIN categories c ON c.id = p.category_id
LEFT JOIN product_variants pv
  ON pv.product_id = p.id
 AND (:size IS NULL OR pv.size = :size)
 AND (:color IS NULL OR pv.color = :color)
WHERE (
    :search IS NULL
    OR p.name ILIKE '%' || :search || '%'
    OR p.brand ILIKE '%' || :search || '%'
)
AND (:categorySlug IS NULL OR c.slug = :categorySlug)
AND (:minPrice IS NULL OR p.price >= :minPrice)
AND (:maxPrice IS NULL OR p.price <= :maxPrice)
AND (:startDate IS NULL OR p.created_at >= :startDate)
AND (:endDate IS NULL OR p.created_at < :endDate + INTERVAL '1 day')
AND (:createdById IS NULL OR p.created_by_user_id = :createdById)
""",
            nativeQuery = true
    )
    Page<Product> findByFilters(
            @Param("search") String search,
            @Param("categorySlug") String categorySlug,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("size") String size,
            @Param("color") String color,
            @Param("createdById") Long createdById,
            Pageable pageable
    );


    @Query("""
    select p from Product p
    where p.isActive = true
      and p.category.id = :categoryId
      and p.id <> :productId
    order by p.createdAt desc
  """)
    List<Product> findRelatedByCategory(
            @Param("categoryId") Long categoryId,
            @Param("productId") Long productId,
            Pageable pageable
    );


    @Query("""
SELECT DISTINCT p FROM Product p
LEFT JOIN FETCH p.createdBy cb
LEFT JOIN FETCH p.productVariants v
LEFT JOIN FETCH v.images
WHERE p.id = :id
""")
    Optional<Product> findProductById(@Param("id") Long id);


    // count product by category
    Long countByCategoryId(Long categoryId);

}
