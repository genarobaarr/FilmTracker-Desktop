package com.src.filmtracker.models.shows;

import com.src.filmtracker.models.shows.Show;

public record SearchResponse(
    Double score,
    Show show
) {}