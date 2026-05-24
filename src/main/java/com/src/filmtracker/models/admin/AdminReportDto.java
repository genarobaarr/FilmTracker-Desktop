package com.src.filmtracker.models.admin;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;

public record AdminReportDto(
    Integer id,
    @SerializedName(value = "reporter_auth_id", alternate = {"reporterAuthId"}) String reporterAuthId,
    @SerializedName(value = "target_type", alternate = {"targetType"}) String targetType,
    @SerializedName(value = "target_id", alternate = {"targetId"}) String targetId,
    String reason,
    String description,
    String status,
    @SerializedName(value = "target_snapshot", alternate = {"targetSnapshot"}) Map<String, Object> targetSnapshot,
    @SerializedName(value = "admin_note", alternate = {"adminNote"}) String adminNote,
    List<String> availableActions,
    @SerializedName(value = "created_at", alternate = {"createdAt"}) String createdAt
) {
}