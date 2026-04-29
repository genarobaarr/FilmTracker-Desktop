package com.src.filmtracker.models;

public record LoginRequest(
    String email, 
    String password
) {}