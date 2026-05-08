package com.src.filmtracker.models.reviews;

public record ReviewSummaryDto(
    String authId,
    Integer reviewsCount,
    Integer totalLikesReceived
) {}