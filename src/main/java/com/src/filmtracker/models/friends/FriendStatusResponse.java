package com.src.filmtracker.models.friends;

public record FriendStatusResponse(
    String status,
    FriendRequestDto relationship
) {}