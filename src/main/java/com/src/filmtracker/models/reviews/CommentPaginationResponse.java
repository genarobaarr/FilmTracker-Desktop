package com.src.filmtracker.models.reviews;

import com.src.filmtracker.models.reviews.CommentDto;
import com.src.filmtracker.models.common.PaginationDto;
import java.util.List;

public record CommentPaginationResponse(
    List<CommentDto> comments,
    PaginationDto pagination
) {}