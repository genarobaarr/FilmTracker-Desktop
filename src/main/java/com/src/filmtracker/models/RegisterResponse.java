package com.src.filmtracker.models;

public record RegisterResponse(
    String id, 
    String email, 
    String role
) {}