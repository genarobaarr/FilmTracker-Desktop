package com.src.filmtracker.models.moderation;

public record ReportRequest(
    String targetType,
    String targetId,
    String reason,
    String description
) {}