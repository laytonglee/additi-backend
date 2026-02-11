package groupproject.additibackend.repository;

import groupproject.additibackend.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository  extends JpaRepository<Category,Long> {

    Optional<Category> findBySlug(String slug);

    List<Category> findByIsActiveTrue();

    boolean existsByNameIgnoreCase(String name);

    List<Category> findByIsActive(Boolean isActive);

    List<Category> findByNameContainingIgnoreCase(String name);

    @Query("""
SELECT c FROM Category c
WHERE (c.isActive = COALESCE(:isActive, c.isActive))
  AND (
      COALESCE(:search, '') = '' OR
      LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) OR
      LOWER(c.description) LIKE LOWER(CONCAT('%', :search, '%'))
  )
  AND (c.createdAt >= COALESCE(:createdAtStart, c.createdAt))
  AND (c.createdAt <= COALESCE(:createdAtEnd, c.createdAt))
  AND (
      :createdById IS NULL
      OR c.createdBy.id = :createdById
  )
ORDER BY c.id DESC
""")
    List<Category> findByFilters(
            @Param("isActive") Boolean isActive,
            @Param("search") String search,
            @Param("createdAtStart") LocalDateTime createdAtStart,
            @Param("createdAtEnd") LocalDateTime createdAtEnd,
            @Param("createdById") Long createdById
    );



}
