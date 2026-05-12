package com.src.filmtracker.models.moderation;

import com.google.gson.annotations.SerializedName;

public record ReportDto(
    Integer id,
    @SerializedName(value = "reporter_auth_id", alternate = {"reporterAuthId"})
    String reporterAuthId,
    @SerializedName(value = "target_type", alternate = {"targetType"})
    String targetType,
    @SerializedName(value = "target_id", alternate = {"targetId"})
    String targetId,
    String reason,
    String description,
    String status,
    @SerializedName(value = "created_at", alternate = {"createdAt"})
    String createdAt
) {
}