package groupproject.additibackend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name="categories",indexes = {
        @Index(name="idx_category_slug",columnList = "slug")
})
public class Category {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Category name is required")
    @Size(min=1, max=100,message = "Category name must be between 2 and 100 characters")
    @Column(nullable = false, unique = true, length = 100)
    private String name;


    @NotBlank(message = "Slug is required")
    @Size(min =2, max =100 ,message = "Slug must be between 2 and 100 characters ")
    @Column(unique = true, length = 100)
    private String slug;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Column(length = 500)
    private String description;

    @NotNull(message = "Active status is required")
    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Column(updatable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "category",cascade = CascadeType.ALL)
    private List<Product> products = new ArrayList<>();

    private String generateSlug(String text) {
        return text.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }

}