package com.src.filmtracker.models.notifications;

public record NotificationMetadataDto(
    Integer tvmazeId,
    String authId,
    Integer reviewId,
    String source
) {
}