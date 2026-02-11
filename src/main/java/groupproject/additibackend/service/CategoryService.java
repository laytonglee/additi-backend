package groupproject.additibackend.service;

import groupproject.additibackend.request.CategoryRequest;
import groupproject.additibackend.response.CategoryResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface CategoryService {
    CategoryResponse createCategory(CategoryRequest request);
    CategoryResponse getCategoryById(Long id);

    List<CategoryResponse> getAllCategories(
            Boolean isActive,
            String search,
            LocalDateTime createdAtStart,
            LocalDateTime createdAtEnd,
            Long createdById
    );
    CategoryResponse updateCategory(Long id ,CategoryRequest request);
    void deleteCategory(Long id);

}
