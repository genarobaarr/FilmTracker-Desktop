package com.src.filmtracker.models.friends;

import com.google.gson.annotations.SerializedName;

public record FriendRequestItemDto(
    @SerializedName("id") 
    Integer id,
    
    @SerializedName(value = "requesterAuthId", alternate = {"requester_auth_id"}) 
    String requesterAuthId,
    
    @SerializedName(value = "receiverAuthId", alternate = {"receiver_auth_id"}) 
    String receiverAuthId,
    
    @SerializedName("status") 
    String status,
    
    @SerializedName(value = "createdAt", alternate = {"created_at"}) 
    String createdAt
) {
    public Integer getSafeId() {
        return id;
    }
    
    public String getSafeRequester() {
        return requesterAuthId;
    }
    
    public String getSafeReceiver() {
        return receiverAuthId;
    }
}