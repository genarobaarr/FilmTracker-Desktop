package com.src.filmtracker.models;

public record VerifyEmailRequest(
    String email, 
    String code
) {}