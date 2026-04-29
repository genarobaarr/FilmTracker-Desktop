package com.src.filmtracker.models;

public record RegisterRequest(
    String username,
    String name, 
    String email, 
    String password
) {}