package groupproject.additibackend.service;

import groupproject.additibackend.request.ProductCreateRequest;
import groupproject.additibackend.response.PageResponse;
import groupproject.additibackend.response.ProductResponse;
import org.springframework.data.domain.Pageable;

public interface ProductService {

    ProductResponse createProduct(ProductCreateRequest request);

    PageResponse<ProductResponse> getAllProducts(Pageable pageable);



}
