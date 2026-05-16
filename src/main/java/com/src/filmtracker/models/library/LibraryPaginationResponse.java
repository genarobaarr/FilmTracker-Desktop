package com.src.filmtracker.models.library;

import com.src.filmtracker.models.common.PaginationDto;
import java.util.List;

public record LibraryPaginationResponse(
    List<LibraryItemDto> data,
    PaginationDto pagination
) {
}