package com.src.filmtracker.services;

import com.src.filmtracker.models.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface IReviewService {
    CompletableFuture<List<ReviewDto>> getShowReviews(Integer tvmazeId);
    CompletableFuture<ReviewDto> createReview(ReviewRequest request);
    CompletableFuture<ReviewDto> updateReview(String reviewId, ReviewRequest request);
    CompletableFuture<Void> deleteReview(String reviewId);
    CompletableFuture<Void> toggleReviewLike(String reviewId, boolean isCurrentlyLiked);
    CompletableFuture<Boolean> isReviewLikedByMe(String reviewId);
    
    CompletableFuture<List<CommentDto>> getReviewComments(String reviewId);
    CompletableFuture<CommentDto> createComment(String reviewId, CommentRequest request);
    CompletableFuture<CommentDto> updateComment(String commentId, CommentRequest request);
    CompletableFuture<Void> deleteComment(String commentId);
    CompletableFuture<Void> toggleCommentLike(String commentId, boolean isCurrentlyLiked);
    CompletableFuture<Boolean> isCommentLikedByMe(String commentId);
    CompletableFuture<List<ReviewDto>> getUserReviews(String authId);
}