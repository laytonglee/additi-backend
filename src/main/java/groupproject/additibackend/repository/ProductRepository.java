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
    SELECT p.*
    FROM products p
    JOIN categories c ON c.id = p.category_id
    WHERE (
        :search IS NULL
        OR p.name ILIKE CONCAT('%', CAST(:search AS text), '%')
        OR p.brand ILIKE CONCAT('%', CAST(:search AS text), '%')
    )
    AND (:categorySlug IS NULL OR c.slug = :categorySlug)
    AND (:minPrice IS NULL OR p.price >= :minPrice)
    AND (:maxPrice IS NULL OR p.price <= :maxPrice)
    AND (:startDate IS NULL OR CAST(p.created_at AS date) >= :startDate)
    AND (:endDate IS NULL OR CAST(p.created_at AS date) <= :endDate)
    """,
            countQuery = """
    SELECT COUNT(*)
    FROM products p
    JOIN categories c ON c.id = p.category_id
    WHERE (
        :search IS NULL
        OR p.name ILIKE CONCAT('%', CAST(:search AS text), '%')
        OR p.brand ILIKE CONCAT('%', CAST(:search AS text), '%')
    )
    AND (:categorySlug IS NULL OR c.slug = :categorySlug)
    AND (:minPrice IS NULL OR p.price >= :minPrice)
    AND (:maxPrice IS NULL OR p.price <= :maxPrice)
    AND (:startDate IS NULL OR CAST(p.created_at AS date) >= :startDate)
    AND (:endDate IS NULL OR CAST(p.created_at AS date) <= :endDate)
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
            Pageable pageable
    );

    @Query(value = """
   SELECT p.id
    FROM products p
   WHERE p.category_id = (SELECT category_id FROM products WHERE id = :productId)
    AND p.id <> :productId
    AND p.is_active = true
  ORDER BY p.created_at DESC
  LIMIT :limit
""", nativeQuery = true)
    List<Long> findRelatedIdsByCategory(
            @Param("productId") Long productId,
            @Param("limit") int limit
    );


    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN FETCH p.productVariants v " +
            "LEFT JOIN FETCH v.images " +
            "WHERE p.id = :id")
    Optional<Product> findProductById(@Param("id") Long id);

}
