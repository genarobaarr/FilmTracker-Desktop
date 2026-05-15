package com.src.filmtracker.models.admin;

import java.util.Map;

public record AuthStatsDto(
    Integer totalUsers,
    Map<String, Integer> newUsers,
    Map<String, Integer> byStatus,
    Map<String, Integer> byRole
) {
}