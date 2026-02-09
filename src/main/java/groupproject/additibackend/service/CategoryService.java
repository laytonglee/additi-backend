package groupproject.additibackend.service;

import groupproject.additibackend.request.CategoryRequest;
import groupproject.additibackend.response.CategoryResponse;

import java.util.List;

public interface CategoryService {
    CategoryResponse createCategory(CategoryRequest request);
    CategoryResponse getCategoryById(Long id);

    List<CategoryResponse> getAllCategories();
    CategoryResponse updateCategory(Long id ,CategoryRequest request);
    void deleteCategory(Long id);

}
