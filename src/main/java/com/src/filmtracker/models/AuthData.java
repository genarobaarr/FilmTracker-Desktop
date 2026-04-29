package com.src.filmtracker.models;

public record AuthData(
        UserDto user, 
        String token
    ) {}
