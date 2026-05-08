package com.src.filmtracker.services.reviews;

import com.src.filmtracker.models.reviews.ReviewPaginationResponse;
import com.src.filmtracker.models.reviews.CommentPaginationResponse;
import com.src.filmtracker.models.reviews.CommentRequest;
import com.src.filmtracker.models.reviews.ReviewRequest;
import com.src.filmtracker.models.reviews.CommentDto;
import com.src.filmtracker.models.reviews.ReviewDto;
import com.src.filmtracker.models.reviews.ReviewSummaryDto;
import java.util.concurrent.CompletableFuture;

public interface IReviewService {
    CompletableFuture<ReviewPaginationResponse> getShowReviews(Integer tvmazeId, int page);
    CompletableFuture<ReviewDto> createReview(ReviewRequest request);
    CompletableFuture<ReviewDto> updateReview(String reviewId, ReviewRequest request);
    CompletableFuture<Void> deleteReview(String reviewId);
    CompletableFuture<Void> toggleReviewLike(String reviewId, boolean isCurrentlyLiked);
    
    CompletableFuture<CommentPaginationResponse> getReviewComments(String reviewId, int page);
    CompletableFuture<CommentDto> createComment(String reviewId, CommentRequest request);
    CompletableFuture<CommentDto> updateComment(String commentId, CommentRequest request);
    CompletableFuture<Void> deleteComment(String commentId);
    CompletableFuture<Void> toggleCommentLike(String commentId, boolean isCurrentlyLiked);
    CompletableFuture<ReviewPaginationResponse> getUserReviews(String authId, int page);
    CompletableFuture<ReviewSummaryDto> getUserSummary(String authId);
}