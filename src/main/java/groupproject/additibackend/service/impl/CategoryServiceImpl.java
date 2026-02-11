package groupproject.additibackend.service.impl;

import groupproject.additibackend.exception.ResourceNotFoundException;
import groupproject.additibackend.mapper.CategoryMapper;
import groupproject.additibackend.model.Category;
import groupproject.additibackend.model.User;
import groupproject.additibackend.repository.CategoryRepository;
import groupproject.additibackend.repository.ProductRepository;
import groupproject.additibackend.repository.UserRepository;
import groupproject.additibackend.request.CategoryRequest;
import groupproject.additibackend.response.CategoryResponse;
import groupproject.additibackend.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    public CategoryResponse createCategory(CategoryRequest request) {
        Category category = categoryMapper.toCategoryEntity(request);

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        category.setCreatedBy(user);

        Category savedCategory = categoryRepository.save(category);
        return categoryMapper.toCategoryResponse(savedCategory);
    }

    @Override
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        CategoryResponse response = categoryMapper.toCategoryResponse(category);
        response.setProductCount(productRepository.countByCategoryId(id)); // ADD COUNT
        return response;
    }

    @Override
    public List<CategoryResponse> getAllCategories(
            Boolean isActive,
            String search,
            LocalDateTime createdAtStart,
            LocalDateTime createdAtEnd,
            Long createdById
    ) {
        log.info("Filtering categories - isActive: {}, search: {}, createdAtStart: {}, createdAtEnd: {}, createdBy: {}",
                isActive, search, createdAtStart, createdAtEnd, createdById);

        List<Category> categories = categoryRepository.findByFilters(
                isActive,
                search,
                createdAtStart,
                createdAtEnd,
                createdById
        );

        return categories.stream()
                .map(this::mapToCategoryResponseWithCount)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        if (!category.getName().equalsIgnoreCase(request.getName()) &&
                categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new ResourceNotFoundException("Category already exists with name: " + request.getName());
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setIsActive(request.getIsActive());
        category.setUpdatedAt(LocalDateTime.now());

        Category updatedCategory = categoryRepository.save(category);

        CategoryResponse response = categoryMapper.toCategoryResponse(updatedCategory);
        response.setProductCount(productRepository.countByCategoryId(id)); // ADD COUNT
        return response;
    }

    @Override
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        Long productCount = productRepository.countByCategoryId(id); // CHANGED FROM categoryRepository
        if (productCount > 0) {
            throw new ResourceNotFoundException("Cannot delete category with existing products");
        }

        categoryRepository.delete(category);
    }

    // Helper method to avoid code duplication
    private CategoryResponse mapToCategoryResponseWithCount(Category category) {
        CategoryResponse response = categoryMapper.toCategoryResponse(category);
        response.setProductCount(productRepository.countByCategoryId(category.getId()));
        return response;
    }
}
