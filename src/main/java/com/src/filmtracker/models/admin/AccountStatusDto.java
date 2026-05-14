package com.src.filmtracker.models.admin;

public record AccountStatusDto(
    String accountStatus,
    String suspendedUntil,
    String moderationReason
) {
}