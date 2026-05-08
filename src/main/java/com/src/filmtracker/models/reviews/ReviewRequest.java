package com.src.filmtracker.models.reviews;

public record ReviewRequest(
    Integer tvmazeId, 
    Integer rating, 
    String title, 
    String content
) {}