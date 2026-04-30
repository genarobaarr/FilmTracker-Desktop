package com.src.filmtracker.services;

import com.google.gson.Gson;
import com.src.filmtracker.models.ProfileResponse;
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
        String token = SessionManager.getInstance().getToken();
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConstants.USERS_PROFILE_URL))
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/json")
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() >= 400) {
                        throw new RuntimeException("Error 401 o 404 en Perfil");
                    }
                    ProfileResponse res = gson.fromJson(response.body(), ProfileResponse.class);
                    return res.data(); 
                });
    }
    
    @Override
    public CompletableFuture<UserDto> getUserById(String authId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConstants.USERS_SERVICE_URL + "/" + authId))
                .header("Accept", "application/json")
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() >= 400) 
                    {
                        return null;
                    }
                    com.google.gson.JsonObject json = com.google.gson.JsonParser.parseString(response.body()).getAsJsonObject();
                    if (json.has("data")) 
                    {
                        return gson.fromJson(json.get("data"), UserDto.class);
                    }
                    return gson.fromJson(json, UserDto.class);
                });
    }
}