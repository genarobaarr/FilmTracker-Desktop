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

    private static final String HEADER_AUTH = "Authorization";
    private static final String HEADER_ACCEPT = "Accept";
    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String TYPE_JSON = "application/json";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String ERROR_API = "Error en la petición: ";
    private static final String KEY_DATA = "data";
    private static final String KEY_STATUS = "status";
    private static final String ROUTE_REQUESTS = "/requests/";

    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    @Override
    public CompletableFuture<FriendsSummaryDto> getUserSummary(String authId) {
        String url = AppConstants.FRIENDS_SERVICE_URL + "/user/" + authId + "/summary";
        
        return client.sendAsync(buildGetRequest(url), HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    com.src.filmtracker.App.checkHttpResponse(response);
                    
                    if (response.statusCode() >= 400) {
                        return new FriendsSummaryDto(authId, 0);
                    }
                    
                    JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                    
                    if (json.has(KEY_DATA)) {
                        return gson.fromJson(json.get(KEY_DATA), FriendsSummaryDto.class);
                    }
                    
                    return gson.fromJson(json, FriendsSummaryDto.class);
                });
    }

    @Override
    public CompletableFuture<FriendPaginationResponse> getFriends(String authId, int page) {
        String url = AppConstants.FRIENDS_SERVICE_URL + "/user/" + authId + "?page=" + page;

        return client.sendAsync(buildGetRequest(url), HttpResponse.BodyHandlers.ofString())
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

        return client.sendAsync(buildGetRequest(url), HttpResponse.BodyHandlers.ofString())
                .thenApply(res -> {
                    com.src.filmtracker.App.checkHttpResponse(res);
                    
                    if (res.statusCode() >= 400) {
                        return new FriendStatusResponse("NONE", null);
                    }
                    
                    JsonObject json = JsonParser.parseString(res.body()).getAsJsonObject();
                    String statusStr = "NONE";
                    
                    if (json.has(KEY_STATUS) && !json.get(KEY_STATUS).isJsonNull()) {
                        statusStr = json.get(KEY_STATUS).getAsString();
                    } else if (json.has(KEY_DATA) && !json.get(KEY_DATA).isJsonNull()) {
                        JsonObject dataObj = json.getAsJsonObject(KEY_DATA);
                        if (dataObj.has(KEY_STATUS) && !dataObj.get(KEY_STATUS).isJsonNull()) {
                            statusStr = dataObj.get(KEY_STATUS).getAsString();
                        }
                    }
                    
                    return new FriendStatusResponse(statusStr, null);
                });
    }

    @Override
    public CompletableFuture<Void> sendFriendRequest(SendFriendRequest request) {
        String url = AppConstants.FRIENDS_SERVICE_URL + "/requests";
        
        JsonObject bodyObj = new JsonObject();
        if (request != null && request.receiverAuthId() != null) {
            bodyObj.addProperty("receiverAuthId", request.receiverAuthId());
        }

        return client.sendAsync(buildPostRequest(url, bodyObj.toString()), HttpResponse.BodyHandlers.ofString())
                .thenApply(res -> {
                    validateResponseThrows(res);
                    return null;
                });
    }

    @Override
    public CompletableFuture<Void> removeFriend(String friendAuthId) {
        String url = AppConstants.FRIENDS_SERVICE_URL + "/" + friendAuthId;

        return client.sendAsync(buildDeleteRequest(url), HttpResponse.BodyHandlers.ofString())
                .thenApply(res -> {
                    validateResponseThrows(res);
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
        return client.sendAsync(buildGetRequest(url), HttpResponse.BodyHandlers.ofString())
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
        String url = AppConstants.FRIENDS_SERVICE_URL + ROUTE_REQUESTS + requestId + "/accept";
        return executePutRequest(url);
    }

    @Override
    public CompletableFuture<Void> rejectFriendRequest(Integer requestId) {
        String url = AppConstants.FRIENDS_SERVICE_URL + ROUTE_REQUESTS + requestId + "/reject";
        return executePutRequest(url);
    }

    private CompletableFuture<Void> executePutRequest(String url) {
        return client.sendAsync(buildPutRequest(url), HttpResponse.BodyHandlers.ofString())
                .thenApply(res -> {
                    validateResponseThrows(res);
                    return null;
                });
    }

    @Override
    public CompletableFuture<Void> cancelFriendRequest(Integer requestId) {
        String url = AppConstants.FRIENDS_SERVICE_URL + ROUTE_REQUESTS + requestId;

        return client.sendAsync(buildDeleteRequest(url), HttpResponse.BodyHandlers.ofString())
                .thenApply(res -> {
                    validateResponseThrows(res);
                    return null;
                });
    }
    
    private void appendAuthHeader(HttpRequest.Builder builder) {
        if (SessionManager.getInstance().isAuthenticated()) {
            builder.header(HEADER_AUTH, BEARER_PREFIX + SessionManager.getInstance().getToken());
        }
    }

    private HttpRequest buildGetRequest(String url) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header(HEADER_ACCEPT, TYPE_JSON)
                .GET();
                
        appendAuthHeader(builder);
        return builder.build();
    }

    private HttpRequest buildPostRequest(String url, String jsonBody) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header(HEADER_CONTENT_TYPE, TYPE_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody));
                
        appendAuthHeader(builder);
        return builder.build();
    }

    private HttpRequest buildPutRequest(String url) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .PUT(HttpRequest.BodyPublishers.ofString(""));
                
        appendAuthHeader(builder);
        return builder.build();
    }

    private HttpRequest buildDeleteRequest(String url) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .DELETE();
                
        appendAuthHeader(builder);
        return builder.build();
    }

    private void validateResponseThrows(HttpResponse<String> res) {
        com.src.filmtracker.App.checkHttpResponse(res);
        if (res.statusCode() >= 400) {
            throw new IllegalStateException(ERROR_API + res.statusCode());
        }
    }
}