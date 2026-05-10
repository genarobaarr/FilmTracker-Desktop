package com.src.filmtracker.models.friends;

public record FriendRequestDto(
    Integer id,
    String requester_auth_id,
    String receiver_auth_id,
    String status
) {
    public Integer getSafeId() {
        return id;
    }
}