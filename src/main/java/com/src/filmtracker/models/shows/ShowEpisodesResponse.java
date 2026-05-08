package com.src.filmtracker.models.shows;

import com.src.filmtracker.models.shows.EpisodeDto;
import java.util.List;

public record ShowEpisodesResponse(
    Integer tvmazeShowId,
    List<EpisodeDto> episodes
) {}