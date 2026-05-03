package com.src.filmtracker.utils;

public final class AppConstants {
    
    private AppConstants() {}

    public static final String SHOWS_SERVICE_URL = "http://localhost:3001/api/shows";
    public static final String SHOWS_BY_GENRE_URL = SHOWS_SERVICE_URL + "/by-genre/";
    public static final String USERS_SERVICE_URL = "http://localhost:3002/api/users";
    public static final String USERS_PROFILE_URL = USERS_SERVICE_URL + "/profile";
    public static final String AUTH_SERVICE_URL = "http://localhost:3003/api/auth";
    public static final String AUTH_LOGIN_URL = AUTH_SERVICE_URL + "/login";
    public static final String AUTH_REGISTER_URL = AUTH_SERVICE_URL + "/register";
    public static final String LIBRARY_SERVICE_URL = "http://localhost:3004/api";
    public static final String FAVORITES_URL = LIBRARY_SERVICE_URL + "/favorites";
    public static final String WATCHLIST_URL = LIBRARY_SERVICE_URL + "/watchlist";
    public static final String REVIEWS_SERVICE_URL = "http://localhost:3005/api";
    public static final String REVIEWS_URL = REVIEWS_SERVICE_URL + "/reviews";
    public static final String COMMENTS_URL = REVIEWS_SERVICE_URL + "/comments";

    public static final int HOME_CAROUSEL_LIMIT = 20;

    public static final String FXML_LOGIN = "/com/src/filmtracker/views/FXMLLogin.fxml";
    public static final String FXML_REGISTER = "/com/src/filmtracker/views/FXMLRegister.fxml";
    public static final String FXML_PROFILE = "/com/src/filmtracker/views/FXMLProfile.fxml";
    public static final String FXML_DASHBOARD = "/com/src/filmtracker/views/FXMLDashboard.fxml";
    public static final String FXML_SHOW_CARD = "/com/src/filmtracker/views/FXMLShowCard.fxml";
    public static final String FXML_SHOW_DETAIL = "/com/src/filmtracker/views/FXMLShowDetail.fxml";
    public static final String FXML_USER_CARD = "/com/src/filmtracker/views/FXMLUserCard.fxml";
    
    public static final String APP_TITLE = "FilmTracker Desktop";
    public static final String MESSAGE_ERROR_API = "Error de comunicación con el servidor.";
    public static final String MESSAGE_RATING_NA = "N/A";
    public static final String MESSAGE_ERROR_AUTH = "Credenciales incorrectas o error en el servidor.";
    public static final String MESSAGE_ERROR_FIELDS = "Por favor, completa todos los campos.";
    public static final String MESSAGE_ERROR_LOAD_CARD = "Error al cargar tarjeta de serie.";
    public static final String MESSAGE_ERROR_SIMILAR = "No se pudieron cargar las series similares.";
    public static final String MESSAGE_INFO_NO_SIMILAR = "No hay series similares disponibles.";
    public static final String MESSAGE_ERROR_PASSWORD_MISMATCH = "Las contraseñas no coinciden.";
    public static final String MESSAGE_ERROR_USERNAME_INVALID = "El usuario debe ser alfanumérico y sin espacios.";
    public static final String MESSAGE_ERROR_REG_FAILED = "Error al crear la cuenta. El usuario o correo ya existen.";
    public static final String MESSAGE_ERROR_REVIEWS = "No se pudieron cargar las reseñas.";
    public static final String MESSAGE_ERROR_REVIEW_ACTION = "Error al procesar tu reseña o comentario.";
    public static final String MESSAGE_ERROR_DUPLICATE_REVIEW = "Ya has publicado una reseña para esta serie.";
    public static final String MESSAGE_ERROR_DUPLICATE_LIB = "Esta serie ya se encuentra en tu lista.";
    
    public static final String COLOR_ACCENT = "#e50914";
    public static final double CARD_ANCHOR_WIDTH = 275.0;
}