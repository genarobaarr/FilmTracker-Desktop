package com.src.filmtracker.models.friends;

import com.google.gson.annotations.SerializedName;

public record FriendStatusResponse(
    @SerializedName("status")
    String status,
    
    @SerializedName("relationship")
    FriendRequestDto relationship
) {}