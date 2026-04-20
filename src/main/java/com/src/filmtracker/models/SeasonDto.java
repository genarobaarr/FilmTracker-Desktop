package com.src.filmtracker.models;

public record SeasonDto(
    Integer number, 
    Integer episodeOrder, 
    String premiereDate, 
    String endDate, 
    ImageDto image, 
    String summary
) {}