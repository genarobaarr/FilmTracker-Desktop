package com.src.filmtracker.models.shows;

import java.util.List;

public record ShowsByGenreResponse(
    String genre,
    List<Show> results
) {}