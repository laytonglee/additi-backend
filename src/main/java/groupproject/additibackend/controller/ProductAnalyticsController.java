package groupproject.additibackend.controller;

import groupproject.additibackend.response.ApiResponse;
import groupproject.additibackend.response.ProductAnalyticsResponse;
import groupproject.additibackend.service.ProductAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductAnalyticsController {

    private final ProductAnalyticsService analyticsService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<ProductAnalyticsResponse>> getDashboardAnalytics() {
        ProductAnalyticsResponse analytics = analyticsService.getDashboardAnalytics();
        return ResponseEntity.ok(ApiResponse.success(analytics, "Analytics retrieved successfully"));
    }
}
