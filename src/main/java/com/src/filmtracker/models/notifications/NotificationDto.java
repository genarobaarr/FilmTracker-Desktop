package com.src.filmtracker.models.notifications;

import com.google.gson.annotations.SerializedName;

public record NotificationDto(
    Integer id,
    @SerializedName(value = "recipient_auth_id", alternate = {"recipientAuthId"}) String recipientAuthId,
    @SerializedName(value = "actor_auth_id", alternate = {"actorAuthId"}) String actorAuthId,
    String type,
    String title,
    String body,
    NotificationMetadataDto metadata,
    @SerializedName(value = "read_at", alternate = {"readAt"}) String readAt,
    @SerializedName(value = "created_at", alternate = {"createdAt"}) String createdAt
) {
    public boolean isRead() {
        return readAt != null;
    }
}