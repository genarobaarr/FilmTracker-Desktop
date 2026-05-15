package com.src.filmtracker.models.admin;

public record AdminUserDetailDto(
    String id,
    String authId,
    String name,
    String username,
    String email,
    String profileImage,
    String role,
    Boolean isEmailVerified,
    String createdAt,
    String updatedAt
) {
}