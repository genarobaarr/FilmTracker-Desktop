package com.src.filmtracker.models;

import java.util.List;

public record HomeResponse(
    List<Show> featured,
    List<Show> topRated,
    List<Show> recent,
    List<Show> ended
) {}
