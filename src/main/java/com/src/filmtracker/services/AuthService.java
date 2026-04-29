package com.src.filmtracker.services;

import com.google.gson.Gson;
import com.src.filmtracker.models.AuthResponse;
import com.src.filmtracker.models.LoginRequest;
import com.src.filmtracker.models.RegisterRequest;
import com.src.filmtracker.models.RegisterResponse;
import com.src.filmtracker.utils.AppConstants;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class AuthService implements IAuthService {
    private final HttpClient client;
    private final Gson gson;

    public AuthService() {
        this.client = HttpClient.newHttpClient();
        this.gson = new Gson();
    }

    @Override
    public CompletableFuture<AuthResponse> login(LoginRequest request) {
        return executePost(AppConstants.AUTH_LOGIN_URL, request, AuthResponse.class);
    }

    @Override
    public CompletableFuture<RegisterResponse> register(RegisterRequest request) {
        return executePost(AppConstants.AUTH_REGISTER_URL, request, RegisterResponse.class);
    }

    private <T> CompletableFuture<T> executePost(String url, Object bodyData, Class<T> responseClass) {
        String jsonBody = gson.toJson(bodyData);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(response -> {
                if (response.statusCode() >= 400) {
                    throw new RuntimeException("Auth error: " + response.statusCode());
                }
                return gson.fromJson(response.body(), responseClass);
            });
    }
}