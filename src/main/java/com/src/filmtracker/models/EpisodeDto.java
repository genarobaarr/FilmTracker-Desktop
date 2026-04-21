package com.src.filmtracker.models;

public record EpisodeDto(
    String name, 
    Integer season, 
    Integer number, 
    String summary, 
    ImageDto image, 
    String airdate,
    Integer runtime
) {}