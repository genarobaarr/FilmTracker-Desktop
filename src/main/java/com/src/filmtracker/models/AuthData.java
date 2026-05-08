package com.src.filmtracker.models;

import com.google.gson.annotations.SerializedName;

public record AuthData(
    @SerializedName("user")
    UserDto user, 
    
    @SerializedName("token")
    String token
) {}