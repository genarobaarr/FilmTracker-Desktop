package com.src.filmtracker.models;

import java.util.List;

public record CommentPaginationResponse(
    List<CommentDto> comments,
    PaginationDto pagination
) {}