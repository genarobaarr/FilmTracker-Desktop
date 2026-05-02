package com.src.filmtracker.services;

import com.google.gson.Gson;
import com.src.filmtracker.models.UserDto;
import com.src.filmtracker.utils.AppConstants;
import com.src.filmtracker.utils.SessionManager;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class UserService implements IUserService {

    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    @Override
    public CompletableFuture<UserDto> getProfile() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConstants.USERS_PROFILE_URL))
                .header("Authorization", "Bearer " + SessionManager.getInstance().getToken())
                .header("Accept", "application/json")
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() >= 400) {
                        throw new RuntimeException("Error: " + response.statusCode());
                    }
                    
                    com.google.gson.JsonObject json = com.google.gson.JsonParser.parseString(response.body()).getAsJsonObject();
                    if (json.has("data")) {
                        return gson.fromJson(json.get("data"), UserDto.class);
                    }
                    
                    return gson.fromJson(json, UserDto.class);
                });
    }

    @Override
    public CompletableFuture<UserDto> getUserById(String authId) {
        String url = AppConstants.USERS_SERVICE_URL + "/auth/" + authId;
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() >= 400) {
                        return null;
                    }
                    
                    com.google.gson.JsonObject json = com.google.gson.JsonParser.parseString(response.body()).getAsJsonObject();
                    if (json.has("data")) {
                        return gson.fromJson(json.get("data"), UserDto.class);
                    }
                    
                    return gson.fromJson(json, UserDto.class);
                });
    }
}