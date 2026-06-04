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

    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String HEADER_ACCEPT = "Accept";
    private static final String HEADER_AUTH = "Authorization";
    private static final String TYPE_JSON = "application/json";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String ERROR_PREFIX = "Error: ";

    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    @Override
    public CompletableFuture<Void> createReport(ReportRequest request) {
        String jsonBody = gson.toJson(request);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(AppConstants.REPORTS_URL))
                .header(HEADER_CONTENT_TYPE, TYPE_JSON)
                .header(HEADER_ACCEPT, TYPE_JSON)
                .header(HEADER_AUTH, BEARER_PREFIX + SessionManager.getInstance().getToken())
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        return client.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    com.src.filmtracker.App.checkHttpResponse(response);
                    
                    if (response.statusCode() >= 400) {
                        throw new IllegalStateException(ERROR_PREFIX + response.statusCode());
                    }
                    
                    return null;
                });
    }
}