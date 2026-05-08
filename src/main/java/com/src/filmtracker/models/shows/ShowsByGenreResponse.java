package com.src.filmtracker.models.shows;

import com.src.filmtracker.models.shows.Show;
import java.util.List;

public record ShowsByGenreResponse(
    String genre,
    List<Show> results
) {}