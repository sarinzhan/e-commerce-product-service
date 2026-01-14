package kg.kazbekov.productservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kg.kazbekov.productservice.dto.ApiResponse;
import kg.kazbekov.productservice.dto.PagedResponse;
import kg.kazbekov.productservice.dto.product.ProductCreateRequest;
import kg.kazbekov.productservice.dto.product.ProductResponse;
import kg.kazbekov.productservice.dto.product.ProductStatusUpdateRequest;
import kg.kazbekov.productservice.dto.product.ProductUpdateRequest;
import kg.kazbekov.productservice.model.ProductStatus;
import kg.kazbekov.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "API для управления товарами")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Получить список товаров")
    public ResponseEntity<PagedResponse<ProductResponse>> getProducts(
            @Parameter(description = "Номер страницы") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Размер страницы") @RequestParam(defaultValue = "20") int limit,
            @Parameter(description = "ID категории") @RequestParam(name = "category_id", required = false) UUID categoryId,
            @Parameter(description = "Статус") @RequestParam(required = false) ProductStatus status,
            @Parameter(description = "Поиск по названию") @RequestParam(required = false) String search,
            @Parameter(description = "Сортировка (price:asc, name:desc)") @RequestParam(required = false) String sort
    ) {
        log.info("Getting products: page={}, limit={}, categoryId={}, status={}, search={}",
                page, limit, categoryId, status, search);
        PagedResponse<ProductResponse> response = productService.getProducts(page, limit, categoryId, status, search, sort);
        log.debug("Found {} products", response.getData().size());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить товар по ID")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(
            @Parameter(description = "UUID товара") @PathVariable UUID id) {
        log.info("Getting product by id: {}", id);
        ProductResponse product = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Получить товар по slug")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductBySlug(
            @Parameter(description = "Slug товара") @PathVariable String slug) {
        log.info("Getting product by slug: {}", slug);
        ProductResponse product = productService.getProductBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    @PostMapping
    @Operation(summary = "Создать товар")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody ProductCreateRequest request) {
        log.info("Creating product: {}", request.getName());
        ProductResponse product = productService.createProduct(request);
        log.info("Product created with id: {}", product.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(product, "Product created successfully"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить товар")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @Parameter(description = "UUID товара") @PathVariable UUID id,
            @Valid @RequestBody ProductUpdateRequest request) {
        log.info("Updating product: {}", id);
        ProductResponse product = productService.updateProduct(id, request);
        return ResponseEntity.ok(ApiResponse.success(product, "Product updated successfully"));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Изменить статус товара")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProductStatus(
            @Parameter(description = "UUID товара") @PathVariable UUID id,
            @Valid @RequestBody ProductStatusUpdateRequest request) {
        log.info("Updating product status: id={}, status={}", id, request.getStatus());
        ProductResponse product = productService.updateProductStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success(product, "Product status updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить товар")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @Parameter(description = "UUID товара") @PathVariable UUID id) {
        log.warn("Deleting product: {}", id);
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Product deleted successfully"));
    }
}
