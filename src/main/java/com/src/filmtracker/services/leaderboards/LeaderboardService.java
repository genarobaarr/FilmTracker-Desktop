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

    private static final String HEADER_ACCEPT = "Accept";
    private static final String HEADER_AUTH = "Authorization";
    private static final String TYPE_JSON = "application/json";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String ERROR_PREFIX = "Error: ";
    private static final String QUERY_PERIOD = "?period=";

    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    @Override
    public CompletableFuture<LeaderboardResponse<UserRankDto>> getTopUsers(String period) {
        Type type = new TypeToken<LeaderboardResponse<UserRankDto>>(){}.getType();
        return executeGet(AppConstants.LEADERBOARDS_USERS_URL + QUERY_PERIOD + period, type);
    }

    @Override
    public CompletableFuture<LeaderboardResponse<ReviewRankDto>> getTopReviews(String period) {
        Type type = new TypeToken<LeaderboardResponse<ReviewRankDto>>(){}.getType();
        return executeGet(AppConstants.LEADERBOARDS_REVIEWS_URL + QUERY_PERIOD + period, type);
    }

    @Override
    public CompletableFuture<LeaderboardResponse<CommentRankDto>> getTopComments(String period) {
        Type type = new TypeToken<LeaderboardResponse<CommentRankDto>>(){}.getType();
        return executeGet(AppConstants.LEADERBOARDS_COMMENTS_URL + QUERY_PERIOD + period, type);
    }

    private <T> CompletableFuture<T> executeGet(String url, Type responseType) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header(HEADER_ACCEPT, TYPE_JSON)
                .GET();

        if (SessionManager.getInstance().isAuthenticated()) {
            builder.header(HEADER_AUTH, BEARER_PREFIX + SessionManager.getInstance().getToken());
        }

        return client.sendAsync(builder.build(), HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    com.src.filmtracker.App.checkHttpResponse(response);
                    
                    if (response.statusCode() >= 400) {
                        throw new IllegalStateException(ERROR_PREFIX + response.statusCode());
                    }
                    
                    return gson.fromJson(response.body(), responseType);
                });
    }
}