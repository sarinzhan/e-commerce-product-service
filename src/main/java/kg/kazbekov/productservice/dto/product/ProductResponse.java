package kg.kazbekov.productservice.dto.product;

import com.fasterxml.jackson.annotation.JsonInclude;
import kg.kazbekov.productservice.model.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductResponse {

    private UUID id;
    private String sku;
    private String name;
    private String slug;
    private String description;
    private String brandName;

    private BigDecimal price;
    private BigDecimal compareAtPrice;
    private ProductStatus status;

    private CategoryInfo category;
    private Map<String, Object> attributes;
    private List<ImageInfo> images;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryInfo {
        private UUID id;
        private String name;
        private String slug;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageInfo {
        private String url;
        private String alt;
        private Boolean primary;
    }
}
