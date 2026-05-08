package com.src.filmtracker.models.shows;

import com.src.filmtracker.models.common.ImageDto;

public record EpisodeDto(
    String name, 
    Integer season, 
    Integer number, 
    String summary, 
    ImageDto image, 
    String airdate,
    Integer runtime
) {}