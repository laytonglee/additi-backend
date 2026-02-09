package groupproject.additibackend.controller;

import groupproject.additibackend.model.Product;
import groupproject.additibackend.repository.ProductRepository;
import groupproject.additibackend.request.ProductCreateRequest;
import groupproject.additibackend.response.ApiResponse;
import groupproject.additibackend.response.PageResponse;
import groupproject.additibackend.response.ProductResponse;
import groupproject.additibackend.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
      private final ProductService productService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody ProductCreateRequest request) {

        ProductResponse response = productService.createProduct(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Product created successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Sort sort = direction.equalsIgnoreCase("ASC") ?
                Sort.by(sortBy).ascending() :
                Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        PageResponse<ProductResponse> response = productService.getAllProducts(
                category, minPrice, maxPrice, startDate, endDate, pageable);

        return ResponseEntity.ok(ApiResponse.success(response, "Products retrieved successfully"));
    }

    @PostMapping("/{productId}/variants/{variantId}/images")
    public ResponseEntity<ApiResponse<ProductResponse>> uploadVariantImages(
            @PathVariable Long productId,
            @PathVariable Long variantId,
            @RequestParam("files") List<MultipartFile> files) throws IOException {

        ProductResponse response = productService.uploadImage(productId, variantId, files);
        return ResponseEntity.ok(ApiResponse.success(response, "Images uploaded successfully"));
    }

//    @DeleteMapping("/{productId}/variants/{variantId}/images/{imageId}")
//    public ResponseEntity<ApiResponse<Void>> deleteVariantImage(
//            @PathVariable Long productId,
//            @PathVariable Long variantId,
//            @PathVariable Long imageId) {
//
//        productService.deleteVariantImage(productId, variantId, imageId);
//        return ResponseEntity.ok(ApiResponse.success(null, "Image deleted successfully"));
//    }



}
