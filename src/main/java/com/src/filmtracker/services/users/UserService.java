package com.src.filmtracker.services.users;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.src.filmtracker.models.users.UserDto;
import com.src.filmtracker.models.users.UpdateProfileRequest;
import com.src.filmtracker.utils.AppConstants;
import com.src.filmtracker.utils.SessionManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;

public class UserService implements IUserService {

    private static final String HEADER_AUTH = "Authorization";
    private static final String HEADER_ACCEPT = "Accept";
    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String TYPE_JSON = "application/json";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String KEY_DATA = "data";
    private static final String ERROR_PREFIX = "Error: ";
    private static final String ERROR_UPDATE_PREFIX = "Update Error: ";
    private static final String ERROR_UPLOAD_PREFIX = "Upload Error: ";

    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    @Override
    public CompletableFuture<UserDto> getProfile() {
        HttpRequest request = buildGetRequest(AppConstants.USERS_PROFILE_URL);
        return executeAndParseUser(request, ERROR_PREFIX);
    }

    @Override
    public CompletableFuture<UserDto> getUserById(String authId) {
        String url = AppConstants.USERS_SERVICE_URL + "/id/" + authId;
        HttpRequest request = buildGetRequest(url);

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    com.src.filmtracker.App.checkHttpResponse(response);
                    
                    if (response.statusCode() >= 400) {
                        return null;
                    }
                    
                    return parseUserDto(response.body());
                });
    }

    @Override
    public CompletableFuture<UserDto> getUserByUsername(String username) {
        String url = AppConstants.USERS_SERVICE_URL + "/" + username;
        HttpRequest request = buildGetRequest(url);

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    com.src.filmtracker.App.checkHttpResponse(response);
                    
                    if (response.statusCode() >= 400) {
                        return null;
                    }
                    
                    return parseUserDto(response.body());
                });
    }

    @Override
    public CompletableFuture<UserDto> updateProfile(UpdateProfileRequest request) {
        String jsonBody = gson.toJson(request);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(AppConstants.USERS_PROFILE_URL))
                .header(HEADER_CONTENT_TYPE, TYPE_JSON)
                .header(HEADER_AUTH, BEARER_PREFIX + SessionManager.getInstance().getToken())
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        return executeAndParseUser(httpRequest, ERROR_UPDATE_PREFIX);
    }

    @Override
    public CompletableFuture<UserDto> uploadProfilePhoto(File file) {
        String boundary = "Boundary-" + System.currentTimeMillis();
        byte[] multipartBody = construirCuerpoMultipart(file, boundary);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(AppConstants.USERS_PROFILE_PHOTO_URL))
                .header(HEADER_AUTH, BEARER_PREFIX + SessionManager.getInstance().getToken())
                .header(HEADER_CONTENT_TYPE, "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(multipartBody))
                .build();

        return executeAndParseUser(httpRequest, ERROR_UPLOAD_PREFIX);
    }

    private HttpRequest buildGetRequest(String url) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header(HEADER_ACCEPT, TYPE_JSON)
                .GET();

        if (SessionManager.getInstance().isAuthenticated()) {
            builder.header(HEADER_AUTH, BEARER_PREFIX + SessionManager.getInstance().getToken());
        }

        return builder.build();
    }

    private CompletableFuture<UserDto> executeAndParseUser(HttpRequest request, String errorPrefix) {
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    com.src.filmtracker.App.checkHttpResponse(response);

                    if (response.statusCode() >= 400) {
                        throw new IllegalStateException(errorPrefix + response.statusCode());
                    }

                    return parseUserDto(response.body());
                });
    }

    private UserDto parseUserDto(String responseBody) {
        JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
        if (json.has(KEY_DATA)) {
            return gson.fromJson(json.get(KEY_DATA), UserDto.class);
        }
        return gson.fromJson(json, UserDto.class);
    }

    private byte[] construirCuerpoMultipart(File file, String boundary) {
        try {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(byteStream, StandardCharsets.UTF_8), true);

            writer.append("--").append(boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"image\"; filename=\"").append(file.getName()).append("\"\r\n");

            String mimeType = Files.probeContentType(file.toPath());
            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }

            writer.append(HEADER_CONTENT_TYPE).append(": ").append(mimeType).append("\r\n");
            writer.append("\r\n");
            writer.flush();

            Files.copy(file.toPath(), byteStream);
            byteStream.flush();

            writer.append("\r\n");
            writer.append("--").append(boundary).append("--\r\n");
            writer.flush();

            return byteStream.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Error al leer el archivo de imagen", e);
        }
    }
}