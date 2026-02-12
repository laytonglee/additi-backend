package groupproject.additibackend.service;

import groupproject.additibackend.mapper.ProductMapper;
import groupproject.additibackend.model.Product;
import groupproject.additibackend.repository.CategoryRepository;
import groupproject.additibackend.repository.ProductRepository;
import groupproject.additibackend.response.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductAnalyticsService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    public ProductAnalyticsResponse getDashboardAnalytics() {
        return ProductAnalyticsResponse.builder()
                .productStats(getProductStats())
                .priceStats(getPriceStats())
                .categoryStats(getCategoryStats())
                .recentProducts(getRecentProducts())
                .topProducts(getTopProducts())
                .build();
    }

    private ProductStats getProductStats() {
        Long totalProducts = productRepository.count();
        Long activeProducts = productRepository.countByIsActive(true);
        Long inactiveProducts = productRepository.countByIsActive(false);
        Long totalCategories = categoryRepository.countByIsActive(true);

        return ProductStats.builder()
                .totalProducts(totalProducts)
                .activeProducts(activeProducts)
                .inactiveProducts(inactiveProducts)
                .totalCategories(totalCategories)
                .build();
    }

    private PriceStats getPriceStats() {
        BigDecimal minPrice = productRepository.findMinPrice();
        BigDecimal maxPrice = productRepository.findMaxPrice();
        BigDecimal avgPrice = productRepository.findAvgPrice();
        BigDecimal totalValue = productRepository.findTotalValue();

        return PriceStats.builder()
                .minPrice(minPrice != null ? minPrice : BigDecimal.ZERO)
                .maxPrice(maxPrice != null ? maxPrice : BigDecimal.ZERO)
                .avgPrice(avgPrice != null ? avgPrice : BigDecimal.ZERO)
                .totalValue(totalValue != null ? totalValue : BigDecimal.ZERO)
                .build();
    }

    private List<CategoryStats> getCategoryStats() {
        List<Map<String, Object>> stats = productRepository.findCategoryStatistics();

        return stats.stream()
                .map(stat -> CategoryStats.builder()
                        .categoryName((String) stat.get("categoryName"))
                        .categorySlug((String) stat.get("categorySlug"))
                        .productCount(toLong(stat.get("productCount")))
                        .totalValue(toBigDecimal(stat.get("totalValue")))
                        .avgPrice(toBigDecimal(stat.get("avgPrice")))
                        .build())
                .toList();
    }

    private Long toLong(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(value.toString());
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }
        if (value instanceof Number number) {
            return new BigDecimal(number.toString());
        }
        return new BigDecimal(value.toString());
    }

    private List<ProductResponse> getRecentProducts() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> recentProducts = productRepository.findTop10ByOrderByCreatedAtDesc(pageable);

        return recentProducts.getContent().stream()
                .map(productMapper::toResponse)
                .toList();
    }

    private List<ProductResponse> getTopProducts() {
        // For now, return products sorted by price (you can change this to sales/popularity later)
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> topProducts = productRepository.findAll(pageable);

        return topProducts.getContent().stream()
                .map(productMapper::toResponse)
                .toList();
    }

    // Additional analytics methods

    public Map<String, Long> getProductsCreatedByMonth() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfYear = now.withDayOfYear(1).withHour(0).withMinute(0).withSecond(0);

        // You can expand this to get monthly breakdown
        Long thisYearCount = productRepository.countProductsCreatedBetween(startOfYear, now);

        return Map.of(
                "thisYear", thisYearCount,
                "thisMonth", productRepository.countProductsCreatedBetween(
                        now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0),
                        now
                ),
                "thisWeek", productRepository.countProductsCreatedBetween(
                        now.minusWeeks(1),
                        now
                )
        );
    }

    public Map<String, Long> getProductsByPriceRange() {
        return Map.of(
                "under50", productRepository.countProductsByPriceRange(BigDecimal.ZERO, BigDecimal.valueOf(50)),
                "50to100", productRepository.countProductsByPriceRange(BigDecimal.valueOf(50), BigDecimal.valueOf(100)),
                "100to500", productRepository.countProductsByPriceRange(BigDecimal.valueOf(100), BigDecimal.valueOf(500)),
                "over500", productRepository.count() -
                        productRepository.countProductsByPriceRange(BigDecimal.ZERO, BigDecimal.valueOf(500))
        );
    }
}
