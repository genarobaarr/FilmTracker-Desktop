package com.src.filmtracker.utils;

public final class AppConstants {
    
    private AppConstants() {}

    public static final String SHOWS_SERVICE_URL = "http://localhost:3001/api/shows";
    public static final String AUTH_SERVICE_URL = "http://localhost:3002/api/auth";
    public static final String USERS_SERVICE_URL = "http://localhost:3003/api/users";

    public static final int HOME_CAROUSEL_LIMIT = 20;

    public static final String FXML_DASHBOARD = "/com/src/filmtracker/views/FXMLDashboard.fxml";
    public static final String FXML_SHOW_CARD = "/com/src/filmtracker/views/FXMLShowCard.fxml";
    
    public static final String APP_TITLE = "FilmTracker Desktop";
    public static final String MESSAGE_ERROR_API = "Error de comunicación con el servidor.";
    public static final String MESSAGE_RATING_NA = "N/A";
}