package groupproject.additibackend.mapper;

import groupproject.additibackend.model.Category;
import groupproject.additibackend.model.User;
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
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setSlug(category.getSlug());
        response.setDescription(category.getDescription());
        response.setIsActive(category.getIsActive());
        response.setCreatedAt(category.getCreatedAt());
        response.setUpdatedAt(category.getUpdatedAt());
        if (category.getCreatedBy() != null) {
            User u = category.getCreatedBy();
            response.setCreatedById(u.getId());
            response.setCreatedByEmail(u.getEmail());
            response.setCreatedByUsername(u.getRealUsername());
            response.setCreatedByPhoto(u.getPhoto());
        }
        return response;
    }
    private String generateSlug(String text) {
        return text.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }
}
