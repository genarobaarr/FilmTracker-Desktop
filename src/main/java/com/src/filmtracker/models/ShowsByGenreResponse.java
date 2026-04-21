package com.src.filmtracker.models;

import java.util.List;

public record ShowsByGenreResponse(
    String genre,
    List<Show> results
) {}