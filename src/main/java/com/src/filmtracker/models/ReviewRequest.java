package com.src.filmtracker.models;

public record ReviewRequest(
    Integer tvmazeId, 
    Integer rating, 
    String title, 
    String content
) {}