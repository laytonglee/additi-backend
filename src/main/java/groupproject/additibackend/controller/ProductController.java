package groupproject.additibackend.controller;

import groupproject.additibackend.mapper.ProductMappers;
import groupproject.additibackend.repository.ProductReponsitory;
import groupproject.additibackend.response.ProductResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductReponsitory productRepo;

    public ProductController(ProductReponsitory productRepo) {
        this.productRepo = productRepo;
    }

    // ✅ GET /api/products (active only, with variants+images)
    @GetMapping
    public List<ProductResponse> getAllActive() {
        return productRepo.findAllActiveWithDetails()
                .stream()
                .map(ProductMappers::toDetail)
                .toList();
    }

}
