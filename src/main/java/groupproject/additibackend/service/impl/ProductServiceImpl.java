package groupproject.additibackend.service.impl;

import groupproject.additibackend.exception.ResourceNotFoundException;
import groupproject.additibackend.mapper.ProductMapper;
import groupproject.additibackend.mapper.ProductVariantMapper;
import groupproject.additibackend.model.Category;
import groupproject.additibackend.model.Product;
import groupproject.additibackend.model.ProductImage;
import groupproject.additibackend.model.ProductVariant;
import groupproject.additibackend.repository.CategoryRepository;
import groupproject.additibackend.repository.ProductRepository;
import groupproject.additibackend.repository.ProductVariantRepository;
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
    private  final ProductRepository productRepository;
    private  final CategoryRepository categoryRepository;
    private  final ProductVariantRepository productVariantRepository;
    private  final ProductMapper productMapper;
    private final R2StorageService r2StorageService;


    @Override
    @Transactional
    public ProductResponse createProduct(
            ProductCreateRequest request,
            Map<Integer, List<MultipartFile>> variantImages) throws IOException {

        log.info("Creating product: {}", request.getName());

        // 1. Validate category
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id: " + request.getCategoryId()));

        // 2. Create product entity
        Product product = productMapper.toProductEntity(request, category);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

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

        // 2. Update basic fields
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setBrand(request.getBrand());
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

    // Helper method to update variants
    private void updateProductVariants(Product product, List<ProductVariantUpdateRequest> variantRequests) {
        log.info("Updating variants for product: {}", product.getId());

        // Collect requested variant IDs
        Set<Long> requestedVariantIds = variantRequests.stream()
                .map(ProductVariantUpdateRequest::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // Get current variants
        List<ProductVariant> currentVariants = new ArrayList<>(product.getProductVariants());

        // Delete variants not in the request
        Iterator<ProductVariant> iterator = currentVariants.iterator();
        while (iterator.hasNext()) {
            ProductVariant variant = iterator.next();
            if (!requestedVariantIds.contains(variant.getId())) {
                log.info("Deleting variant: {}", variant.getId());

                // Delete associated images from storage
                variant.getImages().forEach(image -> {
                    try {
                        r2StorageService.deleteFile(image.getImageKey());
                    } catch (Exception e) {
                        log.error("Failed to delete image: {}", image.getImageKey(), e);
                    }
                });

                iterator.remove();
                product.getProductVariants().remove(variant);
                productVariantRepository.delete(variant);
            }
        }

        // Update existing variants or add new ones
        for (ProductVariantUpdateRequest variantRequest : variantRequests) {
            if (variantRequest.getId() != null) {
                // Update existing variant
                ProductVariant existingVariant = product.getProductVariants().stream()
                        .filter(v -> v.getId().equals(variantRequest.getId()))
                        .findFirst()
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Variant not found with id: " + variantRequest.getId()));

                log.info("Updating existing variant: {}", existingVariant.getId());
                updateVariantFields(existingVariant, variantRequest);

            } else {
                // Add new variant
                log.info("Adding new variant with SKU: {}", variantRequest.getSku());
                ProductVariant newVariant = new ProductVariant();
                updateVariantFields(newVariant, variantRequest);
                newVariant.setProduct(product);
                newVariant.setCreatedAt(LocalDateTime.now());
                product.addVariant(newVariant);
            }
        }
    }

    private void updateVariantFields(ProductVariant variant, ProductVariantUpdateRequest request) {
        variant.setSize(request.getSize());
        variant.setColor(request.getColor());
        variant.setSku(request.getSku());
        variant.setStockQuantity(request.getStockQuantity());
        variant.setPriceAdjustment(request.getPriceAdjustment());
        variant.setUpdatedAt(LocalDateTime.now());
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
                pageable
        );

        return buildPageResponse(productPage, emptyToNull(size), emptyToNull(color));
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

    private String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }


    private PageResponse<ProductResponse> buildPageResponse(Page<Product> productPage,
                                                            String filterSize,
                                                            String filterColor) {

        List<ProductResponse> productResponses = productPage.getContent()
                .stream()
                .map(productMapper::toResponse)
                .peek(p -> {
                    if (p.getVariants() == null) return;

                    p.setVariants(
                            p.getVariants().stream()
                                    .filter(v -> filterSize == null || filterSize.equalsIgnoreCase(v.getSize()))
                                    .filter(v -> filterColor == null || filterColor.equalsIgnoreCase(v.getColor()))
                                    .toList()
                    );
                })
                .toList();

        return PageResponse.<ProductResponse>builder()
                .products(productResponses)
                .pageNumber(productPage.getNumber())
                .pageSize(productPage.getSize())
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .last(productPage.isLast())
                .build();
    }




}
