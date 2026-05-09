package com.src.filmtracker.models.auth;

public record ChangePasswordRequest(
    String currentPassword,
    String newPassword
) {}