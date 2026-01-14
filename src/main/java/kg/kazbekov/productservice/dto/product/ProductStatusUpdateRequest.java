package kg.kazbekov.productservice.dto.product;

import jakarta.validation.constraints.NotNull;
import kg.kazbekov.productservice.model.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private ProductStatus status;
}
