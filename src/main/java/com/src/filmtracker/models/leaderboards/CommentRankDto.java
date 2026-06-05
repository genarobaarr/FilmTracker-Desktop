package com.src.filmtracker.models.leaderboards;

import com.google.gson.annotations.SerializedName;
import com.src.filmtracker.utils.DtoHelper;

public record CommentRankDto(
    Integer rank,
    @SerializedName(value = "comment_id", alternate = {"commentId"}) String commentId,
    @SerializedName(value = "review_id", alternate = {"reviewId"}) String reviewId,
    @SerializedName(value = "auth_id", alternate = {"authId"}) String authId,
    @SerializedName(value = "username", alternate = {"user_name", "user_username"}) String username,
    String content,
    @SerializedName(value = "image_url", alternate = {"imageUrl"}) String imageUrl,
    @SerializedName(value = "created_at", alternate = {"createdAt"}) String createdAt,
    @SerializedName(value = "likes_count", alternate = {"likesCount"}) Integer likesCount
) {
    public int getSafeLikesCount() {
        return DtoHelper.parseSafeInteger(likesCount);
    }
}