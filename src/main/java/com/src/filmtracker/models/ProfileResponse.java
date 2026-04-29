package com.src.filmtracker.models;

public record ProfileResponse(
        String message, 
        UserDto data
    ) {}