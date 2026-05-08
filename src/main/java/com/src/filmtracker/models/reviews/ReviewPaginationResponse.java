package com.src.filmtracker.models.reviews;

import com.src.filmtracker.models.reviews.ReviewDto;
import com.src.filmtracker.models.common.PaginationDto;
import java.util.List;

public record ReviewPaginationResponse(
    List<ReviewDto> reviews,
    PaginationDto pagination
) {}