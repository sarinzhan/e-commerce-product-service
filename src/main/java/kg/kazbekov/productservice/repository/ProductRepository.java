package kg.kazbekov.productservice.repository;

import io.micrometer.observation.annotation.Observed;
import kg.kazbekov.productservice.model.Product;
import kg.kazbekov.productservice.model.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    Optional<Product> findBySku(String sku);

    Optional<Product> findBySlug(String slug);

    boolean existsBySku(String sku);

    boolean existsBySlug(String slug);

    Page<Product> findByStatus(ProductStatus status, Pageable pageable);

    Page<Product> findByCategoryId(UUID categoryId, Pageable pageable);

    @Query(value = "SELECT * FROM products p WHERE " +
            "(:categoryId IS NULL OR p.category_id = CAST(:categoryId AS UUID)) AND " +
            "(:status IS NULL OR p.status = CAST(:status AS VARCHAR)) AND " +
            "(:search IS NULL OR LOWER(p.name) LIKE LOWER('%' || CAST(:search AS VARCHAR) || '%')) " +
            "ORDER BY p.created_at DESC",
            countQuery = "SELECT COUNT(*) FROM products p WHERE " +
                    "(:categoryId IS NULL OR p.category_id = CAST(:categoryId AS UUID)) AND " +
                    "(:status IS NULL OR p.status = CAST(:status AS VARCHAR)) AND " +
                    "(:search IS NULL OR LOWER(p.name) LIKE LOWER('%' || CAST(:search AS VARCHAR) || '%'))",
            nativeQuery = true)
    Page<Product> findWithFilters(
            @Param("categoryId") UUID categoryId,
            @Param("status") String status,
            @Param("search") String search,
            Pageable pageable
    );
}
