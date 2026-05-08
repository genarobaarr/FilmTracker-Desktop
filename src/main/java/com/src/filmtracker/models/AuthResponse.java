package com.src.filmtracker.models;

import com.google.gson.annotations.SerializedName;

public record AuthResponse(
    @SerializedName("message")
    String message, 
    
    @SerializedName("data")
    AuthData data
) {}