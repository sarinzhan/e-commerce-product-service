package kg.kazbekov.productservice.service;

import kg.kazbekov.productservice.dto.PageMeta;
import kg.kazbekov.productservice.dto.PagedResponse;
import kg.kazbekov.productservice.dto.review.ProductRatingResponse;
import kg.kazbekov.productservice.dto.review.ReviewCreateRequest;
import kg.kazbekov.productservice.dto.review.ReviewResponse;
import kg.kazbekov.productservice.exception.DuplicateResourceException;
import kg.kazbekov.productservice.exception.ResourceNotFoundException;
import kg.kazbekov.productservice.mapper.ReviewMapper;
import kg.kazbekov.productservice.model.Product;
import kg.kazbekov.productservice.model.Review;
import kg.kazbekov.productservice.repository.ProductRepository;
import kg.kazbekov.productservice.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final ReviewMapper reviewMapper;

    @Transactional(readOnly = true)
    public PagedResponse<ReviewResponse> getProductReviews(UUID productId, int page, int limit) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product", "id", productId);
        }

        Page<Review> reviewPage = reviewRepository.findByProductIdOrderByCreatedAtDesc(
                productId,
                PageRequest.of(page - 1, limit)
        );

        return PagedResponse.of(
                reviewMapper.toResponseList(reviewPage.getContent()),
                PageMeta.of(page, limit, reviewPage.getTotalElements())
        );
    }

    @Transactional
    public ReviewResponse createReview(UUID productId, ReviewCreateRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        if (reviewRepository.existsByProductIdAndUserId(productId, request.getUserId())) {
            throw new DuplicateResourceException("User already reviewed this product");
        }

        Review review = new Review();
        review.setProduct(product);
        review.setUserId(request.getUserId());
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        review = reviewRepository.save(review);
        return reviewMapper.toResponse(review);
    }

    @Transactional
    public void deleteReview(UUID reviewId) {
        if (!reviewRepository.existsById(reviewId)) {
            throw new ResourceNotFoundException("Review", "id", reviewId);
        }
        reviewRepository.deleteById(reviewId);
    }

    @Transactional(readOnly = true)
    public ProductRatingResponse getProductRating(UUID productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product", "id", productId);
        }

        Double avgRating = reviewRepository.getAverageRatingByProductId(productId);
        Long totalReviews = reviewRepository.countByProductId(productId);

        return ProductRatingResponse.builder()
                .averageRating(avgRating != null ? Math.round(avgRating * 10) / 10.0 : null)
                .totalReviews(totalReviews)
                .build();
    }
}
