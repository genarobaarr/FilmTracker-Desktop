package com.src.filmtracker.models.shows;

import com.src.filmtracker.models.shows.CastDto;
import com.src.filmtracker.models.shows.SeasonDto;
import com.src.filmtracker.models.shows.Show;
import java.util.List;

public record ShowFullResponse(
    Show show,
    List<SeasonDto> seasons,
    List<CastDto> cast
) {}