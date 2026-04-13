package com.src.filmtracker.models;

import java.util.List;

public record Show(
    Integer tvmazeId,
    String name,
    String type,
    String language,
    List<String> genres,
    String status,
    RatingDto rating,
    ImageDto image,
    String summary
) {}
