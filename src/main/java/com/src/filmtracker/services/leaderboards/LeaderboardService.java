package com.src.filmtracker.services.leaderboards;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.src.filmtracker.models.leaderboards.LeaderboardResponse;
import com.src.filmtracker.models.leaderboards.UserRankDto;
import com.src.filmtracker.models.leaderboards.ReviewRankDto;
import com.src.filmtracker.models.leaderboards.CommentRankDto;
import com.src.filmtracker.utils.AppConstants;
import com.src.filmtracker.utils.SessionManager;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class LeaderboardService implements ILeaderboardService {

    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    @Override
    public CompletableFuture<LeaderboardResponse<UserRankDto>> getTopUsers(String period) {
        Type type = new TypeToken<LeaderboardResponse<UserRankDto>>(){}.getType();
        return executeGet(AppConstants.LEADERBOARDS_USERS_URL + "?period=" + period, type);
    }

    @Override
    public CompletableFuture<LeaderboardResponse<ReviewRankDto>> getTopReviews(String period) {
        Type type = new TypeToken<LeaderboardResponse<ReviewRankDto>>(){}.getType();
        return executeGet(AppConstants.LEADERBOARDS_REVIEWS_URL + "?period=" + period, type);
    }

    @Override
    public CompletableFuture<LeaderboardResponse<CommentRankDto>> getTopComments(String period) {
        Type type = new TypeToken<LeaderboardResponse<CommentRankDto>>(){}.getType();
        return executeGet(AppConstants.LEADERBOARDS_COMMENTS_URL + "?period=" + period, type);
    }

    private <T> CompletableFuture<T> executeGet(String url, Type responseType) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .GET();

        if (SessionManager.getInstance().isAuthenticated()) {
            builder.header("Authorization", "Bearer " + SessionManager.getInstance().getToken());
        }

        return client.sendAsync(builder.build(), HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    com.src.filmtracker.App.checkHttpResponse(response);
                    
                    if (response.statusCode() >= 400) {
                        throw new RuntimeException("Error: " + response.statusCode());
                    }
                    
                    return gson.fromJson(response.body(), responseType);
                });
    }
}