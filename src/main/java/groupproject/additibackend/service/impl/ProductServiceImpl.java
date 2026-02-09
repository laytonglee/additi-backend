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
import groupproject.additibackend.response.PageResponse;
import groupproject.additibackend.response.ProductResponse;
import groupproject.additibackend.service.ProductService;
import groupproject.additibackend.service.R2StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {
    private  final ProductRepository productRepository;
    private  final CategoryRepository categoryRepository;
    private  final ProductVariantRepository productVariantRepository;
    private  final ProductMapper productMapper;
    private  final ProductVariantMapper productVariantMapper;
    private final R2StorageService r2StorageService;

    @Override
    public ProductResponse createProduct(ProductCreateRequest request) {
        log.info("Creating product: {}", request.getName());

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        Product product = productMapper.toProductEntity(request, category);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        product.getProductVariants().forEach(variant -> {
            variant.setProduct(product);
            variant.setCreatedAt(LocalDateTime.now());
            variant.setUpdatedAt(LocalDateTime.now());
        });

        Product savedProduct = productRepository.save(product);
        log.info("Product created successfully with id: {}", savedProduct.getId());

        return productMapper.toResponse(savedProduct);
    }

    @Override
    public PageResponse<ProductResponse> getAllProducts(
            String category,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable) {

        Page<Product> productPage = productRepository.findByFilters(
                category, minPrice, maxPrice, startDate, endDate, pageable);
        return buildPageResponse(productPage);
    }

    @Override
    public ProductResponse uploadImage(Long productId, Long variantId, List<MultipartFile> files) throws IOException {
        log.info("Uploading images for variant {} of product {}", variantId, productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Variant not found with id: " + variantId));

        if (!variant.getProduct().getId().equals(productId)) {
            throw new ResourceNotFoundException("Variant does not belong to this product");
        }

        String folder = "products/" + productId + "/variants/" + variantId;
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
        log.info("Uploaded {} images for variant {}", files.size(), variantId);

        return productMapper.toResponse(product);
    }


    private PageResponse<ProductResponse> buildPageResponse(Page<Product> productPage) {
        return PageResponse.<ProductResponse>builder()
                .products(productPage.getContent().stream()
                        .map(productMapper::toResponse)
                        .toList())
                .pageNumber(productPage.getNumber())
                .pageSize(productPage.getSize())
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .last(productPage.isLast())
                .build();
    }






}
