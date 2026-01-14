package kg.kazbekov.productservice.dto.product;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductUpdateRequest {

    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    private String description;

    @Size(max = 100, message = "Brand name must not exceed 100 characters")
    private String brandName;

    private UUID categoryId;

    @Positive(message = "Price must be positive")
    private BigDecimal price;

    private BigDecimal compareAtPrice;

    private Map<String, Object> attributes;

    private List<ProductCreateRequest.ImageRequest> images;
}
