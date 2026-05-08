package com.src.filmtracker.models.friends;

public record FriendsSummaryDto(
    String authId,
    Integer friendsCount
) {}