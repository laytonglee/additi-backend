package groupproject.additibackend.mapper;

import groupproject.additibackend.model.Category;
import groupproject.additibackend.request.CategoryRequest;
import groupproject.additibackend.response.CategoryResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class CategoryMapper {

    public Category toCategoryEntity(CategoryRequest categoryRequest) {
        Category category = new Category();

        category.setName(categoryRequest.getName());
        category.setSlug(generateSlug(categoryRequest.getName()));
        category.setDescription(categoryRequest.getDescription());
        category.setIsActive(categoryRequest.getIsActive() != null ? categoryRequest.getIsActive() : true);
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        return category;

    }

    public CategoryResponse toCategoryResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .isActive(category.getIsActive())
                .createdAt(category.getCreatedAt())
//                .productCount(category.getProducts() != null ? (long) category.getProducts().size() : 0L)
                .build();
    }

    private String generateSlug(String text) {
        return text.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }
}
