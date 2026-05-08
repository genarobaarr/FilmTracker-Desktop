package com.src.filmtracker.models.auth;

import com.src.filmtracker.models.users.UserDto;

public record RegisterResponse(
        String message, 
        UserDto data
    ) {}