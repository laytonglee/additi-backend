package groupproject.additibackend.repository;

import groupproject.additibackend.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductReponsitory extends JpaRepository<Product, Long> {

    @Query("""
    select distinct p from Product p
    join fetch p.category c
    left join fetch p.productVariants v
    left join fetch v.images i
    where p.isActive = true
  """)
    List<Product> findAllActiveWithDetails();

    @Query("""
    select distinct p from Product p
    join fetch p.category c
    left join fetch p.productVariants v
    left join fetch v.images i
    where p.id = :id
  """)
    Optional<Product> findDetailById(@Param("id") Long id);
}
