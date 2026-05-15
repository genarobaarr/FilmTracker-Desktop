package com.src.filmtracker.models.admin;

import java.util.List;

public record ModerationStatsDto(
    Integer totalReports,
    Integer pendingReports,
    Integer resolvedReports,
    List<ReasonCountDto> reportsByReason,
    List<ActionCountDto> actionsTaken
) {
}