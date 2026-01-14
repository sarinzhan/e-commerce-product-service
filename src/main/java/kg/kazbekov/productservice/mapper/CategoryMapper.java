package kg.kazbekov.productservice.mapper;

import kg.kazbekov.productservice.dto.category.CategoryResponse;
import kg.kazbekov.productservice.model.Category;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class CategoryMapper {

    public CategoryResponse toResponse(Category category) {
        if (category == null) {
            return null;
        }

        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .imageUrl(category.getImageUrl())
                .isActive(category.getIsActive())
                .build();
    }

    public CategoryResponse toResponseWithChildren(Category category) {
        if (category == null) {
            return null;
        }

        List<CategoryResponse> children = category.getChildren() != null
                ? category.getChildren().stream()
                    .filter(Category::getIsActive)
                    .map(this::toResponseWithChildren)
                    .toList()
                : Collections.emptyList();

        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .imageUrl(category.getImageUrl())
                .isActive(category.getIsActive())
                .children(children.isEmpty() ? null : children)
                .build();
    }

    public List<CategoryResponse> toResponseList(List<Category> categories) {
        return categories.stream()
                .map(this::toResponse)
                .toList();
    }
}
