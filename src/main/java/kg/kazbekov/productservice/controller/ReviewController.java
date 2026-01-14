package kg.kazbekov.productservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kg.kazbekov.productservice.dto.ApiResponse;
import kg.kazbekov.productservice.dto.PagedResponse;
import kg.kazbekov.productservice.dto.review.ProductRatingResponse;
import kg.kazbekov.productservice.dto.review.ReviewCreateRequest;
import kg.kazbekov.productservice.dto.review.ReviewResponse;
import kg.kazbekov.productservice.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "API для управления отзывами")
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/api/v1/products/{productId}/reviews")
    @Operation(summary = "Получить отзывы товара")
    public ResponseEntity<PagedResponse<ReviewResponse>> getProductReviews(
            @Parameter(description = "UUID товара") @PathVariable UUID productId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        log.info("Getting reviews for product: {}", productId);
        PagedResponse<ReviewResponse> reviews = reviewService.getProductReviews(productId, page, limit);
        return ResponseEntity.ok(reviews);
    }

    @PostMapping("/api/v1/products/{productId}/reviews")
    @Operation(summary = "Добавить отзыв к товару")
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @Parameter(description = "UUID товара") @PathVariable UUID productId,
            @Valid @RequestBody ReviewCreateRequest request
    ) {
        log.info("Creating review for product: {}, rating: {}", productId, request.getRating());
        ReviewResponse review = reviewService.createReview(productId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(review, "Review created successfully"));
    }

    @DeleteMapping("/api/v1/reviews/{reviewId}")
    @Operation(summary = "Удалить отзыв")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @Parameter(description = "UUID отзыва") @PathVariable UUID reviewId
    ) {
        log.warn("Deleting review: {}", reviewId);
        reviewService.deleteReview(reviewId);
        return ResponseEntity.ok(ApiResponse.success(null, "Review deleted successfully"));
    }

    @GetMapping("/api/v1/products/{productId}/rating")
    @Operation(summary = "Получить средний рейтинг товара")
    public ResponseEntity<ApiResponse<ProductRatingResponse>> getProductRating(
            @Parameter(description = "UUID товара") @PathVariable UUID productId
    ) {
        log.info("Getting rating for product: {}", productId);
        ProductRatingResponse rating = reviewService.getProductRating(productId);
        return ResponseEntity.ok(ApiResponse.success(rating));
    }
}
