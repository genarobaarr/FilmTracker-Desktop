package com.src.filmtracker.services.admin;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.src.filmtracker.models.admin.AccountStatusDto;
import com.src.filmtracker.models.admin.AdminReportResponse;
import com.src.filmtracker.models.admin.AdminActionRequest;
import com.src.filmtracker.models.users.UserDto;
import com.src.filmtracker.utils.AppConstants;
import com.src.filmtracker.utils.SessionManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class AdminService implements IAdminService {

    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    @Override
    public CompletableFuture<List<UserDto>> searchUsers(String query) {
        String url = AppConstants.ADMIN_USERS_SEARCH_URL + "?q=" + query;
        HttpRequest request = construirPeticionGet(url);

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(response -> {
            if (response.statusCode() >= 400) {
                return new ArrayList<>();
            }
            
            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
            
            if (json.has("data")) {
                if (json.get("data").isJsonArray()) {
                    return gson.fromJson(json.get("data"), new TypeToken<List<UserDto>>(){}.getType());
                }
                
                if (json.get("data").isJsonObject()) {
                    JsonObject dataObj = json.get("data").getAsJsonObject();
                    if (dataObj.has("users")) {
                        return gson.fromJson(dataObj.get("users"), new TypeToken<List<UserDto>>(){}.getType());
                    }
                }
            }
            
            return new ArrayList<>();
        });
    }

    @Override
    public CompletableFuture<AccountStatusDto> getAccountStatus(String authId) {
        String url = AppConstants.ADMIN_AUTH_USERS_URL + "/" + authId + "/status";
        HttpRequest request = construirPeticionGet(url);

        return ejecutarPeticion(request, AccountStatusDto.class);
    }

    @Override
    public CompletableFuture<Void> suspendUser(String authId, String duration) {
        String url = AppConstants.ADMIN_AUTH_USERS_URL + "/" + authId + "/suspend";
        String body = gson.toJson(Map.of("duration", duration));
        HttpRequest request = construirPeticionPatch(url, body);

        return ejecutarPeticionVacia(request);
    }

    @Override
    public CompletableFuture<Void> banUser(String authId, String reason) {
        String url = AppConstants.ADMIN_AUTH_USERS_URL + "/" + authId + "/ban";
        String body = gson.toJson(Map.of("reason", reason));
        HttpRequest request = construirPeticionPatch(url, body);

        return ejecutarPeticionVacia(request);
    }

    @Override
    public CompletableFuture<Void> unbanUser(String authId) {
        String url = AppConstants.ADMIN_AUTH_USERS_URL + "/" + authId + "/unban";
        HttpRequest request = construirPeticionPatch(url, "{}");

        return ejecutarPeticionVacia(request);
    }

    @Override
    public CompletableFuture<AdminReportResponse> getAdminReports(String status, int page) {
        String url = AppConstants.ADMIN_MODERATION_REPORTS_URL + "?status=" + status + "&page=" + page;
        HttpRequest request = construirPeticionGet(url);

        return ejecutarPeticion(request, AdminReportResponse.class);
    }

    @Override
    public CompletableFuture<Void> executeReportAction(String reportId, AdminActionRequest requestObj) {
        String url = AppConstants.ADMIN_MODERATION_REPORTS_URL + "/" + reportId + "/actions";
        String body = gson.toJson(requestObj);
        HttpRequest request = construirPeticionPost(url, body);

        return ejecutarPeticionVacia(request);
    }

    @Override
    public CompletableFuture<Void> dismissReport(String reportId, String note) {
        String url = AppConstants.ADMIN_MODERATION_REPORTS_URL + "/" + reportId + "/dismiss";
        String body = gson.toJson(Map.of("note", note));
        HttpRequest request = construirPeticionPost(url, body);

        return ejecutarPeticionVacia(request);
    }

    private HttpRequest construirPeticionGet(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + SessionManager.getInstance().getToken())
                .GET()
                .build();
    }

    private HttpRequest construirPeticionPost(String url, String json) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + SessionManager.getInstance().getToken())
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
    }

    private HttpRequest construirPeticionPatch(String url, String json) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + SessionManager.getInstance().getToken())
                .method("PATCH", HttpRequest.BodyPublishers.ofString(json))
                .build();
    }

    private <T> CompletableFuture<T> ejecutarPeticion(HttpRequest request, Class<T> claseDestino) {
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(response -> {
            if (response.statusCode() >= 400) {
                throw new RuntimeException("Error: " + response.statusCode());
            }
            
            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
            
            if (json.has("data")) {
                return gson.fromJson(json.get("data"), claseDestino);
            }
            
            return gson.fromJson(json, claseDestino);
        });
    }

    private CompletableFuture<Void> ejecutarPeticionVacia(HttpRequest request) {
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(response -> {
            if (response.statusCode() >= 400) {
                throw new RuntimeException("Error: " + response.statusCode());
            }
            
            return null;
        });
    }
}