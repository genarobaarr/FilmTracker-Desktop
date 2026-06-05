package com.src.filmtracker.models.shows;

public record SearchResponse(
    Double score,
    Show show
) {}