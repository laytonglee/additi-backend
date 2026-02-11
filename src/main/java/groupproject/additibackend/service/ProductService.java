package groupproject.additibackend.service;

import groupproject.additibackend.request.ProductCreateRequest;
import groupproject.additibackend.request.ProductUpdateRequest;
import groupproject.additibackend.response.PageResponse;
import groupproject.additibackend.response.ProductDetailResponse;
import groupproject.additibackend.response.ProductResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ProductService {

    ProductResponse createProduct(
            ProductCreateRequest request,
            Map<Integer ,List<MultipartFile>> varaintImages) throws IOException;


    ProductResponse updateProduct(Long productId, ProductUpdateRequest request);

    ProductResponse updateProductWithImages(
            Long id,
            ProductUpdateRequest request,
            Map<Integer ,List<MultipartFile>> varaintImages
    ) throws IOException;

//    PageResponse<ProductResponse> getAllProducts(
//            String search,
//            String categorySlug,
//            BigDecimal minPrice,
//            BigDecimal maxPrice,
//            LocalDate startDate,
//            LocalDate endDate,
//            String size,
//            String color,
//            int page,
//            int limit,
//            String sortBy,
//            String sortDir
//    );


    ProductResponse uploadImage(Long productId, Long variantId, List<MultipartFile> files) throws IOException;

    ProductDetailResponse getProductById(Long id);

    void deleteProduct(Long id);


    PageResponse<ProductResponse> getAllProducts(
            String search,
            String categorySlug,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            LocalDate startDate,
            LocalDate endDate,
            String size,
            String color,
            Long createdById,
            Pageable pageable);
}