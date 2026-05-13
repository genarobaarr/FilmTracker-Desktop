package com.src.filmtracker.models.leaderboards;

import com.google.gson.annotations.SerializedName;

public record UserRankDto(
    Integer rank,
    @SerializedName("auth_id") String authId,
    @SerializedName("total_likes") Integer totalLikes,
    @SerializedName("review_likes") Integer reviewLikes,
    @SerializedName("comment_likes") Integer commentLikes
) {}