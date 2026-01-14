package kg.kazbekov.productservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponse<T> {
    private List<T> data;
    private PageMeta meta;

    public static <T> PagedResponse<T> of(List<T> data, PageMeta meta) {
        return PagedResponse.<T>builder()
                .data(data)
                .meta(meta)
                .build();
    }
}
