package com.src.filmtracker.services.admin;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.src.filmtracker.models.admin.AccountStatusDto;
import com.src.filmtracker.models.admin.AdminReportResponse;
import com.src.filmtracker.models.admin.AdminActionRequest;
import com.src.filmtracker.models.admin.AdminUserDetailDto;
import com.src.filmtracker.models.admin.AuthStatsDto;
import com.src.filmtracker.models.admin.ModerationStatsDto;
import com.src.filmtracker.models.admin.ReviewStatsDto;
import com.src.filmtracker.models.users.UserDto;
import com.src.filmtracker.utils.AppConstants;
import com.src.filmtracker.utils.SessionManager;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class AdminService implements IAdminService {

    private static final String HEADER_AUTH = "Authorization";
    private static final String HEADER_ACCEPT = "Accept";
    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String TYPE_JSON = "application/json";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String KEY_DATA = "data";
    private static final String KEY_USERS = "users";
    private static final String ERROR_PREFIX = "Error: ";
    private static final String METHOD_PATCH = "PATCH";

    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    @Override
    public CompletableFuture<List<UserDto>> searchUsers(String query) {
        String url = AppConstants.ADMIN_USERS_SEARCH_URL + "?q=" + query;
        HttpRequest request = construirPeticionGet(url);

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(response -> {
            com.src.filmtracker.App.checkHttpResponse(response);
            
            if (response.statusCode() >= 400) {
                return new ArrayList<>();
            }
            
            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
            
            if (json.has(KEY_DATA)) {
                if (json.get(KEY_DATA).isJsonArray()) {
                    return gson.fromJson(json.get(KEY_DATA), new TypeToken<List<UserDto>>(){}.getType());
                }
                
                if (json.get(KEY_DATA).isJsonObject()) {
                    JsonObject dataObj = json.get(KEY_DATA).getAsJsonObject();
                    if (dataObj.has(KEY_USERS)) {
                        return gson.fromJson(dataObj.get(KEY_USERS), new TypeToken<List<UserDto>>(){}.getType());
                    }
                }
            }
            
            return new ArrayList<>();
        });
    }

    @Override
    public CompletableFuture<AccountStatusDto> getAccountStatus(String authId) {
        String url = AppConstants.ADMIN_AUTH_USERS_URL + "/" + authId + "/status";
        return ejecutarPeticion(construirPeticionGet(url), AccountStatusDto.class);
    }

    @Override
    public CompletableFuture<Void> suspendUser(String authId, String duration, String reason) {
        String url = AppConstants.ADMIN_AUTH_USERS_URL + "/" + authId + "/suspend";
        long dias = calcularDiasSuspension(duration);
        
        ZonedDateTime fechaFutura = ZonedDateTime.now(ZoneOffset.UTC).plusDays(dias);
        String suspendedUntilStr = fechaFutura.format(DateTimeFormatter.ISO_INSTANT);
        
        Map<String, String> payload = new HashMap<>();
        payload.put("suspendedUntil", suspendedUntilStr);
        payload.put("reason", reason);
        
        String body = gson.toJson(payload);
        return ejecutarPeticionVacia(construirPeticionPatch(url, body));
    }

    @Override
    public CompletableFuture<Void> banUser(String authId, String reason) {
        String url = AppConstants.ADMIN_AUTH_USERS_URL + "/" + authId + "/ban";
        String body = gson.toJson(Map.of("reason", reason));
        return ejecutarPeticionVacia(construirPeticionPatch(url, body));
    }

    @Override
    public CompletableFuture<Void> unbanUser(String authId) {
        String url = AppConstants.ADMIN_AUTH_USERS_URL + "/" + authId + "/unban";
        return ejecutarPeticionVacia(construirPeticionPatch(url, "{}"));
    }

    @Override
    public CompletableFuture<AdminReportResponse> getAdminReports(String status, int page) {
        String url = AppConstants.ADMIN_MODERATION_REPORTS_URL + "?status=" + status + "&page=" + page;
        return ejecutarPeticion(construirPeticionGet(url), AdminReportResponse.class);
    }

    @Override
    public CompletableFuture<Void> executeReportAction(String reportId, AdminActionRequest requestObj) {
        String url = AppConstants.ADMIN_MODERATION_REPORTS_URL + "/" + reportId + "/actions";
        return ejecutarPeticionVacia(construirPeticionPost(url, gson.toJson(requestObj)));
    }

    @Override
    public CompletableFuture<Void> dismissReport(String reportId, String note) {
        String url = AppConstants.ADMIN_MODERATION_REPORTS_URL + "/" + reportId + "/dismiss";
        return ejecutarPeticionVacia(construirPeticionPost(url, gson.toJson(Map.of("note", note))));
    }
    
    @Override
    public CompletableFuture<Void> deleteReviewDirectly(String reviewId) {
        String url = AppConstants.REVIEWS_URL + "/" + reviewId;
        return ejecutarPeticionVacia(construirPeticionDelete(url));
    }
    
    @Override
    public CompletableFuture<AuthStatsDto> getAuthStats() {
        return ejecutarPeticion(construirPeticionGet(AppConstants.ADMIN_AUTH_STATS_URL), AuthStatsDto.class);
    }

    @Override
    public CompletableFuture<ReviewStatsDto> getReviewStats() {
        return ejecutarPeticion(construirPeticionGet(AppConstants.ADMIN_REVIEWS_STATS_URL), ReviewStatsDto.class);
    }

    @Override
    public CompletableFuture<ModerationStatsDto> getModerationStats() {
        return ejecutarPeticion(construirPeticionGet(AppConstants.ADMIN_MODERATION_STATS_URL), ModerationStatsDto.class);
    }
    
    @Override
    public CompletableFuture<AdminUserDetailDto> getAdminUserDetails(String authId) {
        String url = AppConstants.USERS_SERVICE_URL + "/admin/users/" + authId;
        return ejecutarPeticion(construirPeticionGet(url), AdminUserDetailDto.class);
    }

    @Override
    public CompletableFuture<Void> removeProfilePhotoDirectly(String authId) {
        String url = AppConstants.USERS_SERVICE_URL + "/admin/users/" + authId + "/profile-photo";
        return ejecutarPeticionVacia(construirPeticionDelete(url));
    }

    @Override
    public CompletableFuture<Void> removeReviewImageDirectly(String reviewId) {
        String url = AppConstants.REVIEWS_URL + "/" + reviewId + "/image";
        return ejecutarPeticionVacia(construirPeticionDelete(url));
    }

    @Override
    public CompletableFuture<Void> removeCommentImageDirectly(String commentId) {
        String url = AppConstants.COMMENTS_URL + "/" + commentId + "/image";
        return ejecutarPeticionVacia(construirPeticionDelete(url));
    }

    @Override
    public CompletableFuture<Void> deleteCommentDirectly(String commentId) {
        String url = AppConstants.COMMENTS_URL + "/" + commentId;
        return ejecutarPeticionVacia(construirPeticionDelete(url));
    }
    
    private long calcularDiasSuspension(String duration) {
        return switch (duration) {
            case "1_DAY" -> 1L;
            case "3_DAYS" -> 3L;
            case "30_DAYS" -> 30L;
            default -> 7L;
        };
    }

    private HttpRequest construirPeticionGet(String url) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header(HEADER_ACCEPT, TYPE_JSON)
                .GET();
        return agregarAuth(builder).build();
    }

    private HttpRequest construirPeticionPost(String url, String json) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header(HEADER_CONTENT_TYPE, TYPE_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(json));
        return agregarAuth(builder).build();
    }

    private HttpRequest construirPeticionPatch(String url, String json) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header(HEADER_CONTENT_TYPE, TYPE_JSON)
                .method(METHOD_PATCH, HttpRequest.BodyPublishers.ofString(json));
        return agregarAuth(builder).build();
    }

    private HttpRequest construirPeticionDelete(String url) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .DELETE();
        return agregarAuth(builder).build();
    }

    private HttpRequest.Builder agregarAuth(HttpRequest.Builder builder) {
        if (SessionManager.getInstance().isAuthenticated()) {
            builder.header(HEADER_AUTH, BEARER_PREFIX + SessionManager.getInstance().getToken());
        }
        return builder;
    }

    private <T> CompletableFuture<T> ejecutarPeticion(HttpRequest request, Class<T> claseDestino) {
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(response -> {
            com.src.filmtracker.App.checkHttpResponse(response);
            
            if (response.statusCode() >= 400) {
                throw new IllegalStateException(ERROR_PREFIX + response.statusCode());
            }
            
            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
            
            if (json.has(KEY_DATA)) {
                return gson.fromJson(json.get(KEY_DATA), claseDestino);
            }
            
            return gson.fromJson(json, claseDestino);
        });
    }

    private CompletableFuture<Void> ejecutarPeticionVacia(HttpRequest request) {
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(response -> {
            com.src.filmtracker.App.checkHttpResponse(response);
            
            if (response.statusCode() >= 400) {
                throw new IllegalStateException(ERROR_PREFIX + response.statusCode());
            }
            
            return null;
        });
    }
}