package com.src.filmtracker.utils;

public final class AppConstants {
    
    private AppConstants() {
    }

    private static final ConfigManager CONFIG = ConfigManager.getInstance();

    public static final String SHOWS_SERVICE_URL = CONFIG.getProperty("api.shows.url", "http://localhost:3001/api/shows");
    public static final String SHOWS_BY_GENRE_URL = SHOWS_SERVICE_URL + "/by-genre/";
    
    public static final String USERS_SERVICE_URL = CONFIG.getProperty("api.users.url", "http://localhost:3002/api/users");
    public static final String USERS_PROFILE_URL = USERS_SERVICE_URL + "/profile";
    public static final String USERS_PROFILE_PHOTO_URL = USERS_PROFILE_URL + "/photo";
    
    public static final String AUTH_SERVICE_URL = CONFIG.getProperty("api.auth.url", "http://localhost:3003/api/auth");
    public static final String AUTH_LOGIN_URL = AUTH_SERVICE_URL + "/login";
    public static final String AUTH_REGISTER_URL = AUTH_SERVICE_URL + "/register";
    public static final String AUTH_VERIFY_EMAIL_URL = AUTH_SERVICE_URL + "/verify-email";
    public static final String AUTH_RESEND_VERIFICATION_URL = AUTH_SERVICE_URL + "/resend-verification";
    public static final String AUTH_CHANGE_PASSWORD_URL = AUTH_SERVICE_URL + "/change-password";
    public static final String AUTH_FORGOT_PASSWORD_URL = AUTH_SERVICE_URL + "/forgot-password";
    public static final String AUTH_RESET_PASSWORD_URL = AUTH_SERVICE_URL + "/reset-password";
    
    public static final String LIBRARY_SERVICE_URL = CONFIG.getProperty("api.library.url", "http://localhost:3004/api");
    public static final String FAVORITES_URL = LIBRARY_SERVICE_URL + "/favorites";
    public static final String WATCHLIST_URL = LIBRARY_SERVICE_URL + "/watchlist";
    
    public static final String REVIEWS_SERVICE_URL = CONFIG.getProperty("api.reviews.url", "http://localhost:3005/api");
    public static final String REVIEWS_URL = REVIEWS_SERVICE_URL + "/reviews";
    public static final String COMMENTS_URL = REVIEWS_SERVICE_URL + "/comments";
    public static final String LEADERBOARDS_URL = REVIEWS_SERVICE_URL + "/leaderboards";
    public static final String LEADERBOARDS_USERS_URL = LEADERBOARDS_URL + "/users";
    public static final String LEADERBOARDS_REVIEWS_URL = LEADERBOARDS_URL + "/reviews";
    public static final String LEADERBOARDS_COMMENTS_URL = LEADERBOARDS_URL + "/comments";
    
    public static final String FRIENDS_SERVICE_URL = CONFIG.getProperty("api.friends.url", "http://localhost:3006/api/friends");
    
    public static final String MODERATION_SERVICE_URL = CONFIG.getProperty("api.moderation.url", "http://localhost:3007/api/moderation");
    public static final String REPORTS_URL = MODERATION_SERVICE_URL + "/reports";
    public static final String ADMIN_USERS_SEARCH_URL = USERS_SERVICE_URL + "/admin/search";
    public static final String ADMIN_AUTH_USERS_URL = AUTH_SERVICE_URL + "/admin/users";
    public static final String ADMIN_MODERATION_REPORTS_URL = MODERATION_SERVICE_URL + "/admin/reports";
    public static final String ROUTE_ADMIN_STATS = "/admin/stats";
    public static final String ADMIN_AUTH_STATS_URL = AUTH_SERVICE_URL + ROUTE_ADMIN_STATS;
    public static final String ADMIN_REVIEWS_STATS_URL = REVIEWS_URL + ROUTE_ADMIN_STATS;
    public static final String ADMIN_MODERATION_STATS_URL = MODERATION_SERVICE_URL + ROUTE_ADMIN_STATS;
    
    public static final String NOTIFICATIONS_SERVICE_URL = CONFIG.getProperty("api.notifications.url", "http://localhost:3008/api/notifications");
    public static final String NOTIFICATIONS_UNREAD_URL = NOTIFICATIONS_SERVICE_URL + "/unread-count";
    public static final String NOTIFICATIONS_READ_ALL_URL = NOTIFICATIONS_SERVICE_URL + "/read-all";

    public static final int HOME_CAROUSEL_LIMIT = 20;

    public static final String FXML_LOGIN = "/com/src/filmtracker/views/FXMLLogin.fxml";
    public static final String FXML_REGISTER = "/com/src/filmtracker/views/FXMLRegister.fxml";
    public static final String FXML_PROFILE = "/com/src/filmtracker/views/FXMLProfile.fxml";
    public static final String FXML_DASHBOARD = "/com/src/filmtracker/views/FXMLDashboard.fxml";
    public static final String FXML_SHOW_CARD = "/com/src/filmtracker/views/FXMLShowCard.fxml";
    public static final String FXML_SHOW_DETAIL = "/com/src/filmtracker/views/FXMLShowDetail.fxml";
    public static final String FXML_USER_CARD = "/com/src/filmtracker/views/FXMLUserCard.fxml";
    public static final String FXML_VERIFY_EMAIL = "/com/src/filmtracker/views/FXMLVerifyEmail.fxml";
    public static final String FXML_CHANGE_PASSWORD = "/com/src/filmtracker/views/FXMLChangePassword.fxml";
    public static final String FXML_FORGOT_PASSWORD = "/com/src/filmtracker/views/FXMLForgotPassword.fxml";
    public static final String FXML_RESET_PASSWORD = "/com/src/filmtracker/views/FXMLResetPassword.fxml";
    public static final String FXML_FRIENDS_MANAGER = "/com/src/filmtracker/views/FXMLFriendsManager.fxml";
    public static final String FXML_REPORT = "/com/src/filmtracker/views/FXMLReport.fxml";
    public static final String FXML_LEADERBOARDS = "/com/src/filmtracker/views/FXMLLeaderboards.fxml";
    public static final String FXML_NOTIFICATIONS = "/com/src/filmtracker/views/FXMLNotifications.fxml";
    public static final String FXML_ADMIN_PANEL = "/com/src/filmtracker/views/FXMLAdminPanel.fxml";
    public static final String FXML_CUSTOM_ALERT = "/com/src/filmtracker/views/FXMLCustomAlert.fxml";
    
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
    public static final String MESSAGE_ERROR_UNVERIFIED = "Debes verificar tu correo electrónico para poder publicar.";
    public static final String MESSAGE_ERROR_CODE_EMPTY = "Por favor, ingresa el código de verificación de 6 dígitos.";
    public static final String MESSAGE_SUCCESS_VERIFIED = "¡Correo verificado con éxito!";
    public static final String MESSAGE_SUCCESS_RESEND = "Se ha reenviado el código a tu correo.";
    public static final String MESSAGE_SUCCESS_PASSWORD = "La contraseña ha sido actualizada correctamente.";
    public static final String MESSAGE_ERROR_PASSWORD_CURRENT = "La contraseña actual es incorrecta.";
    public static final String MESSAGE_SUCCESS_FORGOT = "Si el correo existe, se enviaron las instrucciones.";
    public static final String MESSAGE_SUCCESS_RESET = "Contraseña actualizada con éxito.";
    public static final String MESSAGE_SUCCESS_FRIEND_ADD = "Solicitud de amistad enviada.";
    public static final String MESSAGE_SUCCESS_FRIEND_REMOVE = "Amigo eliminado correctamente.";
    public static final String MESSAGE_ERROR_FRIEND_ACTION = "Error al procesar la acción de amistad.";
    public static final String MESSAGE_SUCCESS_ACCEPTED = "Solicitud aceptada correctamente.";
    public static final String MESSAGE_SUCCESS_REJECTED = "Solicitud rechazada correctamente.";
    public static final String MESSAGE_SUCCESS_CANCELED = "Solicitud cancelada correctamente.";
    public static final String MESSAGE_SUCCESS_PHOTO = "Foto de perfil actualizada correctamente.";
    public static final String MESSAGE_ERROR_PHOTO = "Error al subir la foto de perfil.";
    public static final String MESSAGE_SUCCESS_REVIEW_UPDATE = "Reseña actualizada correctamente.";
    public static final String MESSAGE_SUCCESS_REPORT = "Reporte enviado correctamente.";
    public static final String MESSAGE_ERROR_REPORT = "Error al enviar el reporte.";
    public static final String MESSAGE_ERROR_REPORT_DUPLICATE = "Ya has enviado un reporte para este elemento y se encuentra en revisión.";
    public static final String MESSAGE_SUCCESS_READ_ALL = "Todas las notificaciones han sido marcadas como leídas.";
    public static final String MESSAGE_ERROR_NOTIFICATIONS = "Error al procesar las notificaciones.";
    public static final String MESSAGE_SUCCESS_ADMIN_ACTION = "Acción administrativa ejecutada correctamente.";
    public static final String MESSAGE_TITLE_SUSPEND = "Suspender Usuario";
    public static final String MESSAGE_HEADER_SUSPEND = "Selecciona la duración de la suspensión";
    public static final String MESSAGE_TITLE_BAN = "Banear Usuario";
    public static final String MESSAGE_CONTENT_BAN = "¿Estás seguro de que deseas banear permanentemente a este usuario?";
    public static final String MESSAGE_TITLE_UNBAN = "Desbanear Usuario";
    public static final String MESSAGE_CONTENT_UNBAN = "¿Deseas reactivar la cuenta de este usuario?";
    public static final String MESSAGE_ERROR_SESSION_EXPIRED = "Tu sesión ha expirado por seguridad. Por favor, inicia sesión nuevamente.";
    public static final String MESSAGE_ERROR_SERVER_DOWN_LOGIN = "No hay conexión con el servidor. Se ha cerrado la sesión por seguridad.";
    public static final String MESSAGE_ERROR_SERVER_DOWN_HOME = "Error de conexión. Se ha regresado al panel principal por seguridad.";
    public static final String MESSAGE_ERROR_EMPTY_FIELDS = "Todos los campos obligatorios deben estar llenos.";
    public static final String MESSAGE_ERROR_INVALID_EMAIL = "El formato del correo electrónico no es válido.";
    public static final String MESSAGE_ERROR_INVALID_USERNAME = "El usuario debe tener entre 3 y 20 caracteres alfanuméricos.";
    public static final String MESSAGE_ERROR_INVALID_PASSWORD = "La contraseña debe tener al menos 8 caracteres, una mayúscula y un número.";
    public static final String MESSAGE_ERROR_MAX_LENGTH = "El texto excede la longitud máxima permitida.";
    public static final String MESSAGE_ERROR_SELECT_RATING = "Debes seleccionar una calificación válida.";
    public static final String MESSAGE_ERROR_SELECT_OPTION = "Debes seleccionar una opción de la lista.";
    
    public static final String COLOR_ACCENT = "#e50914";
    public static final double CARD_ANCHOR_WIDTH = 275.0;
}