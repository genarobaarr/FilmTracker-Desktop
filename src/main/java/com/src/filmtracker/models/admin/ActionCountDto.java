package com.src.filmtracker.models.admin;

import com.google.gson.annotations.SerializedName;

public record ActionCountDto(
    @SerializedName("action_type") String actionType,
    Integer count
) {
}