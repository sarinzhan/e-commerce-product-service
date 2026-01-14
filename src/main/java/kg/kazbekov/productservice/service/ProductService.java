package kg.kazbekov.productservice.service;

import kg.kazbekov.productservice.dto.PageMeta;
import kg.kazbekov.productservice.dto.PagedResponse;
import kg.kazbekov.productservice.dto.product.ProductCreateRequest;
import kg.kazbekov.productservice.dto.product.ProductResponse;
import kg.kazbekov.productservice.dto.product.ProductStatusUpdateRequest;
import kg.kazbekov.productservice.dto.product.ProductUpdateRequest;
import kg.kazbekov.productservice.exception.DuplicateResourceException;
import kg.kazbekov.productservice.exception.ResourceNotFoundException;
import kg.kazbekov.productservice.mapper.ProductMapper;
import kg.kazbekov.productservice.model.Category;
import kg.kazbekov.productservice.model.Product;
import kg.kazbekov.productservice.model.ProductStatus;
import kg.kazbekov.productservice.repository.CategoryRepository;
import kg.kazbekov.productservice.repository.ProductRepository;
import kg.kazbekov.productservice.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    @Transactional(readOnly = true)
    public PagedResponse<ProductResponse> getProducts(int page, int limit, UUID categoryId,
                                                       ProductStatus status, String search, String sort) {
        Pageable pageable = PageRequest.of(page - 1, limit);
        String statusStr = status != null ? status.name() : null;
        Page<Product> productPage = productRepository.findWithFilters(categoryId, statusStr, search, pageable);

        return PagedResponse.of(
                productMapper.toResponseList(productPage.getContent()),
                PageMeta.of(page, limit, productPage.getTotalElements())
        );
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        return productMapper.toResponse(product);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "slug", slug));
        return productMapper.toResponse(product);
    }

    @Transactional
    public ProductResponse createProduct(ProductCreateRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));

        String sku = request.getSku() != null ? request.getSku() : generateSku();
        if (productRepository.existsBySku(sku)) {
            throw new DuplicateResourceException("Product", "sku", sku);
        }

        String slug = SlugUtil.toSlug(request.getName());
        slug = ensureUniqueSlug(slug, null);

        Product product = new Product();
        product.setSku(sku);
        product.setName(request.getName());
        product.setSlug(slug);
        product.setDescription(request.getDescription());
        product.setBrandName(request.getBrandName());
        product.setCategory(category);
        product.setPrice(request.getPrice());
        product.setCompareAtPrice(request.getCompareAtPrice());
        product.setAttributes(request.getAttributes());

        if (request.getStatus() != null) {
            product.setStatus(ProductStatus.valueOf(request.getStatus()));
        }

        if (request.getImages() != null) {
            product.setImages(request.getImages().stream()
                    .map(img -> Map.of(
                            "url", img.getUrl(),
                            "alt", img.getAlt() != null ? img.getAlt() : "",
                            "primary", String.valueOf(img.getPrimary())
                    ))
                    .toList());
        }

        product = productRepository.save(product);
        return productMapper.toResponse(product);
    }

    @Transactional
    public ProductResponse updateProduct(UUID id, ProductUpdateRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        if (request.getName() != null) {
            product.setName(request.getName());
            String newSlug = SlugUtil.toSlug(request.getName());
            product.setSlug(ensureUniqueSlug(newSlug, id));
        }

        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }

        if (request.getBrandName() != null) {
            product.setBrandName(request.getBrandName());
        }

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));
            product.setCategory(category);
        }

        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }

        if (request.getCompareAtPrice() != null) {
            product.setCompareAtPrice(request.getCompareAtPrice());
        }

        if (request.getAttributes() != null) {
            product.setAttributes(request.getAttributes());
        }

        if (request.getImages() != null) {
            product.setImages(request.getImages().stream()
                    .map(img -> Map.of(
                            "url", img.getUrl(),
                            "alt", img.getAlt() != null ? img.getAlt() : "",
                            "primary", String.valueOf(img.getPrimary())
                    ))
                    .toList());
        }

        product = productRepository.save(product);
        return productMapper.toResponse(product);
    }

    @Transactional
    public ProductResponse updateProductStatus(UUID id, ProductStatusUpdateRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        product.setStatus(request.getStatus());
        product = productRepository.save(product);

        return productMapper.toResponse(product);
    }

    @Transactional
    public void deleteProduct(UUID id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product", "id", id);
        }
        productRepository.deleteById(id);
    }

    private String generateSku() {
        return "SKU-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String ensureUniqueSlug(String baseSlug, UUID excludeId) {
        String slug = baseSlug;
        int counter = 1;

        while (true) {
            var existing = productRepository.findBySlug(slug);
            if (existing.isEmpty() || (excludeId != null && existing.get().getId().equals(excludeId))) {
                break;
            }
            slug = baseSlug + "-" + counter++;
        }

        return slug;
    }
}
