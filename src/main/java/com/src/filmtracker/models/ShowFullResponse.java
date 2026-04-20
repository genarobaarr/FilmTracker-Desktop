package com.src.filmtracker.models;

import java.util.List;

public record ShowFullResponse(
    Show show,
    List<SeasonDto> seasons,
    List<CastDto> cast
) {}