package groupproject.additibackend.controller;

import groupproject.additibackend.model.ProductStatus;
import groupproject.additibackend.response.ApiResponse;
import groupproject.additibackend.response.ProductResponse;
import groupproject.additibackend.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;

    @PutMapping("/{id}/featured")
    public ResponseEntity<ApiResponse<ProductResponse>> setFeatured(
            @PathVariable Long id,
            @RequestParam boolean featured,
            @RequestParam(required = false) Integer order) {

        ProductResponse response = productService.setFeatured(id, featured, order);
        return ResponseEntity.ok(ApiResponse.success(response, "Product featured status updated"));
    }
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ProductResponse>> setStatus(
            @PathVariable Long id,
            @RequestParam ProductStatus status,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime availableDate) {

        ProductResponse response = productService.setProductStatus(id, status, availableDate);
        return ResponseEntity.ok(ApiResponse.success(response, "Product status updated"));
    }
}
