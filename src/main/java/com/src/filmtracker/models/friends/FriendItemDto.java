package com.src.filmtracker.models.friends;

import com.google.gson.annotations.SerializedName;

public record FriendItemDto(
    @SerializedName("id") 
    Integer id,
    
    @SerializedName(value = "friendAuthId", alternate = {"friend_auth_id"}) 
    String friendAuthId
) {}