package com.src.filmtracker.utils;

public final class AppConstants {
    
    private AppConstants() {}

    public static final String API_BASE_URL = "http://localhost:3001/api/shows";
    
    public static final int HOME_CAROUSEL_LIMIT = 20;

    public static final String FXML_DASHBOARD = "/com/src/filmtracker/views/FXMLDashboard.fxml";
    public static final String FXML_SHOW_CARD = "/com/src/filmtracker/views/FXMLShowCard.fxml";
    
    public static final String MESSAGE_ERROR_API = "No se pudo conectar con el servidor.";
    public static final String MESSAGE_RATING_NA = "N/A";
    
    public static final String APP_TITLE = "FilmTracker Desktop";
    public static final double DEFAULT_ALERT_WIDTH = 400;
}