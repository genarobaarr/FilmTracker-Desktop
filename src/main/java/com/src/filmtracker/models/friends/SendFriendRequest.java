package com.src.filmtracker.models.friends;

import com.google.gson.annotations.SerializedName;

public record SendFriendRequest(
    @SerializedName(value = "receiverAuthId", alternate = {"receiver_auth_id"})
    String receiverAuthId
) {}