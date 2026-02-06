package groupproject.additibackend.service.impl;

import groupproject.additibackend.exception.ResourceNotFoundException;
import groupproject.additibackend.mapper.ProductMapper;
import groupproject.additibackend.mapper.ProductVariantMapper;
import groupproject.additibackend.model.Category;
import groupproject.additibackend.model.Product;
import groupproject.additibackend.repository.CategoryRepository;
import groupproject.additibackend.repository.ProductRepository;
import groupproject.additibackend.request.ProductCreateRequest;
import groupproject.additibackend.response.PageResponse;
import groupproject.additibackend.response.ProductResponse;
import groupproject.additibackend.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {
    private  final ProductRepository productRepository;
    private  final CategoryRepository categoryRepository;
    private  final ProductMapper productMapper;
    private  final ProductVariantMapper productVariantMapper;

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
    public PageResponse<ProductResponse> getAllProducts(Pageable pageable) {
        log.info("Fetching all products with pagination");

        Page<Product> productPage = productRepository.findAll(pageable);
        return buildPageResponse(productPage);
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
