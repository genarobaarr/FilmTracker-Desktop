package com.src.filmtracker.models.leaderboards;

import com.google.gson.annotations.SerializedName;

public record ReviewRankDto(
    Integer rank,
    @SerializedName(value = "review_id", alternate = {"reviewId"}) String reviewId,
    @SerializedName(value = "auth_id", alternate = {"authId"}) String authId,
    @SerializedName(value = "username", alternate = {"user_name", "user_username"}) String username,
    @SerializedName(value = "show_name", alternate = {"showName", "series_name", "seriesName"}) String showName,
    @SerializedName(value = "tvmaze_id", alternate = {"tvmazeId"}) Integer tvmazeId,
    Integer rating,
    String title,
    String content,
    @SerializedName(value = "image_url", alternate = {"imageUrl"}) String imageUrl,
    @SerializedName(value = "created_at", alternate = {"createdAt"}) String createdAt,
    @SerializedName(value = "likes_count", alternate = {"likesCount"}) Integer likesCount
) {
    public int getSafeLikesCount() {
        if (likesCount == null) {
            return 0;
        }
        
        return likesCount;
    }
}