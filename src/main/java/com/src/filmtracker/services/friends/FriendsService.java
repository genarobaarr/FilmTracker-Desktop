package com.src.filmtracker.services.friends;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.src.filmtracker.models.friends.*;
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
                    com.src.filmtracker.App.checkHttpResponse(response);
                    
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

    @Override
    public CompletableFuture<FriendPaginationResponse> getFriends(String authId, int page) {
        String url = AppConstants.FRIENDS_SERVICE_URL + "/user/" + authId + "?page=" + page;

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + SessionManager.getInstance().getToken())
                .GET()
                .build();

        return client.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                .thenApply(res -> {
                    com.src.filmtracker.App.checkHttpResponse(res);
                    
                    if (res.statusCode() >= 400) {
                        return null;
                    }
                    
                    return gson.fromJson(res.body(), FriendPaginationResponse.class);
                });
    }

    @Override
    public CompletableFuture<FriendStatusResponse> getRelationshipStatus(String otherAuthId) {
        String url = AppConstants.FRIENDS_SERVICE_URL + "/" + otherAuthId + "/status";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + SessionManager.getInstance().getToken())
                .GET()
                .build();

        return client.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                .thenApply(res -> {
                    com.src.filmtracker.App.checkHttpResponse(res);
                    
                    if (res.statusCode() >= 400) {
                        return new FriendStatusResponse("NONE", null);
                    }
                    
                    JsonObject json = JsonParser.parseString(res.body()).getAsJsonObject();
                    String statusStr = "NONE";
                    
                    if (json.has("status")) {
                        if (!json.get("status").isJsonNull()) {
                            statusStr = json.get("status").getAsString();
                        }
                    } else if (json.has("data")) {
                        if (!json.get("data").isJsonNull()) {
                            JsonObject dataObj = json.getAsJsonObject("data");
                            if (dataObj.has("status")) {
                                if (!dataObj.get("status").isJsonNull()) {
                                    statusStr = dataObj.get("status").getAsString();
                                }
                            }
                        }
                    }
                    
                    return new FriendStatusResponse(statusStr, null);
                });
    }

    @Override
    public CompletableFuture<Void> sendFriendRequest(SendFriendRequest request) {
        String url = AppConstants.FRIENDS_SERVICE_URL + "/requests";
        
        JsonObject bodyObj = new JsonObject();
        
        if (request != null) {
            if (request.receiverAuthId() != null) {
                bodyObj.addProperty("receiverAuthId", request.receiverAuthId());
            }
        }

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + SessionManager.getInstance().getToken())
                .POST(HttpRequest.BodyPublishers.ofString(bodyObj.toString()))
                .build();

        return client.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                .thenApply(res -> {
                    com.src.filmtracker.App.checkHttpResponse(res);
                    
                    if (res.statusCode() >= 400) {
                        throw new RuntimeException("Error: " + res.statusCode());
                    }
                    
                    return null;
                });
    }

    @Override
    public CompletableFuture<Void> removeFriend(String friendAuthId) {
        String url = AppConstants.FRIENDS_SERVICE_URL + "/" + friendAuthId;

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + SessionManager.getInstance().getToken())
                .DELETE()
                .build();

        return client.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                .thenApply(res -> {
                    com.src.filmtracker.App.checkHttpResponse(res);
                    
                    if (res.statusCode() >= 400) {
                        throw new RuntimeException("Error: " + res.statusCode());
                    }
                    
                    return null;
                });
    }

    @Override
    public CompletableFuture<FriendRequestPaginationResponse> getIncomingRequests(int page) {
        String url = AppConstants.FRIENDS_SERVICE_URL + "/requests/incoming?page=" + page;
        return executeGetRequestPagination(url);
    }

    @Override
    public CompletableFuture<FriendRequestPaginationResponse> getOutgoingRequests(int page) {
        String url = AppConstants.FRIENDS_SERVICE_URL + "/requests/outgoing?page=" + page;
        return executeGetRequestPagination(url);
    }

    private CompletableFuture<FriendRequestPaginationResponse> executeGetRequestPagination(String url) {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + SessionManager.getInstance().getToken())
                .GET()
                .build();

        return client.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                .thenApply(res -> {
                    com.src.filmtracker.App.checkHttpResponse(res);
                    
                    if (res.statusCode() >= 400) {
                        return null;
                    }
                    
                    return gson.fromJson(res.body(), FriendRequestPaginationResponse.class);
                });
    }

    @Override
    public CompletableFuture<Void> acceptFriendRequest(Integer requestId) {
        String url = AppConstants.FRIENDS_SERVICE_URL + "/requests/" + requestId + "/accept";
        return executePutRequest(url);
    }

    @Override
    public CompletableFuture<Void> rejectFriendRequest(Integer requestId) {
        String url = AppConstants.FRIENDS_SERVICE_URL + "/requests/" + requestId + "/reject";
        return executePutRequest(url);
    }

    private CompletableFuture<Void> executePutRequest(String url) {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + SessionManager.getInstance().getToken())
                .PUT(HttpRequest.BodyPublishers.ofString(""))
                .build();

        return client.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                .thenApply(res -> {
                    com.src.filmtracker.App.checkHttpResponse(res);
                    
                    if (res.statusCode() >= 400) {
                        throw new RuntimeException("Error: " + res.statusCode());
                    }
                    
                    return null;
                });
    }

    @Override
    public CompletableFuture<Void> cancelFriendRequest(Integer requestId) {
        String url = AppConstants.FRIENDS_SERVICE_URL + "/requests/" + requestId;
        
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + SessionManager.getInstance().getToken())
                .DELETE()
                .build();

        return client.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                .thenApply(res -> {
                    com.src.filmtracker.App.checkHttpResponse(res);
                    
                    if (res.statusCode() >= 400) {
                        throw new RuntimeException("Error: " + res.statusCode());
                    }
                    
                    return null;
                });
    }
}