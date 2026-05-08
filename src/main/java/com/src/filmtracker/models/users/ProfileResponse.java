package com.src.filmtracker.models.users;

import com.src.filmtracker.models.users.UserDto;

public record ProfileResponse(
        String message, 
        UserDto data
    ) {}