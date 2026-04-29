package com.src.filmtracker.models;

public record RegisterResponse(
        String message, 
        UserDto data
    ) {}