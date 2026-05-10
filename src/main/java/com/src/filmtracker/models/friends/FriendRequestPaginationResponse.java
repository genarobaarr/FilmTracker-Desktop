package com.src.filmtracker.models.friends;

import com.src.filmtracker.models.common.PaginationDto;
import java.util.List;

public record FriendRequestPaginationResponse(
    List<FriendRequestItemDto> data,
    PaginationDto pagination
) {}