package com.src.filmtracker.models.shows;

import com.src.filmtracker.models.common.ImageDto;

public record SeasonDto(
    Integer number, 
    Integer episodeOrder, 
    String premiereDate, 
    String endDate, 
    ImageDto image, 
    String summary
) {}