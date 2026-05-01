package com.src.filmtracker.models;

import java.util.List;

public record ReviewPaginationResponse(
    List<ReviewDto> reviews,
    PaginationDto pagination
) {}