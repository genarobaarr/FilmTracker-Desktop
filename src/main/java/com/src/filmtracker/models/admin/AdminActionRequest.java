package com.src.filmtracker.models.admin;

public record AdminActionRequest(
    String actionType,
    String note,
    String duration
) {
}