package kg.kazbekov.productservice.repository;

import io.micrometer.observation.annotation.Observed;
import kg.kazbekov.productservice.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    Optional<Category> findBySlug(String slug);

    boolean existsBySlug(String slug);

    List<Category> findByParentIsNullAndIsActiveTrueOrderByNameAsc();

    List<Category> findByParentIdOrderByNameAsc(UUID parentId);

    List<Category> findByIsActiveTrueOrderByNameAsc();
}
