package com.src.filmtracker.models;

public record AuthResponse(
    UserDto user, 
    String token
) {}