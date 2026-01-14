package kg.kazbekov.productservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageMeta {
    private int page;
    private int limit;
    private long totalCount;
    private int totalPages;

    public static PageMeta of(int page, int limit, long totalCount) {
        return PageMeta.builder()
                .page(page)
                .limit(limit)
                .totalCount(totalCount)
                .totalPages((int) Math.ceil((double) totalCount / limit))
                .build();
    }
}
