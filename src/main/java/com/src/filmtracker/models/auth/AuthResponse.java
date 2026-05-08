package com.src.filmtracker.models.auth;

import com.src.filmtracker.models.auth.AuthData;
import com.google.gson.annotations.SerializedName;

public record AuthResponse(
    @SerializedName("message")
    String message, 
    
    @SerializedName("data")
    AuthData data
) {}