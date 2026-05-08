package com.src.filmtracker.models.auth;

public record LoginRequest(
    String email, 
    String password
) {}