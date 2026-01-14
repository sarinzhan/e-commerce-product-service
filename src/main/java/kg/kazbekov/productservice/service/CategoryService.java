package kg.kazbekov.productservice.service;

import kg.kazbekov.productservice.dto.category.CategoryCreateRequest;
import kg.kazbekov.productservice.dto.category.CategoryResponse;
import kg.kazbekov.productservice.dto.category.CategoryUpdateRequest;
import kg.kazbekov.productservice.exception.DuplicateResourceException;
import kg.kazbekov.productservice.exception.ResourceNotFoundException;
import kg.kazbekov.productservice.mapper.CategoryMapper;
import kg.kazbekov.productservice.model.Category;
import kg.kazbekov.productservice.repository.CategoryRepository;
import kg.kazbekov.productservice.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategoryTree() {
        List<Category> rootCategories = categoryRepository.findByParentIsNullAndIsActiveTrueOrderByNameAsc();
        return rootCategories.stream()
                .map(categoryMapper::toResponseWithChildren)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        return categoryMapper.toResponseWithChildren(category);
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategoryBySlug(String slug) {
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "slug", slug));
        return categoryMapper.toResponseWithChildren(category);
    }

    @Transactional
    public CategoryResponse createCategory(CategoryCreateRequest request) {
        String slug = request.getSlug() != null
                ? request.getSlug()
                : SlugUtil.toSlug(request.getName());

        if (categoryRepository.existsBySlug(slug)) {
            throw new DuplicateResourceException("Category", "slug", slug);
        }

        Category parent = null;
        if (request.getParentId() != null) {
            parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getParentId()));
        }

        Category category = new Category();
        category.setParent(parent);
        category.setName(request.getName());
        category.setSlug(slug);
        category.setImageUrl(request.getImageUrl());
        category.setIsActive(request.getIsActive());

        category = categoryRepository.save(category);
        return categoryMapper.toResponse(category);
    }

    @Transactional
    public CategoryResponse updateCategory(UUID id, CategoryUpdateRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        if (request.getName() != null) {
            category.setName(request.getName());
        }

        if (request.getSlug() != null) {
            if (!category.getSlug().equals(request.getSlug()) &&
                    categoryRepository.existsBySlug(request.getSlug())) {
                throw new DuplicateResourceException("Category", "slug", request.getSlug());
            }
            category.setSlug(request.getSlug());
        }

        if (request.getImageUrl() != null) {
            category.setImageUrl(request.getImageUrl());
        }

        if (request.getIsActive() != null) {
            category.setIsActive(request.getIsActive());
        }

        if (request.getParentId() != null) {
            Category newParent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getParentId()));
            category.setParent(newParent);
        }

        category = categoryRepository.save(category);
        return categoryMapper.toResponse(category);
    }

    @Transactional
    public void deleteCategory(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        if (!category.getChildren().isEmpty()) {
            throw new IllegalArgumentException("Cannot delete category with subcategories");
        }

        if (!category.getProducts().isEmpty()) {
            throw new IllegalArgumentException("Cannot delete category with products");
        }

        categoryRepository.delete(category);
    }
}
