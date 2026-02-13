package groupproject.additibackend.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import groupproject.additibackend.request.ProductUpdateRequest;
import groupproject.additibackend.request.ProductVariantRequest;
import groupproject.additibackend.response.ProductDetailResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import groupproject.additibackend.request.ProductCreateRequest;
import groupproject.additibackend.response.ApiResponse;
import groupproject.additibackend.response.PageResponse;
import groupproject.additibackend.response.ProductResponse;
import groupproject.additibackend.service.ProductService;
import lombok.RequiredArgsConstructor;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {
      private final ProductService productService;
      private final ObjectMapper objectMapper;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @RequestPart("product") String productJson,
            @RequestPart(value = "variant_0_images", required = false) List<MultipartFile> variant0Images,
            @RequestPart(value = "variant_1_images", required = false) List<MultipartFile> variant1Images,
            @RequestPart(value = "variant_2_images", required = false) List<MultipartFile> variant2Images,
            @RequestPart(value = "variant_3_images", required = false) List<MultipartFile> variant3Images,
            @RequestPart(value = "variant_4_images", required = false) List<MultipartFile> variant4Images
    ) throws IOException {

        log.info("Received product JSON: {}", productJson);

        // Parse JSON string to ProductCreateRequest
        ProductCreateRequest request = objectMapper.readValue(productJson, ProductCreateRequest.class);


        // Collect variant images
        Map<Integer, List<MultipartFile>> variantImages = new HashMap<>();
        if (variant0Images != null && !variant0Images.isEmpty()) {
            variantImages.put(0, variant0Images);
            log.info("Variant 0: {} images", variant0Images.size());
        }
        if (variant1Images != null && !variant1Images.isEmpty()) {
            variantImages.put(1, variant1Images);
            log.info("Variant 1: {} images", variant1Images.size());
        }
        if (variant2Images != null && !variant2Images.isEmpty()) {
            variantImages.put(2, variant2Images);
            log.info("Variant 2: {} images", variant2Images.size());
        }
        if (variant3Images != null && !variant3Images.isEmpty()) {
            variantImages.put(3, variant3Images);
        }
        if (variant4Images != null && !variant4Images.isEmpty()) {
            variantImages.put(4, variant4Images);
        }

        ProductResponse response = productService.createProduct(request, variantImages);

        log.info("Product created successfully with id: {}", response.getId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Product created successfully"));
    }

    @PutMapping(value = "/{productId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProductResponse>> updateProductWithImages(
            @PathVariable Long productId,
            @RequestPart("product") String productJson,
            @RequestPart(value = "variant_0_images", required = false) List<MultipartFile> variant0Images,
            @RequestPart(value = "variant_1_images", required = false) List<MultipartFile> variant1Images,
            @RequestPart(value = "variant_2_images", required = false) List<MultipartFile> variant2Images,
            @RequestPart(value = "variant_3_images", required = false) List<MultipartFile> variant3Images
    ) throws IOException {

        log.info("Updating product {} with images", productId);

        // Parse JSON
        ProductUpdateRequest request = objectMapper.readValue(productJson, ProductUpdateRequest.class);

        // Collect images
        Map<Integer, List<MultipartFile>> variantImages = new HashMap<>();
        if (variant0Images != null && !variant0Images.isEmpty()) {
            variantImages.put(0, variant0Images);
        }
        if (variant1Images != null && !variant1Images.isEmpty()) {
            variantImages.put(1, variant1Images);
        }
        if (variant2Images != null && !variant2Images.isEmpty()) {
            variantImages.put(2, variant2Images);
        }
        if (variant3Images != null && !variant3Images.isEmpty()) {
            variantImages.put(3, variant3Images);
        }

        ProductResponse response = productService.updateProductWithImages(
                productId, request, variantImages);

        return ResponseEntity.ok(
                ApiResponse.success(response, "Product updated successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String categorySlug,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String sizeValue,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        Pageable pageable = buildPageable(page, size, sortBy, direction);

        PageResponse<ProductResponse> pageResp = productService.getAllProducts(
                search,
                categorySlug,
                minPrice,
                maxPrice,
                startDate,
                endDate,
                sizeValue,
                color,
                categoryId,
                pageable
        );
        Map<String, Object> data = buildResponseData(pageResp);
        return ResponseEntity.ok(ApiResponse.success(data, "Products retrieved successfully"));
    }

    @GetMapping("/best-sellers")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getBestSellers(
            @RequestParam(defaultValue = "12") int limit) {
        return ResponseEntity.ok(
                ApiResponse.success(productService.getBestSellers(limit), "Best sellers retrieved successfully"));
    }

    @GetMapping("/featured")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getFeaturedProducts() {
        return ResponseEntity.ok(
                ApiResponse.success(productService.getFeaturedProducts(), "Featured products retrieved successfully"));
    }

    @GetMapping("/coming-soon")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getComingSoonProducts() {
        return ResponseEntity.ok(
                ApiResponse.success(productService.getComingSoonProducts(), "Coming soon products retrieved successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getById(@PathVariable Long id) {
        ProductDetailResponse data = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.success(data, "Product retrieved successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Product deleted successfully"));
    }

    private Pageable buildPageable(int page, int size, String sortBy, String direction) {
        int validatedPage = Math.max(page, 0);
        int validatedSize = Math.min(Math.max(size, 1), 100);
        String validatedSortBy = validateSortField(sortBy);
        Sort.Direction sortDirection = parseSortDirection(direction);

        return PageRequest.of(validatedPage, validatedSize, Sort.by(sortDirection, validatedSortBy));
    }

    private String validateSortField(String sortBy) {
        Set<String> allowedFields = Set.of("id", "name", "price", "createdAt", "updatedAt", "salesCount", "status");
        String normalized = (sortBy == null) ? "id" : sortBy.trim();
        return allowedFields.contains(normalized) ? normalized : "id";
    }

    private Sort.Direction parseSortDirection(String direction) {
        try {
            return Sort.Direction.fromString(direction);
        } catch (IllegalArgumentException ex) {
            return Sort.Direction.DESC;
        }
    }

    private Map<String, Object> buildResponseData(PageResponse<ProductResponse> pageResponse) {
        List<Map<String, Object>> products = pageResponse.getContent().stream()
                .map(product -> objectMapper.convertValue(product, new TypeReference<Map<String, Object>>() {}))
                .peek(productMap -> productMap.remove("salesCount"))
                .toList();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("products", products);
        data.put("pageNumber", pageResponse.getPageNumber());
        data.put("pageSize", pageResponse.getPageSize());
        data.put("totalElements", pageResponse.getTotalElements());
        data.put("totalPages", pageResponse.getTotalPages());

        return data;
    }

}
