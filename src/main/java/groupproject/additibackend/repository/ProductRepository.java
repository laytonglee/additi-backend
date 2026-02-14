package groupproject.additibackend.repository;

import groupproject.additibackend.model.Order;
import groupproject.additibackend.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    interface BestSellerProjection {
        Product getProduct();
        Long getTotalSold();
    }

    interface ProductSalesProjection {
        Long getProductId();
        Long getTotalSold();
    }

    @Query(
            value = """
SELECT DISTINCT p.*
FROM products p
JOIN categories c ON c.id = p.category_id
LEFT JOIN product_variants pv
  ON pv.product_id = p.id
 AND (:size IS NULL OR LOWER(pv.size) = LOWER(:size))
 AND (:color IS NULL OR LOWER(pv.color) = LOWER(:color))
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
AND ((:size IS NULL AND :color IS NULL) OR pv.id IS NOT NULL)
""",
            countQuery = """
SELECT COUNT(DISTINCT p.id)
FROM products p
JOIN categories c ON c.id = p.category_id
LEFT JOIN product_variants pv
  ON pv.product_id = p.id
 AND (:size IS NULL OR LOWER(pv.size) = LOWER(:size))
 AND (:color IS NULL OR LOWER(pv.color) = LOWER(:color))
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
AND ((:size IS NULL AND :color IS NULL) OR pv.id IS NOT NULL)
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

    @Query("""
    select oi.product as product, coalesce(sum(oi.quantity), 0) as totalSold
    from OrderItem oi
    join oi.order o
    where oi.product.isActive = true
      and oi.product.status = groupproject.additibackend.model.ProductStatus.ACTIVE
      and o.status in :statuses
    group by oi.product
    order by totalSold desc, max(o.createdAt) desc
    """)
    List<BestSellerProjection> findBestSellersByOrderStatuses(
            @Param("statuses") List<Order.OrderStatus> statuses,
            Pageable pageable
    );

    @Query("""
    select oi.product.id as productId, coalesce(sum(oi.quantity), 0) as totalSold
    from OrderItem oi
    join oi.order o
    where oi.product.id in :productIds
      and o.status in :statuses
    group by oi.product.id
    """)
    List<ProductSalesProjection> findProductSalesByIdsAndOrderStatuses(
            @Param("productIds") List<Long> productIds,
            @Param("statuses") List<Order.OrderStatus> statuses
    );

    @Query("""
    select p from Product p
    where p.isActive = true
      and p.isFeatured = true
    order by
      case when p.featuredOrder is null then 1 else 0 end,
      p.featuredOrder asc,
      p.createdAt desc
    """)
    List<Product> findFeaturedProducts();

    @Query("""
    select p from Product p
    where p.isActive = true
      and p.status = groupproject.additibackend.model.ProductStatus.COMING_SOON
    order by
      case when p.availableDate is null then 1 else 0 end,
      p.availableDate asc,
      p.createdAt desc
    """)
    List<Product> findComingSoonProducts();

    // count product by category
    Long countByCategoryId(Long categoryId);


    // === Analytics Queries ===

    // Count products by status
    Long countByIsActive(Boolean isActive);

    // Find recent products
    Page<Product> findTop10ByOrderByCreatedAtDesc(Pageable pageable);

    // Price statistics
    @Query("SELECT MIN(p.price) FROM Product p WHERE p.isActive = true")
    BigDecimal findMinPrice();

    @Query("SELECT MAX(p.price) FROM Product p WHERE p.isActive = true")
    BigDecimal findMaxPrice();

    @Query("SELECT AVG(p.price) FROM Product p WHERE p.isActive = true")
    BigDecimal findAvgPrice();

    @Query("SELECT SUM(p.price) FROM Product p WHERE p.isActive = true")
    BigDecimal findTotalValue();

    // Category statistics
    @Query("SELECT new map(c.name as categoryName, c.slug as categorySlug, " +
            "COUNT(p) as productCount, " +
            "SUM(p.price) as totalValue, " +
            "AVG(p.price) as avgPrice) " +
            "FROM Product p JOIN p.category c " +
            "WHERE p.isActive = true " +
            "GROUP BY c.id, c.name, c.slug " +
            "ORDER BY COUNT(p) DESC")
    List<Map<String, Object>> findCategoryStatistics();

    // Products created in date range
    @Query("SELECT COUNT(p) FROM Product p WHERE p.createdAt BETWEEN :startDate AND :endDate")
    Long countProductsCreatedBetween(@Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate);

    // Products by price range
    @Query("SELECT COUNT(p) FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice")
    Long countProductsByPriceRange(@Param("minPrice") BigDecimal minPrice,
                                   @Param("maxPrice") BigDecimal maxPrice);

}
