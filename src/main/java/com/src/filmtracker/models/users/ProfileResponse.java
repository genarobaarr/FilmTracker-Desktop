package com.src.filmtracker.models.users;

public record ProfileResponse(
        String message, 
        UserDto data
    ) {}