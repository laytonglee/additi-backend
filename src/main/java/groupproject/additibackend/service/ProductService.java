package groupproject.additibackend.service;

import groupproject.additibackend.request.ProductCreateRequest;
import groupproject.additibackend.response.PageResponse;
import groupproject.additibackend.response.ProductResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ProductService {

    ProductResponse createProduct(ProductCreateRequest request);

    PageResponse<ProductResponse> getAllProducts(
            String  categorySlug,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable);


    ProductResponse uploadImage(Long productId, Long variantId, List<MultipartFile> files) throws IOException;

}