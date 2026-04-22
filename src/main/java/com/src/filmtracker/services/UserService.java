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
    private final HttpClient client;
    private final Gson gson;

    public UserService() {
        this.client = HttpClient.newHttpClient();
        this.gson = new Gson();
    }

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
                        throw new RuntimeException("Error al obtener perfil: " + response.statusCode());
                    }
                    return gson.fromJson(response.body(), UserDto.class);
                });
    }
}