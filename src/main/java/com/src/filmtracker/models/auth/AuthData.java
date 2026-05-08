package com.src.filmtracker.models.auth;

import com.google.gson.annotations.SerializedName;
import com.src.filmtracker.models.users.UserDto;

public record AuthData(
    @SerializedName("user")
    UserDto user, 
    
    @SerializedName("token")
    String token
) {}