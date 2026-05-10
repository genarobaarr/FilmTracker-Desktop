package com.src.filmtracker.services.friends;

import com.src.filmtracker.models.friends.*;
import java.util.concurrent.CompletableFuture;

public interface IFriendsService {
    CompletableFuture<FriendsSummaryDto> getUserSummary(String authId);
    CompletableFuture<FriendPaginationResponse> getFriends(int page);
    CompletableFuture<FriendStatusResponse> getRelationshipStatus(String otherAuthId);
    CompletableFuture<Void> sendFriendRequest(SendFriendRequest request);
    CompletableFuture<Void> removeFriend(String friendAuthId);
}