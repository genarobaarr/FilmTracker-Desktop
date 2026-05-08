package com.src.filmtracker.models.auth;

public record VerifyEmailRequest(
    String email, 
    String code
) {}