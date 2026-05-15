package com.src.filmtracker.models.admin;

import java.util.Map;

public record ReviewStatsDto(
    Map<String, Number> totals
) {
}