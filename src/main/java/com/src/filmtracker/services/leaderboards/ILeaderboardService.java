package com.src.filmtracker.services.leaderboards;

import com.src.filmtracker.models.leaderboards.LeaderboardResponse;
import com.src.filmtracker.models.leaderboards.UserRankDto;
import com.src.filmtracker.models.leaderboards.ReviewRankDto;
import com.src.filmtracker.models.leaderboards.CommentRankDto;
import java.util.concurrent.CompletableFuture;

public interface ILeaderboardService {
    CompletableFuture<LeaderboardResponse<UserRankDto>> getTopUsers(String period);
    CompletableFuture<LeaderboardResponse<ReviewRankDto>> getTopReviews(String period);
    CompletableFuture<LeaderboardResponse<CommentRankDto>> getTopComments(String period);
}