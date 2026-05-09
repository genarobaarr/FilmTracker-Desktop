package com.src.filmtracker.models.users;

public record UpdateProfileRequest(
    String name,
    String username,
    String profileImage
) {}