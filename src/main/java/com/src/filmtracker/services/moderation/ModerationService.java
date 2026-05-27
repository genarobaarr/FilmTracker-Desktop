package com.src.filmtracker.services.moderation;

import com.google.gson.Gson;
import com.src.filmtracker.models.moderation.ReportRequest;
import com.src.filmtracker.utils.AppConstants;
import com.src.filmtracker.utils.SessionManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class ModerationService implements IModerationService {

    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    @Override
    public CompletableFuture<Void> createReport(ReportRequest request) {
        String jsonBody = gson.toJson(request);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(AppConstants.REPORTS_URL))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + SessionManager.getInstance().getToken())
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        return client.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    com.src.filmtracker.App.checkHttpResponse(response);
                    
                    if (response.statusCode() >= 400) {
                        throw new RuntimeException("Error: " + response.statusCode());
                    }
                    
                    return null;
                });
    }
}