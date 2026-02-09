package groupproject.additibackend.service.impl;

import groupproject.additibackend.mapper.CategoryMapper;
import groupproject.additibackend.model.Category;
import groupproject.additibackend.repository.CategoryRepository;
import groupproject.additibackend.request.CategoryRequest;
import groupproject.additibackend.response.CategoryResponse;
import groupproject.additibackend.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public CategoryResponse createCategory(CategoryRequest request) {
        log.info("createCategory");

        Category category = categoryMapper.toCategoryEntity(request);
        Category savedCategory = categoryRepository.save(category);

        return categoryMapper.toCategoryResponse(savedCategory);
    }

    @Override
    public CategoryResponse getCategoryById(Long id) {
        return null;
    }

    @Override
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toCategoryResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryResponse updateCategory(CategoryRequest request) {
        return null;
    }

    @Override
    public void deleteCategory(Long id) {

    }
}
