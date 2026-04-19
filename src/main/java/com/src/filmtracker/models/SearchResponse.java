package com.src.filmtracker.models;

public record SearchResponse(
    Double score,
    Show show
) {}