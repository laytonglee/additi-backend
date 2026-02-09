package groupproject.additibackend.service;

import groupproject.additibackend.request.ProductCreateRequest;
import groupproject.additibackend.response.PageResponse;
import groupproject.additibackend.response.ProductResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ProductService {

    ProductResponse createProduct(ProductCreateRequest request);

    PageResponse<ProductResponse> getAllProducts(Pageable pageable);


    ProductResponse uploadImage(Long productId, Long variantId, List<MultipartFile> files) throws IOException;

}