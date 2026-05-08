package com.src.filmtracker.models.library;

import com.google.gson.annotations.SerializedName;

public record LibraryItemDto(
    @SerializedName(value = "id", alternate = {"_id"}) 
    Integer id,
    
    @SerializedName(value = "authId", alternate = {"auth_id"}) 
    String authId,
    
    @SerializedName(value = "tvmazeId", alternate = {"tvmaze_id"}) 
    Integer tvmazeId,
    
    @SerializedName(value = "createdAt", alternate = {"created_at"}) 
    String createdAt
) {}