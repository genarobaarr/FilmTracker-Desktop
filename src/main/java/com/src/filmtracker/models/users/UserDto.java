package com.src.filmtracker.models.users;

import com.google.gson.annotations.SerializedName;

public record UserDto(
    String id,
    String authId,
    String name,
    String username,
    String email,
    String profileImage,
    String role,
    @SerializedName(value = "isEmailVerified", alternate = {"emailVerified"})
    Boolean isEmailVerified,
    String createdAt
) {
    public String getSafeAuthId() {
        if (authId != null) {
            if (!authId.isEmpty()) {
                return authId;
            }
        }
        
        if (id != null) {
            if (!id.isEmpty()) {
                return id;
            }
        }
        
        return "";
    }
    
    public boolean isVerified() {
        if (isEmailVerified != null) {
            return isEmailVerified;
        }
        
        return false;
    }
}