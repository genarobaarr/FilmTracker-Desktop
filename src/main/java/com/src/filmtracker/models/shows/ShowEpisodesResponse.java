package com.src.filmtracker.models.shows;

import java.util.List;

public record ShowEpisodesResponse(
    Integer tvmazeShowId,
    List<EpisodeDto> episodes
) {}