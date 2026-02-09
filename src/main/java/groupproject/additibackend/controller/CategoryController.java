package groupproject.additibackend.controller;

import groupproject.additibackend.request.CategoryRequest;
import groupproject.additibackend.response.ApiResponse;
import groupproject.additibackend.response.CategoryResponse;
import groupproject.additibackend.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

     private final  CategoryService categoryService;

    @PostMapping
     public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
             @Valid @RequestBody CategoryRequest request)
     {
         CategoryResponse response = categoryService.createCategory(request);
         return ResponseEntity
                 .status(HttpStatus.CREATED)
                 .body(ApiResponse.success(response, "Category created successfully"));

     }

     @GetMapping
     public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategory(){
         List<CategoryResponse> response = categoryService.getAllCategories();
         return ResponseEntity.ok(ApiResponse.success(response, "Categories retrieved successfully"));
     }






}
