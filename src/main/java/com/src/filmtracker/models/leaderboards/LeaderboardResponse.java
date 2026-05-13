package com.src.filmtracker.models.leaderboards;

import java.util.List;

public record LeaderboardResponse<T>(
    String period,
    Integer limit,
    List<T> top
) {}