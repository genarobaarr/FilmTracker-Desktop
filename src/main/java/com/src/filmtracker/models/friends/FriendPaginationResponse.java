package com.src.filmtracker.models.friends;

import com.src.filmtracker.models.common.PaginationDto;
import java.util.List;

public record FriendPaginationResponse(
    List<FriendItemDto> data,
    PaginationDto pagination
) {}