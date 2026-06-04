package com.src.filmtracker.services.notifications;

import com.google.gson.Gson;
import com.src.filmtracker.models.notifications.NotificationResponse;
import com.src.filmtracker.models.notifications.UnreadCountResponse;
import com.src.filmtracker.utils.AppConstants;
import com.src.filmtracker.utils.SessionManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class NotificationService implements INotificationService {

    private static final String HEADER_AUTH = "Authorization";
    private static final String HEADER_ACCEPT = "Accept";
    private static final String TYPE_JSON = "application/json";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String ERROR_PREFIX = "Error: ";

    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    @Override
    public CompletableFuture<NotificationResponse> getNotifications(int page) {
        String url = AppConstants.NOTIFICATIONS_SERVICE_URL + "?page=" + page;
        HttpRequest request = crearPeticionGet(url);
        
        return ejecutarPeticion(request, NotificationResponse.class);
    }

    @Override
    public CompletableFuture<UnreadCountResponse> getUnreadCount() {
        HttpRequest request = crearPeticionGet(AppConstants.NOTIFICATIONS_UNREAD_URL);
        
        return ejecutarPeticion(request, UnreadCountResponse.class);
    }

    @Override
    public CompletableFuture<Void> markAsRead(Integer notificationId) {
        String url = AppConstants.NOTIFICATIONS_SERVICE_URL + "/" + notificationId + "/read";
        HttpRequest request = crearPeticionPut(url);
        
        return ejecutarPeticionVacia(request);
    }

    @Override
    public CompletableFuture<Void> markAllAsRead() {
        HttpRequest request = crearPeticionPut(AppConstants.NOTIFICATIONS_READ_ALL_URL);
        
        return ejecutarPeticionVacia(request);
    }

    @Override
    public CompletableFuture<Void> deleteNotification(Integer notificationId) {
        String url = AppConstants.NOTIFICATIONS_SERVICE_URL + "/" + notificationId;
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header(HEADER_AUTH, BEARER_PREFIX + SessionManager.getInstance().getToken())
                .DELETE()
                .build();
                
        return ejecutarPeticionVacia(request);
    }

    private HttpRequest crearPeticionGet(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header(HEADER_ACCEPT, TYPE_JSON)
                .header(HEADER_AUTH, BEARER_PREFIX + SessionManager.getInstance().getToken())
                .GET()
                .build();
    }

    private HttpRequest crearPeticionPut(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header(HEADER_ACCEPT, TYPE_JSON)
                .header(HEADER_AUTH, BEARER_PREFIX + SessionManager.getInstance().getToken())
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();
    }

    private <T> CompletableFuture<T> ejecutarPeticion(HttpRequest request, Class<T> claseDestino) {
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    com.src.filmtracker.App.checkHttpResponse(response);
                    
                    if (response.statusCode() >= 400) {
                        throw new IllegalStateException(ERROR_PREFIX + response.statusCode());
                    }
                    
                    return gson.fromJson(response.body(), claseDestino);
                });
    }

    private CompletableFuture<Void> ejecutarPeticionVacia(HttpRequest request) {
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    com.src.filmtracker.App.checkHttpResponse(response);
                    
                    if (response.statusCode() >= 400) {
                        throw new IllegalStateException(ERROR_PREFIX + response.statusCode());
                    }
                    
                    return null;
                });
    }
}