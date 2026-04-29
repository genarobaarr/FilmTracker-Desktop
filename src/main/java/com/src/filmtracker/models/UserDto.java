package com.src.filmtracker.models;

public record UserDto(
    String id,
    String authId,
    String name,
    String username,
    String email,
    String profileImage,
    String role,
    Boolean isEmailVerified,
    String createdAt
) {}