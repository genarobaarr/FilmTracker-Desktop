package com.src.filmtracker.models.common;

public record PaginationDto(
    Integer page,
    Integer limit,
    Integer total,
    Integer totalPages,
    Boolean hasNextPage,
    Boolean hasPreviousPage
) {}