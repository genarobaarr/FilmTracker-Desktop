package com.src.filmtracker.models;

public record UserDto(
    String id, 
    String username, 
    String email, 
    String role, 
    String name, 
    String profileImage
) {}