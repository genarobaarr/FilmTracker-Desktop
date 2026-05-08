package com.src.filmtracker.services.friends;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.src.filmtracker.models.friends.FriendsSummaryDto;
import com.src.filmtracker.utils.AppConstants;
import com.src.filmtracker.utils.SessionManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class FriendsService implements IFriendsService {

    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    @Override
    public CompletableFuture<FriendsSummaryDto> getUserSummary(String authId) {
        String url = AppConstants.FRIENDS_SERVICE_URL + "/user/" + authId + "/summary";
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + SessionManager.getInstance().getToken())
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() >= 400) {
                        return new FriendsSummaryDto(authId, 0);
                    }
                    
                    JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                    
                    if (json.has("data")) {
                        return gson.fromJson(json.get("data"), FriendsSummaryDto.class);
                    }
                    
                    return gson.fromJson(json, FriendsSummaryDto.class);
                });
    }
}