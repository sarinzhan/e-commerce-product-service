package kg.kazbekov.productservice.mapper;

import kg.kazbekov.productservice.dto.product.ProductResponse;
import kg.kazbekov.productservice.model.Product;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class ProductMapper {

    public ProductResponse toResponse(Product product) {
        if (product == null) {
            return null;
        }

        return ProductResponse.builder()
                .id(product.getId())
                .sku(product.getSku())
                .name(product.getName())
                .slug(product.getSlug())
                .description(product.getDescription())
                .brandName(product.getBrandName())
                .price(product.getPrice())
                .compareAtPrice(product.getCompareAtPrice())
                .status(product.getStatus())
                .category(mapCategory(product))
                .attributes(product.getAttributes())
                .images(mapImages(product.getImages()))
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    public List<ProductResponse> toResponseList(List<Product> products) {
        if (products == null) {
            return Collections.emptyList();
        }
        return products.stream()
                .map(this::toResponse)
                .toList();
    }

    private ProductResponse.CategoryInfo mapCategory(Product product) {
        if (product.getCategory() == null) {
            return null;
        }
        return ProductResponse.CategoryInfo.builder()
                .id(product.getCategory().getId())
                .name(product.getCategory().getName())
                .slug(product.getCategory().getSlug())
                .build();
    }

    private List<ProductResponse.ImageInfo> mapImages(List<Map<String, String>> images) {
        if (images == null || images.isEmpty()) {
            return null;
        }
        return images.stream()
                .map(img -> ProductResponse.ImageInfo.builder()
                        .url(img.get("url"))
                        .alt(img.get("alt"))
                        .primary(Boolean.parseBoolean(img.getOrDefault("primary", "false")))
                        .build())
                .toList();
    }
}
