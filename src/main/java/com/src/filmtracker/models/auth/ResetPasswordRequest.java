package com.src.filmtracker.models.auth;

public record ResetPasswordRequest(
    String token,
    String password
) {}