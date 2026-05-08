package com.src.filmtracker.models.shows;

import com.src.filmtracker.models.shows.Show;
import java.util.List;

public record HomeResponse(
    List<Show> featured,
    List<Show> topRated,
    List<Show> recent,
    List<Show> ended
) {}
