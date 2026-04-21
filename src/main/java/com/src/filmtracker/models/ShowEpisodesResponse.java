package com.src.filmtracker.models;

import java.util.List;

public record ShowEpisodesResponse(
    Integer tvmazeShowId,
    List<EpisodeDto> episodes
) {}