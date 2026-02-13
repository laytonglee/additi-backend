package groupproject.additibackend.service.impl;

import groupproject.additibackend.exception.BusinessValidationException;
import groupproject.additibackend.exception.ResourceNotFoundException;
import groupproject.additibackend.mapper.ProductMapper;
import groupproject.additibackend.mapper.ProductVariantMapper;
import groupproject.additibackend.model.*;
import groupproject.additibackend.repository.CategoryRepository;
import groupproject.additibackend.repository.ProductRepository;
import groupproject.additibackend.repository.ProductVariantRepository;
import groupproject.additibackend.repository.UserRepository;
import groupproject.additibackend.request.ProductCreateRequest;
import groupproject.additibackend.request.ProductUpdateRequest;
import groupproject.additibackend.request.ProductVariantUpdateRequest;
import groupproject.additibackend.response.PageResponse;
import groupproject.additibackend.response.ProductDetailResponse;
import groupproject.additibackend.response.ProductResponse;
import groupproject.additibackend.service.ProductService;
import groupproject.additibackend.service.R2StorageService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {
    private static final List<Order.OrderStatus> SALES_COUNTED_STATUSES = List.of(
            Order.OrderStatus.CONFIRMED,
            Order.OrderStatus.SHIPPED,
            Order.OrderStatus.DELIVERED
    );

    private  final ProductRepository productRepository;
    private  final CategoryRepository categoryRepository;
    private  final ProductVariantRepository productVariantRepository;
    private  final ProductMapper productMapper;
    private final R2StorageService r2StorageService;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ProductResponse createProduct(
            ProductCreateRequest request,
            Map<Integer, List<MultipartFile>> variantImages) throws IOException {

        log.info("Creating product: {}", request.getName());
        validateProductCreate(request);
        // ✅ 0) Get current user (createdBy)
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        // 1. Validate category
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id: " + request.getCategoryId()));

        // 2. Create product entity
        Product product = productMapper.toProductEntity(request, category);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        product.setCreatedBy(currentUser);

        // 3. Set up variants
        product.getProductVariants().forEach(variant -> {
            variant.setProduct(product);
            variant.setCreatedAt(LocalDateTime.now());
            variant.setUpdatedAt(LocalDateTime.now());
        });

        // 4. Save product first to get ID
        Product savedProduct = productRepository.save(product);
        log.info("Product created successfully with id: {}", savedProduct.getId());

        // 5. Upload images for each variant (optimized)
        if (variantImages != null && !variantImages.isEmpty()) {
            List<ProductVariant> variants = savedProduct.getProductVariants();

            for (int i = 0; i < variants.size(); i++) {
                ProductVariant variant = variants.get(i);
                List<MultipartFile> files = variantImages.get(i);

                if (files != null && !files.isEmpty()) {
                    // Direct upload without extra DB calls
                    uploadImagesForVariant(savedProduct.getId(), variant, files);
                }
            }
            // Refresh product to get updated images
            Product finalSavedProduct = savedProduct;
            savedProduct = productRepository.findById(savedProduct.getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found with id: " + finalSavedProduct.getId()));
        }

        log.info("Product created with {} variants and images",
                savedProduct.getProductVariants().size());

        return productMapper.toResponse(savedProduct);
    }

    @Transactional
    public ProductResponse updateProduct(Long productId, ProductUpdateRequest request) {
        log.info("Updating product with id: {}", productId);

        // 1. Find existing product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + productId));
        // Vilidate sku
        validateProductUpdate(product, request);

        // 2. Update basic fields
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setBrand(request.getBrand());
        if (request.getIsActive() != null) {
            product.setIsActive(request.getIsActive());
        }
        if (request.getStatus() != null) {
            product.setStatus(request.getStatus());
        }
        if (request.getIsFeatured() != null) {
            product.setIsFeatured(request.getIsFeatured());
        }

        if (Boolean.FALSE.equals(product.getIsFeatured())) {
            product.setFeaturedOrder(null);
        } else if (request.getFeaturedOrder() != null) {
            product.setFeaturedOrder(request.getFeaturedOrder());
        }

        if (product.getStatus() == ProductStatus.COMING_SOON) {
            if (request.getAvailableDate() != null) {
                product.setAvailableDate(request.getAvailableDate());
            }
        } else {
            product.setAvailableDate(null);
        }
        product.setUpdatedAt(LocalDateTime.now());

        // 3. Update category if changed
        if (!product.getCategory().getId().equals(request.getCategoryId())) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Category not found with id: " + request.getCategoryId()));
            product.setCategory(category);
        }

        // 4. Update variants if provided
        if (request.getVariants() != null && !request.getVariants().isEmpty()) {
            updateProductVariants(product, request.getVariants());
        }

        // 5. Save and return
        Product updatedProduct = productRepository.save(product);
        log.info("Product updated successfully with id: {}", updatedProduct.getId());

        return productMapper.toResponse(updatedProduct);
    }

    @Override
    @Transactional
    public ProductResponse updateProductWithImages(Long id, ProductUpdateRequest request, Map<Integer, List<MultipartFile>> varaintImages) throws IOException {
        // 1. Update product basic info
        ProductResponse response = updateProduct(id, request);

        // 2. Upload images for variants if provided
        if (varaintImages != null && !varaintImages.isEmpty()) {
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found with id: " + id));

            List<ProductVariant> variants = product.getProductVariants();

            for (Map.Entry<Integer, List<MultipartFile>> entry : varaintImages.entrySet()) {
                int variantIndex = entry.getKey();
                List<MultipartFile> files = entry.getValue();

                if (variantIndex < variants.size() && files != null && !files.isEmpty()) {
                    ProductVariant variant = variants.get(variantIndex);
                    uploadImagesForVariant(id, variant, files);
                }
            }

            // Refresh to get updated images
            product = productRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found with id: " + id));

            response = productMapper.toResponse(product);
        }

        log.info("Product updated with images successfully: {}", id);
        return response;
    }

    // Helper method (more efficient - no extra DB queries)
    private void uploadImagesForVariant(Long productId, ProductVariant variant,
                                        List<MultipartFile> files) throws IOException {
        log.info("Uploading {} images for variant {}", files.size(), variant.getId());

        String folder = "products/" + productId + "/variants/" + variant.getId();
        List<String> uploadedKeys = r2StorageService.uploadMultipleFiles(files, folder);

        for (String fileKey : uploadedKeys) {
            ProductImage image = new ProductImage();
            image.setImageKey(fileKey);
            image.setImageUrl(r2StorageService.getPublicUrl(fileKey));
            image.setVariant(variant);
            image.setUploadedAt(LocalDateTime.now());

            variant.addImage(image);
        }

        productVariantRepository.save(variant);
        log.info("Uploaded {} images for variant {}", files.size(), variant.getId());
    }

    // Keep this for standalone image uploads
    @Override
    @Transactional
    public ProductResponse uploadImage(Long productId, Long variantId, List<MultipartFile> files)
            throws IOException {
        log.info("Uploading images for variant {} of product {}", variantId, productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + productId));

        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Variant not found with id: " + variantId));

        if (!variant.getProduct().getId().equals(productId)) {
            throw new ResourceNotFoundException("Variant does not belong to this product");
        }

        uploadImagesForVariant(productId, variant, files);

        return productMapper.toResponse(product);
    }
    /**
     * Enhanced variant update with better tracking and cleanup
     */
    private void updateProductVariants(Product product, List<ProductVariantUpdateRequest> variantRequests) {

        // Collect requested variant IDs
        Set<Long> requestedVariantIds = variantRequests.stream()
                .map(ProductVariantUpdateRequest::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // Current variants
        List<ProductVariant> currentVariants = new ArrayList<>(product.getProductVariants());
        List<ProductVariant> variantsToDelete = new ArrayList<>();

        // Identify variants to delete
        for (ProductVariant variant : currentVariants) {
            if (!requestedVariantIds.contains(variant.getId())) {
                variantsToDelete.add(variant);
            }
        }

        // Delete removed variants and their images
        deleteVariantsWithImages(product, variantsToDelete);

        // Update existing or create new variants
        for (ProductVariantUpdateRequest variantRequest : variantRequests) {
            if (variantRequest.getId() != null) {
                updateExistingVariant(product, variantRequest);
            } else {
                addNewVariant(product, variantRequest);
            }
        }
    }

    /**
     * Deletes variants and cleans up their images from storage
     */
    private void deleteVariantsWithImages(Product product, List<ProductVariant> variantsToDelete) {
        for (ProductVariant variant : variantsToDelete) {
            log.info("Deleting variant {} with {} images", variant.getId(), variant.getImages().size());

            // Delete images from R2 storage
            List<String> imageKeys = variant.getImages().stream()
                    .map(ProductImage::getImageKey)
                    .collect(Collectors.toList());

            for (String imageKey : imageKeys) {
                try {
                    r2StorageService.deleteFile(imageKey);
                    log.debug("Deleted image: {}", imageKey);
                } catch (Exception e) {
                    log.error("Failed to delete image: {}", imageKey, e);
                }
            }

            // Remove from product and delete
            product.getProductVariants().remove(variant);
            productVariantRepository.delete(variant);
            log.info("Variant {} deleted successfully", variant.getId());
        }
    }

    private void validateProductUpdate(Product product, ProductUpdateRequest request) {
        // price
        if (request.getPrice() != null && request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessValidationException("Product price must be greater than zero");
        }

        ProductStatus effectiveStatus = request.getStatus() != null
                ? request.getStatus()
                : product.getStatus();
        LocalDateTime effectiveAvailableDate = request.getAvailableDate() != null
                ? request.getAvailableDate()
                : product.getAvailableDate();

        if (effectiveStatus == ProductStatus.COMING_SOON && effectiveAvailableDate == null) {
            throw new BusinessValidationException("Available date is required when status is COMING_SOON");
        }

        // variants: only validate if client actually sent them
        if (request.getVariants() == null) {
            return; // not updating variants
        }

        if (request.getVariants().isEmpty()) {
            return;
            // OR if you want to reject, replace with:
            // throw new BusinessValidationException("Product must have at least one variant");
        }

        validateVariantSkusForUpdate(product, request.getVariants());
    }

    private void validateProductCreate(ProductCreateRequest request) {
        ProductStatus status = request.getStatus() != null
                ? request.getStatus()
                : ProductStatus.ACTIVE;

        if (status == ProductStatus.COMING_SOON && request.getAvailableDate() == null) {
            throw new BusinessValidationException("Available date is required when status is COMING_SOON");
        }
    }

    /**
     * Updates an existing variant
     */
    private void updateExistingVariant(Product product, ProductVariantUpdateRequest request) {
        ProductVariant existingVariant = product.getProductVariants().stream()
                .filter(v -> v.getId().equals(request.getId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Variant not found with id: " + request.getId()));

        log.info("Updating existing variant: {} (SKU: {} -> {})",
                existingVariant.getId(), existingVariant.getSku(), request.getSku());

        updateVariantFields(existingVariant, request);
    }

    /**
     * Adds a new variant to the product
     */
    private void addNewVariant(Product product, ProductVariantUpdateRequest request) {
        log.info("Adding new variant with SKU: {}", request.getSku());

        ProductVariant newVariant = new ProductVariant();
        updateVariantFields(newVariant, request);
        newVariant.setProduct(product);
        newVariant.setCreatedAt(LocalDateTime.now());
        product.addVariant(newVariant);
    }

    private void updateVariantFields(ProductVariant variant, ProductVariantUpdateRequest request) {
        variant.setSize(request.getSize());
        variant.setColor(request.getColor());
        variant.setSku(request.getSku());
        variant.setStockQuantity(request.getStockQuantity());
        variant.setPriceAdjustment(request.getPriceAdjustment());
        variant.setUpdatedAt(LocalDateTime.now());
    }

    /**
     * Validates variant SKUs for update operation
     * This prevents the duplicate key constraint violation error
     */
    private void validateVariantSkusForUpdate(Product product, List<ProductVariantUpdateRequest> variants) {
        Set<String> newSkus = new HashSet<>();

        // Build map of existing variant SKUs from the already-loaded product
        Map<Long, String> existingVariantSkus = product.getProductVariants().stream()
                .collect(Collectors.toMap(ProductVariant::getId, ProductVariant::getSku));

        log.debug("Validating SKUs. Existing variants for product {}: {}",
                product.getId(), existingVariantSkus);

        for (ProductVariantUpdateRequest variant : variants) {
            if (variant.getSku() == null || variant.getSku().isBlank()) {
                throw new BusinessValidationException("Variant SKU cannot be empty");
            }

            String normalizedSku = variant.getSku().trim().toUpperCase();

            // Check for duplicates in request
            if (!newSkus.add(normalizedSku)) {
                throw new BusinessValidationException(
                        String.format("Duplicate SKU in request: %s", normalizedSku)
                );
            }

            // FIXED: If variant is keeping its existing SKU, skip validation
            if (variant.getId() != null) {
                String existingSku = existingVariantSkus.get(variant.getId());
                if (existingSku != null && existingSku.equalsIgnoreCase(normalizedSku)) {
                    log.debug("Variant {} keeping its existing SKU: {}", variant.getId(), normalizedSku);
                    continue; // This variant is keeping its own SKU - no need to validate
                }
            }

            // Validate SKU doesn't exist elsewhere in database
            validateSkuUnique(normalizedSku, variant.getId());
        }
    }

    private void validateSkuUnique(String sku, Long currentVariantId) {
        productVariantRepository.findBySku(sku).ifPresent(existing -> {
            // If we're updating an existing variant, only throw if SKU belongs to a DIFFERENT variant
            if (currentVariantId == null || !existing.getId().equals(currentVariantId)) {
                throw new BusinessValidationException(
                        String.format("SKU '%s' already exists (Variant ID: %d)", sku, existing.getId())
                );
            }
        });
    }

    @Override
    public PageResponse<ProductResponse> getAllProducts(
            String search,
            String categorySlug,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            LocalDate startDate,
            LocalDate endDate,
            String size,
            String color,
            Long createdById,
            Pageable pageable
    ) {
        Page<Product> productPage = productRepository.findByFilters(
                emptyToNull(search),
                emptyToNull(categorySlug),
                minPrice,
                maxPrice,
                startDate,
                endDate,
                emptyToNull(size),
                emptyToNull(color),
                createdById,
                pageable
        );

        Map<Long, Integer> salesByProductId = getSalesByProductId(productPage.getContent());

        return buildPageResponse(productPage, emptyToNull(size), emptyToNull(color), salesByProductId);
    }


    @Override
    public ProductDetailResponse getProductById(Long id) {
        Product product = productRepository.findProductById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        ProductResponse productRes = productMapper.toResponse(product);

        Long categoryId = product.getCategory().getId();
        List<ProductResponse> related = productRepository
                .findRelatedByCategory(categoryId, id, PageRequest.of(0, 8))
                .stream()
                .map(productMapper::toResponse)
                .toList();

        return ProductDetailResponse.builder()
                .product(productRes)
                .relatedProducts(related)
                .build();
    }

    @Override
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        // Delete all images from R2
        product.getProductVariants().forEach(variant ->
                variant.getImages().forEach(image ->
                        r2StorageService.deleteFile(image.getImageKey())
                )
        );
        productRepository.delete(product);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<ProductResponse> getBestSellers(int limit) {
        int safeLimit = limit > 0 ? Math.min(limit, 100) : 12;
        Pageable pageable = PageRequest.of(0, safeLimit);
        return productRepository.findBestSellersByOrderStatuses(SALES_COUNTED_STATUSES, pageable)
                .stream()
                .map(row -> {
                    ProductResponse response = productMapper.toResponse(row.getProduct());
                    response.setSalesCount(row.getTotalSold() == null ? 0 : row.getTotalSold().intValue());
                    return response;
                })
                .toList();
    }

    @Override
    public List<ProductResponse> getFeaturedProducts() {
        return productRepository.findFeaturedProducts()
                .stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Override
    public List<ProductResponse> getComingSoonProducts() {
        return productRepository.findComingSoonProducts()
                .stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ProductResponse setFeatured(Long productId, boolean featured, Integer featuredOrder) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        product.setIsFeatured(featured);
        product.setFeaturedOrder(featured ? featuredOrder : null);
        product.setUpdatedAt(LocalDateTime.now());

        return productMapper.toResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductResponse setProductStatus(Long productId, ProductStatus status, LocalDateTime availableDate) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        product.setStatus(status);
        product.setAvailableDate(availableDate);
        product.setUpdatedAt(LocalDateTime.now());

        return productMapper.toResponse(productRepository.save(product));
    }


    private String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }


    private PageResponse<ProductResponse> buildPageResponse(
            Page<Product> productPage,
            String filterSize,
            String filterColor,
            Map<Long, Integer> salesByProductId) {

        List<ProductResponse> productResponses = productPage.getContent()
                .stream()
                .map(productMapper::toResponse)
                .peek(p -> {
                    p.setSalesCount(salesByProductId.getOrDefault(p.getId(), 0));

                    if (p.getVariants() == null) return;

                    p.setVariants(
                            p.getVariants().stream()
                                    .filter(v -> filterSize == null ||
                                            filterSize.equalsIgnoreCase(v.getSize()))
                                    .filter(v -> filterColor == null ||
                                            filterColor.equalsIgnoreCase(v.getColor()))
                                    .toList()
                    );
                })
                .toList();

        return PageResponse.<ProductResponse>builder()
                .content(productResponses)
                .pageNumber(productPage.getNumber())
                .pageSize(productPage.getSize())
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .build();
    }

    private Map<Long, Integer> getSalesByProductId(List<Product> products) {
        if (products == null || products.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Long> productIds = products.stream()
                .map(Product::getId)
                .filter(Objects::nonNull)
                .toList();

        if (productIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return productRepository.findProductSalesByIdsAndOrderStatuses(productIds, SALES_COUNTED_STATUSES)
                .stream()
                .collect(Collectors.toMap(
                        ProductRepository.ProductSalesProjection::getProductId,
                        row -> row.getTotalSold() == null ? 0 : row.getTotalSold().intValue()
                ));
    }



}
