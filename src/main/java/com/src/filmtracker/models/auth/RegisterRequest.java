package com.src.filmtracker.models.auth;

public record RegisterRequest(
    String username,
    String name, 
    String email, 
    String password
) {}