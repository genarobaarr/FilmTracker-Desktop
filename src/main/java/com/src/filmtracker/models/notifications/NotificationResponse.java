package com.src.filmtracker.models.notifications;

import com.src.filmtracker.models.common.PaginationDto;
import java.util.List;

public record NotificationResponse(
    List<NotificationDto> data,
    PaginationDto pagination
) {
}