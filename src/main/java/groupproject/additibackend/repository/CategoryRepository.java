package groupproject.additibackend.repository;

import groupproject.additibackend.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository  extends JpaRepository<Category,Long> {

    Optional<Category> findBySlug(String slug);

    List<Category> findByIsActiveTrue();

    boolean existsByNameIgnoreCase(String name);

    boolean existsBySlug(String slug);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId")
    Long countProductsByCategoryId(Long categoryId);

}
