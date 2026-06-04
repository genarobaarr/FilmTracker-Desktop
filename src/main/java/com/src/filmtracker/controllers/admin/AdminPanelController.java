package com.src.filmtracker.controllers.admin;

import com.src.filmtracker.App;
import com.src.filmtracker.models.admin.*;
import com.src.filmtracker.models.users.UserDto;
import com.src.filmtracker.services.admin.AdminService;
import com.src.filmtracker.services.admin.IAdminService;
import com.src.filmtracker.services.shows.IShowService;
import com.src.filmtracker.services.shows.ShowService;
import com.src.filmtracker.services.users.IUserService;
import com.src.filmtracker.services.users.UserService;
import com.src.filmtracker.utils.AppConstants;
import com.src.filmtracker.utils.CustomAlertHelper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class AdminPanelController {

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_DISMISSED = "DISMISSED";
    private static final String STATUS_ACTION_TAKEN = "ACTION_TAKEN";
    private static final String STATUS_ALL = "ALL";
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_SUSPENDED = "SUSPENDED";
    private static final String STATUS_BANNED = "BANNED";

    private static final String COLOR_GREEN = "#4caf50";
    private static final String COLOR_ORANGE = "#ff9800";
    private static final String COLOR_RED = "#e50914";
    private static final String COLOR_DARK = "#2a2a2a";
    private static final String COLOR_BLUE = "#2196f3";
    private static final String COLOR_PURPLE = "#9c27b0";
    private static final String COLOR_PINK = "#e91e63";

    private static final String TEXT_DESCONOCIDO = "Desconocido";
    private static final String TEXT_AT_DESCONOCIDO = "@Desconocido";
    private static final String TEXT_EL_USUARIO = "El usuario ";
    private static final String TEXT_POR = " por ";
    private static final String TEXT_JUSTIFICANDO = " justificando que \"";

    private static final String TARGET_USER = "USER";
    private static final String TARGET_REVIEW = "REVIEW";
    private static final String TARGET_COMMENT = "COMMENT";

    private static final String ACTION_DISMISS = "DISMISS_REPORT";
    private static final String ACTION_SUSPEND = "SUSPEND_USER";
    private static final String ACTION_BAN = "BAN_USER";
    private static final String ACTION_DEL_REVIEW = "DELETE_REVIEW";
    private static final String ACTION_DEL_COMMENT = "DELETE_COMMENT";
    private static final String ACTION_RM_PROF_IMG = "REMOVE_PROFILE_IMAGE";
    private static final String ACTION_RM_REV_IMG = "REMOVE_REVIEW_IMAGE";
    private static final String ACTION_RM_COM_IMG = "REMOVE_COMMENT_IMAGE";

    private static final String STYLE_BOLD = "-fx-font-weight: bold;";
    
    @FXML private FlowPane statsCardsContainer;
    @FXML private TextField searchUserField;
    @FXML private VBox usersListContainer;
    @FXML private VBox userDetailPane;
    
    @FXML private ComboBox<String> reportStatusFilter;
    @FXML private VBox reportsListContainer;
    @FXML private VBox reportDetailPane;
    
    @FXML private Tab tabDashboard;
    @FXML private Tab tabReports;

    private final IAdminService adminService = new AdminService();
    private final IUserService userService = new UserService();
    private final IShowService showService = new ShowService();
    
    private final Map<String, String> statusMapUiToBackend = new LinkedHashMap<>();
    private final Map<String, String> durationMapUiToBackend = new LinkedHashMap<>();
    private final Map<String, String> actionMapBackendToUi = new LinkedHashMap<>();
    private final Map<String, String> targetTypeMap = new LinkedHashMap<>();

    @FXML
    public void initialize() {
        inicializarDiccionarios();
        prepararFiltrosReportes();
        configurarBuscadorUsuarios();

        tabDashboard.setOnSelectionChanged(e -> {
            if (tabDashboard.isSelected()) {
                loadStats();
            }
        });

        tabReports.setOnSelectionChanged(e -> {
            if (tabReports.isSelected()) {
                loadReports();
            }
        });

        loadStats();
    }

    private void inicializarDiccionarios() {
        statusMapUiToBackend.put("Pendiente", STATUS_PENDING);
        statusMapUiToBackend.put("Descartado", STATUS_DISMISSED);
        statusMapUiToBackend.put("Acción Tomada", STATUS_ACTION_TAKEN);
        statusMapUiToBackend.put("Todos", STATUS_ALL);

        durationMapUiToBackend.put("1 día", "1_DAY");
        durationMapUiToBackend.put("3 días", "3_DAYS");
        durationMapUiToBackend.put("7 días", "7_DAYS");
        durationMapUiToBackend.put("30 días", "30_DAYS");

        targetTypeMap.put(TARGET_USER, "Usuario");
        targetTypeMap.put(TARGET_REVIEW, "Reseña");
        targetTypeMap.put(TARGET_COMMENT, "Comentario");

        actionMapBackendToUi.put(ACTION_DISMISS, "Descartar Reporte");
        actionMapBackendToUi.put(ACTION_SUSPEND, "Suspender Usuario");
        actionMapBackendToUi.put(ACTION_BAN, "Banear Usuario");
        actionMapBackendToUi.put(ACTION_DEL_REVIEW, "Eliminar Reseña");
        actionMapBackendToUi.put(ACTION_DEL_COMMENT, "Eliminar Comentario");
        actionMapBackendToUi.put(ACTION_RM_PROF_IMG, "Eliminar Foto Perfil");
        actionMapBackendToUi.put(ACTION_RM_REV_IMG, "Eliminar Foto Reseña");
        actionMapBackendToUi.put(ACTION_RM_COM_IMG, "Eliminar Foto Comentario");
    }

    @FXML
    private void loadStats() {
        if (statsCardsContainer == null) {
            return;
        }
        
        statsCardsContainer.getChildren().clear();
        
        adminService.getAuthStats()
            .thenAccept(stats -> Platform.runLater(() -> renderAuthStats(stats)))
            .exceptionally(e -> { 
                App.procesarErrorCritico(e); 
                return null; 
            });
        
        adminService.getReviewStats()
            .thenAccept(stats -> Platform.runLater(() -> renderReviewStats(stats)))
            .exceptionally(e -> { 
                App.procesarErrorCritico(e); 
                return null; 
            });
        
        adminService.getModerationStats()
            .thenAccept(stats -> Platform.runLater(() -> renderModerationStats(stats)))
            .exceptionally(e -> { 
                App.procesarErrorCritico(e); 
                return null; 
            });
    }
    
    @FXML private void handleBack() { 
        App.setRoot(AppConstants.FXML_DASHBOARD); 
    }
    
    @FXML private void handleMinimize() { 
        ((Stage) searchUserField.getScene().getWindow()).setIconified(true); 
    }
    
    @FXML private void handleClose() { 
        Platform.exit(); 
        System.exit(0); 
    }

    @FXML
    private void handleSearchUsers() {
        String query = searchUserField.getText().trim();
        
        if (query.isEmpty()) {
            return;
        }
        
        usersListContainer.getChildren().clear();
        userDetailPane.getChildren().clear();
        
        adminService.searchUsers(query).thenAccept(users -> 
            Platform.runLater(() -> {
                if (users != null && !users.isEmpty()) {
                    for (UserDto user : users) {
                        usersListContainer.getChildren().add(construirFilaUsuario(user));
                    }
                }
            })
        ).exceptionally(e -> {
            App.procesarErrorCritico(e);
            return null;
        });
    }

    @FXML
    private void loadReports() {
        if (reportsListContainer == null) {
            return;
        }
        
        reportsListContainer.getChildren().clear();
        reportDetailPane.getChildren().clear();
        
        peticionReportes(1);
    }

    private void renderAuthStats(AuthStatsDto stats) {
        if (stats != null && stats.byStatus() != null) {
            statsCardsContainer.getChildren().add(crearTarjetaMetrica("Usuarios Registrados", String.valueOf(stats.totalUsers()), COLOR_GREEN));

            Integer activos = stats.byStatus().getOrDefault(STATUS_ACTIVE, 0);
            Integer suspendidos = stats.byStatus().getOrDefault(STATUS_SUSPENDED, 0);
            Integer baneados = stats.byStatus().getOrDefault(STATUS_BANNED, 0);
            
            statsCardsContainer.getChildren().add(crearTarjetaMetrica("Usuarios Activos", String.valueOf(activos), COLOR_GREEN));
            statsCardsContainer.getChildren().add(crearTarjetaMetrica("Cuentas Suspendidas", String.valueOf(suspendidos), COLOR_ORANGE));
            statsCardsContainer.getChildren().add(crearTarjetaMetrica("Cuentas Baneadas", String.valueOf(baneados), COLOR_RED));
        }
    }

    private void renderReviewStats(ReviewStatsDto stats) {
        if (stats != null && stats.totals() != null) {
            Number reviews = stats.totals().getOrDefault("reviews", 0);
            Number comments = stats.totals().getOrDefault("comments", 0);
            Number likes = stats.totals().getOrDefault("likes", 0);
            
            statsCardsContainer.getChildren().add(crearTarjetaMetrica("Reseñas Globales", String.valueOf(reviews), COLOR_BLUE));
            statsCardsContainer.getChildren().add(crearTarjetaMetrica("Comentarios Globales", String.valueOf(comments), COLOR_BLUE));
            statsCardsContainer.getChildren().add(crearTarjetaMetrica("Likes Repartidos", String.valueOf(likes), COLOR_PINK));
        }
    }

    private void renderModerationStats(ModerationStatsDto stats) {
        if (stats != null) {
            statsCardsContainer.getChildren().add(crearTarjetaMetrica("Reportes Históricos", String.valueOf(stats.totalReports()), COLOR_PURPLE));
            statsCardsContainer.getChildren().add(crearTarjetaMetrica("Reportes Pendientes", String.valueOf(stats.pendingReports()), COLOR_ORANGE));
            statsCardsContainer.getChildren().add(crearTarjetaMetrica("Reportes Resueltos", String.valueOf(stats.resolvedReports()), COLOR_GREEN));
        }
    }

    private VBox crearTarjetaMetrica(String titulo, String valor, String colorHex) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPrefSize(220, 110);
        card.setStyle("-fx-background-color: #1a1a1a; -fx-padding: 15; -fx-background-radius: 8; -fx-border-color: " + colorHex + "; -fx-border-width: 0 0 0 5;");
        
        Label titleLbl = new Label(titulo);
        titleLbl.setTextFill(Color.GRAY);
        titleLbl.setStyle(STYLE_BOLD + " -fx-font-size: 13px;");
        
        Label valLbl = new Label(valor);
        valLbl.setTextFill(Color.WHITE);
        valLbl.setStyle("-fx-font-size: 28px; " + STYLE_BOLD);
        
        card.getChildren().addAll(titleLbl, valLbl);
        return card;
    }

    private String traducirRazon(String reasonCode) {
        if (reasonCode == null) {
            return "Otro";
        }
        
        return switch (reasonCode) {
            case "SPAM" -> "Spam o contenido comercial no deseado";
            case "OFFENSIVE_CONTENT" -> "Contenido ofensivo o abusivo";
            case "HARASSMENT" -> "Acoso o intimidación";
            case "HATE_SPEECH" -> "Incitación al odio o discriminación";
            case "SEXUAL_CONTENT" -> "Contenido sexual o explícito";
            case "VIOLENCE" -> "Violencia o daño físico";
            case "SPOILER" -> "Spoiler sin advertencia previa";
            case "FAKE_PROFILE" -> "Perfil falso o suplantación de identidad";
            case "INAPPROPRIATE_IMAGE", "Imagen de perfil o contenido inapropiado" -> "Imagen inapropiada";
            default -> "Otro motivo";
        };
    }

    private void prepararFiltrosReportes() {
        reportStatusFilter.getItems().addAll(statusMapUiToBackend.keySet());
        reportStatusFilter.setValue("Pendiente");
    }

    private HBox construirFilaUsuario(UserDto user) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: #2a2a2a; -fx-padding: 10; -fx-background-radius: 5; -fx-cursor: hand;");

        Label name = new Label(user.username());
        name.setTextFill(Color.WHITE);
        name.setStyle(STYLE_BOLD);

        Label email = new Label(user.email());
        email.setTextFill(Color.GRAY);

        row.getChildren().addAll(name, email);
        row.setOnMouseClicked(e -> cargarDetalleUsuario(user));

        return row;
    }

    private void cargarDetalleUsuario(UserDto user) {
        userDetailPane.getChildren().clear();
        String authId = user.getSafeAuthId();
        
        CompletableFuture<AccountStatusDto> statusFuture = adminService.getAccountStatus(authId);
        CompletableFuture<AdminUserDetailDto> detailsFuture = adminService.getAdminUserDetails(authId);
        
        statusFuture.thenAcceptBoth(detailsFuture, (status, details) -> 
            Platform.runLater(() -> dibujarPanelDetalleUsuario(user, status, details, authId))
        ).exceptionally(e -> {
            App.procesarErrorCritico(e);
            return null;
        });
    }
    
    private void configurarBuscadorUsuarios() {
        if (searchUserField != null) {
            searchUserField.setOnAction(e -> handleSearchUsers());
        }
    }

    private void dibujarPanelDetalleUsuario(UserDto user, AccountStatusDto status, AdminUserDetailDto details, String authId) {
        userDetailPane.getChildren().clear();

        Label header = new Label("Detalles de Usuario: @" + user.username());
        configurarClicPerfil(header, user);

        VBox infoBox = new VBox(5);
        
        if (details != null) {
            Label lblName = new Label("Nombre: " + details.name());
            lblName.setTextFill(Color.LIGHTGRAY);
            
            String verificadoStr = Boolean.TRUE.equals(details.isEmailVerified()) ? " (Verificado)" : " (No Verificado)";
            Label lblEmail = new Label("Email: " + details.email() + verificadoStr);
            lblEmail.setTextFill(Color.LIGHTGRAY);
            
            Label lblRole = new Label("Rol: " + details.role());
            lblRole.setTextFill(Color.LIGHTGRAY);
            
            Label lblCreated = new Label("Miembro desde: " + formatearFecha(details.createdAt()));
            lblCreated.setTextFill(Color.LIGHTGRAY);
            
            infoBox.getChildren().addAll(lblName, lblEmail, lblRole, lblCreated);
        }

        String stStr = (status != null && status.accountStatus() != null) ? status.accountStatus() : TEXT_DESCONOCIDO;
        
        Label statusLbl = new Label("Estado: " + traducirEstadoUsuario(stStr));
        statusLbl.setTextFill(Color.web(AppConstants.COLOR_ACCENT));
        statusLbl.setStyle(STYLE_BOLD);

        userDetailPane.getChildren().addAll(header, infoBox, statusLbl);

        if (STATUS_SUSPENDED.equals(stStr) && status != null && status.suspendedUntil() != null) {
            Label suspLbl = new Label("Suspendido hasta: " + formatearFecha(status.suspendedUntil()));
            suspLbl.setTextFill(Color.ORANGE);
            userDetailPane.getChildren().add(suspLbl);
        }

        inyectarBotonesUsuario(stStr, authId, user, details);
    }

    private void inyectarBotonesUsuario(String stStr, String authId, UserDto user, AdminUserDetailDto details) {
        FlowPane actions = new FlowPane();
        actions.setHgap(10);
        actions.setVgap(10);
        
        if (STATUS_BANNED.equals(stStr)) {
            Button unban = new Button("Desbanear");
            unban.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; -fx-cursor: hand;");
            unban.setOnAction(e -> procesarAccionUsuario(adminService.unbanUser(authId), user));
            actions.getChildren().add(unban);
        } else {
            Button ban = new Button("Banear Permanente");
            ban.setStyle("-fx-background-color: #e50914; -fx-text-fill: white; -fx-cursor: hand;");
            ban.setOnAction(e -> procesarAccionUsuario(adminService.banUser(authId, "Violación a los términos (Admin)"), user));

            ComboBox<String> dur = new ComboBox<>();
            dur.getItems().addAll(durationMapUiToBackend.keySet());
            dur.setPromptText("Duración");

            Button susp = new Button("Suspender");
            susp.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white; -fx-cursor: hand;");
            susp.setOnAction(e -> {
                if (dur.getValue() != null) {
                    String backendDur = durationMapUiToBackend.get(dur.getValue());
                    String reason = "Suspensión administrativa desde el panel de control.";
                    procesarAccionUsuario(adminService.suspendUser(authId, backendDur, reason), user);
                }
            });

            actions.getChildren().addAll(ban, dur, susp);
        }

        if (details != null && details.profileImage() != null) {
            Button rmPhoto = new Button("Quitar Foto Perfil");
            rmPhoto.setStyle(COLOR_DARK + " -fx-text-fill: white; -fx-cursor: hand;");
            
            rmPhoto.setOnAction(e -> 
                adminService.removeProfilePhotoDirectly(authId).thenRun(() -> 
                    Platform.runLater(() -> {
                        mostrarAlertaExito("Foto de perfil eliminada correctamente.");
                        cargarDetalleUsuario(user);
                    })
                ).exceptionally(err -> {
                    Platform.runLater(() -> mostrarAlertaError("Error al eliminar la foto."));
                    return null;
                })
            );
            actions.getChildren().add(rmPhoto);
        }

        userDetailPane.getChildren().add(actions);
    }

    private void configurarClicPerfil(Label label, UserDto user) {
        label.setTextFill(Color.web(AppConstants.COLOR_ACCENT));
        label.setStyle("-fx-font-size: 18px; " + STYLE_BOLD + " -fx-cursor: hand; -fx-underline: true;");
        label.setOnMouseClicked(e -> App.showProfileView(user));
    }

    private String traducirEstadoUsuario(String status) {
        if (STATUS_ACTIVE.equals(status)) {
            return "Activo";
        }
        if (STATUS_SUSPENDED.equals(status)) {
            return "Suspendido";
        }
        if (STATUS_BANNED.equals(status)) {
            return "Baneado";
        }
        return TEXT_DESCONOCIDO;
    }

    private void procesarAccionUsuario(java.util.concurrent.CompletableFuture<Void> futuro, UserDto user) {
        futuro.thenRun(() -> 
            Platform.runLater(() -> {
                mostrarAlertaExito(AppConstants.MESSAGE_SUCCESS_ADMIN_ACTION);
                cargarDetalleUsuario(user);
            })
        ).exceptionally(e -> {
            Platform.runLater(() -> {
                if (!App.procesarErrorCritico(e)) {
                    mostrarAlertaError("Error al modificar estado.");
                }
            });
            return null;
        });
    }

    private void peticionReportes(int page) {
        String filterUi = reportStatusFilter.getValue();
        String filterBackend = statusMapUiToBackend.get(filterUi);
        
        adminService.getAdminReports(filterBackend, page).thenAccept(res -> 
            Platform.runLater(() -> {
                if (res != null && res.reports() != null) {
                    for (AdminReportDto r : res.reports()) {
                        reportsListContainer.getChildren().add(construirFilaReporte(r));
                    }
                }
            })
        ).exceptionally(e -> {
            App.procesarErrorCritico(e);
            return null;
        });
    }

    private HBox construirFilaReporte(AdminReportDto r) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: #2a2a2a; -fx-padding: 10; -fx-background-radius: 5; -fx-cursor: hand;");

        String translatedType = targetTypeMap.getOrDefault(r.targetType(), r.targetType());
        Label type = new Label("[" + translatedType + "]");
        type.setTextFill(Color.web(AppConstants.COLOR_ACCENT));
        type.setStyle(STYLE_BOLD);

        Label reason = new Label(traducirRazon(r.reason()));
        reason.setTextFill(Color.WHITE);

        row.getChildren().addAll(type, reason);
        row.setOnMouseClicked(e -> dibujarDetalleReporte(r));

        return row;
    }

    private void dibujarDetalleReporte(AdminReportDto r) {
        reportDetailPane.getChildren().clear();

        String translatedStatus = TEXT_DESCONOCIDO;
        for (Map.Entry<String, String> entry : statusMapUiToBackend.entrySet()) {
            if (entry.getValue().equals(r.status())) {
                translatedStatus = entry.getKey();
                break;
            }
        }

        Label header = new Label("Reporte #" + r.id() + " - " + translatedStatus);
        header.setTextFill(Color.WHITE);
        header.setFont(Font.font("System", FontWeight.BOLD, 18));

        VBox explanationBox = new VBox(5);
        explanationBox.setStyle("-fx-background-color: #121212; -fx-padding: 15; -fx-background-radius: 8;");
        resolverYDibujarExplicacion(r, explanationBox);

        reportDetailPane.getChildren().addAll(header, explanationBox);
        dibujarInteraccionesReporte(r, reportDetailPane);
    }

    private void dibujarInteraccionesReporte(AdminReportDto r, VBox parent) {
        if (STATUS_PENDING.equals(r.status())) {
            TextArea noteArea = new TextArea();
            noteArea.setPromptText("Nota administrativa (opcional)...");
            noteArea.setPrefRowCount(2);
            noteArea.setStyle("-fx-control-inner-background: #2a2a2a; -fx-text-inner-color: white;");

            FlowPane actionsPane = new FlowPane();
            actionsPane.setHgap(10);
            actionsPane.setVgap(10);

            inyectarBotonesDeAccionReporte(r, actionsPane, noteArea);

            parent.getChildren().addAll(noteArea, actionsPane);
            return;
        }

        if (r.adminNote() != null && !r.adminNote().trim().isEmpty()) {
            VBox noteBox = new VBox(5);
            noteBox.setStyle("-fx-background-color: #2a2a2a; -fx-padding: 10; -fx-background-radius: 5; -fx-border-color: #ff9800; -fx-border-width: 0 0 0 4;");
            
            Label noteTitle = new Label("Nota de resolución:");
            noteTitle.setTextFill(Color.web(COLOR_ORANGE));
            noteTitle.setStyle(STYLE_BOLD);
            
            Label noteContent = new Label(r.adminNote());
            noteContent.setTextFill(Color.WHITE);
            noteContent.setWrapText(true);
            
            noteBox.getChildren().addAll(noteTitle, noteContent);
            parent.getChildren().add(noteBox);
        }
    }
    
    private void resolverYDibujarExplicacion(AdminReportDto r, VBox container) {
        Label loading = new Label("Construyendo informe del reporte...");
        loading.setTextFill(Color.LIGHTGRAY);
        loading.setWrapText(true);
        container.getChildren().add(loading);

        CompletableFuture<UserDto> reporterFuture = userService.getUserById(r.reporterAuthId());
        CompletableFuture<UserDto> reportedFuture = obtenerUsuarioReportado(r);

        reporterFuture.thenAcceptBoth(reportedFuture, (reporter, reported) -> 
            Platform.runLater(() -> {
                container.getChildren().clear();
                if (TARGET_REVIEW.equals(r.targetType())) {
                    procesarExplicacionResenaFlujo(r, reporter, reported, container);
                } else if (TARGET_USER.equals(r.targetType())) {
                    procesarExplicacionUsuarioFlujo(r, reporter, reported, container);
                } else if (TARGET_COMMENT.equals(r.targetType())) {
                    procesarExplicacionComentarioFlujo(r, reporter, reported, container);
                } else {
                    procesarExplicacionGenericaFlujo(r, reporter, reported, container);
                }
            })
        ).exceptionally(err -> {
            Platform.runLater(() -> {
                if (!App.procesarErrorCritico(err)) {
                    container.getChildren().clear();
                    Label errLbl = new Label("Error al cargar la información del reporte.");
                    errLbl.setTextFill(Color.web(AppConstants.COLOR_ACCENT));
                    container.getChildren().add(errLbl);
                }
            });
            return null;
        });
    }

    private CompletableFuture<UserDto> obtenerUsuarioReportado(AdminReportDto r) {
        if (TARGET_USER.equals(r.targetType())) {
            return userService.getUserById(r.targetId());
        }
        
        Map<String, Object> snap = r.targetSnapshot();
        if (snap != null) {
            if (snap.containsKey("ownerId")) {
                return userService.getUserById(String.valueOf(snap.get("ownerId")));
            }
            if (snap.containsKey("username")) {
                return userService.getUserByUsername(String.valueOf(snap.get("username")));
            }
        }
        return CompletableFuture.completedFuture(null);
    }

    private javafx.scene.text.Text crearTextoPlano(String contenido) {
        javafx.scene.text.Text text = new javafx.scene.text.Text(contenido);
        text.setFill(Color.LIGHTGRAY);
        return text;
    }

    private javafx.scene.text.Text crearEnlacePerfil(UserDto user, String fallbackName) {
        String name = (user != null && user.username() != null) ? "@" + user.username() : fallbackName;
        
        javafx.scene.text.Text link = new javafx.scene.text.Text(name);
        link.setFill(Color.web(AppConstants.COLOR_ACCENT));
        link.setStyle(STYLE_BOLD + " -fx-cursor: hand; -fx-underline: true;");
        
        if (user != null) {
            link.setOnMouseClicked(e -> App.showProfileView(user));
        }
        return link;
    }

    private void procesarExplicacionResenaFlujo(AdminReportDto r, UserDto reporter, UserDto reported, VBox container) {
        javafx.scene.text.TextFlow flow = new javafx.scene.text.TextFlow();
        
        flow.getChildren().add(crearTextoPlano(TEXT_EL_USUARIO));
        flow.getChildren().add(crearEnlacePerfil(reporter, TEXT_AT_DESCONOCIDO));
        flow.getChildren().add(crearTextoPlano(" reporta una RESEÑA del usuario "));
        flow.getChildren().add(crearEnlacePerfil(reported, TEXT_AT_DESCONOCIDO));
        flow.getChildren().add(crearTextoPlano(TEXT_POR + traducirRazon(r.reason()) + TEXT_JUSTIFICANDO + r.description() + "\"." + System.lineSeparator() + System.lineSeparator()));
        
        Map<String, Object> snap = r.targetSnapshot();
        String content = String.valueOf(snap.getOrDefault("content", "Sin contenido"));
        flow.getChildren().add(crearTextoPlano("Contenido original de la reseña: \"" + content + "\""));
        
        Object tvIdObj = snap.get("tvmazeId");
        if (tvIdObj != null) {
            Integer tvId = ((Double) tvIdObj).intValue();
            showService.getFullShowDetails(tvId).thenAccept(full -> {
                final String finalSeries = (full != null && full.show() != null) ? full.show().name() : "Serie desconocida";
                Platform.runLater(() -> flow.getChildren().add(crearTextoPlano(System.lineSeparator() + System.lineSeparator() + "En la serie: " + finalSeries)));
            });
        }
        container.getChildren().add(flow);
    }

    private void procesarExplicacionUsuarioFlujo(AdminReportDto r, UserDto reporter, UserDto reported, VBox container) {
        javafx.scene.text.TextFlow flow = new javafx.scene.text.TextFlow();
        flow.getChildren().addAll(
            crearTextoPlano(TEXT_EL_USUARIO),
            crearEnlacePerfil(reporter, TEXT_AT_DESCONOCIDO),
            crearTextoPlano(" reporta al perfil de "),
            crearEnlacePerfil(reported, TEXT_AT_DESCONOCIDO),
            crearTextoPlano(TEXT_POR + traducirRazon(r.reason()) + TEXT_JUSTIFICANDO + r.description() + "\".")
        );
        container.getChildren().add(flow);
    }

    private void procesarExplicacionComentarioFlujo(AdminReportDto r, UserDto reporter, UserDto reported, VBox container) {
        javafx.scene.text.TextFlow flow = new javafx.scene.text.TextFlow();
        flow.getChildren().addAll(
            crearTextoPlano(TEXT_EL_USUARIO),
            crearEnlacePerfil(reporter, TEXT_AT_DESCONOCIDO),
            crearTextoPlano(" reporta un COMENTARIO del usuario "),
            crearEnlacePerfil(reported, TEXT_AT_DESCONOCIDO),
            crearTextoPlano(TEXT_POR + traducirRazon(r.reason()) + TEXT_JUSTIFICANDO + r.description() + "\"." + System.lineSeparator() + System.lineSeparator())
        );
        
        Map<String, Object> snap = r.targetSnapshot();
        String content = String.valueOf(snap.getOrDefault("content", "Sin contenido"));
        flow.getChildren().add(crearTextoPlano("Contenido original del comentario: \"" + content + "\""));
        container.getChildren().add(flow);
    }

    private void procesarExplicacionGenericaFlujo(AdminReportDto r, UserDto reporter, UserDto reported, VBox container) {
        javafx.scene.text.TextFlow flow = new javafx.scene.text.TextFlow();
        flow.getChildren().addAll(
            crearTextoPlano(TEXT_EL_USUARIO),
            crearEnlacePerfil(reporter, TEXT_AT_DESCONOCIDO),
            crearTextoPlano(" reporta un elemento de tipo " + r.targetType() + " del usuario "),
            crearEnlacePerfil(reported, TEXT_AT_DESCONOCIDO),
            crearTextoPlano(TEXT_POR + traducirRazon(r.reason()) + ".")
        );
        container.getChildren().add(flow);
    }

    private void inyectarBotonesDeAccionReporte(AdminReportDto r, FlowPane container, TextArea noteArea) {
        if (STATUS_PENDING.equals(r.status()) && r.availableActions() != null) {
            for (String actBackend : r.availableActions()) {
                String actUi = actionMapBackendToUi.getOrDefault(actBackend, actBackend);
                Button btn = new Button(actUi);
                btn.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-cursor: hand;");
                btn.setOnAction(e -> procesarClicAccionReporte(r, actBackend, noteArea.getText()));
                container.getChildren().add(btn);
            }
        }
    }

    private void procesarClicAccionReporte(AdminReportDto r, String actionBackend, String note) {
        if (ACTION_SUSPEND.equals(actionBackend)) {
            solicitarDuracionSuspension(r.id(), actionBackend, note);
            return;
        }
        if (ACTION_DISMISS.equals(actionBackend)) {
             ejecutarDescarteDirecto(r.id(), note);
             return;
        }
        if (esAccionDeBypass(actionBackend)) {
            ejecutarAccionConBypass(r, actionBackend, note);
            return;
        }
        ejecutarAccion(r.id(), actionBackend, note, null);
    }

    private void ejecutarDescarteDirecto(Integer reportId, String note) {
        adminService.dismissReport(String.valueOf(reportId), note)
            .thenRun(this::finalizarAccionExito)
            .exceptionally(e -> {
                Platform.runLater(() -> {
                    if (!App.procesarErrorCritico(e)) mostrarAlertaError("Error descartando reporte.");
                });
                return null;
            });
    }

    private boolean esAccionDeBypass(String action) {
        return ACTION_DEL_REVIEW.equals(action) ||
               ACTION_DEL_COMMENT.equals(action) ||
               ACTION_RM_REV_IMG.equals(action) ||
               ACTION_RM_COM_IMG.equals(action);
    }

    private void ejecutarAccionConBypass(AdminReportDto r, String action, String note) {
        String targetId = r.targetId();
        java.util.concurrent.CompletableFuture<Void> future = null;
        String defaultNote = "";
        
        if (action != null){ 
            switch (action) {
                case ACTION_DEL_REVIEW -> {
                    future = adminService.deleteReviewDirectly(targetId);
                    defaultNote = "Reseña eliminada administrativamente.";
                }
                case ACTION_DEL_COMMENT -> {
                    future = adminService.deleteCommentDirectly(targetId);
                    defaultNote = "Comentario eliminado administrativamente.";
                }
                case ACTION_RM_REV_IMG -> {
                    future = adminService.removeReviewImageDirectly(targetId);
                    defaultNote = "Imagen de reseña eliminada administrativamente.";
                }
                case ACTION_RM_COM_IMG -> {
                    future = adminService.removeCommentImageDirectly(targetId);
                    defaultNote = "Imagen de comentario eliminada administrativamente.";
                }
            }
        }
        
        if (future != null) {
            String finalNote = (note == null || note.trim().isEmpty()) ? defaultNote : note;
            future.whenComplete((res, ex) -> 
                adminService.dismissReport(String.valueOf(r.id()), finalNote)
                    .thenRun(this::finalizarAccionExito)
                    .exceptionally(e2 -> {
                        Platform.runLater(() -> {
                            if (!App.procesarErrorCritico(e2)) mostrarAlertaError("Error cerrando el reporte.");
                        });
                        return null;
                    })
            );
        }
    }

    private void solicitarDuracionSuspension(Integer id, String actionBackend, String note) {
        List<String> choices = new ArrayList<>(durationMapUiToBackend.keySet());
        ChoiceDialog<String> dialog = new ChoiceDialog<>(choices.get(0), choices);
        
        dialog.setTitle("Suspender Usuario");
        dialog.setHeaderText("Selecciona la duración de la suspensión");
        
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String backendDur = durationMapUiToBackend.get(result.get());
            ejecutarAccion(id, actionBackend, note, backendDur);
        }
    }

    private void ejecutarAccion(Integer id, String actionBackend, String note, String durationBackend) {
        AdminActionRequest req = new AdminActionRequest(actionBackend, note, durationBackend);
        adminService.executeReportAction(String.valueOf(id), req)
            .thenRun(this::finalizarAccionExito)
            .exceptionally(e -> {
                Platform.runLater(() -> {
                    if (!App.procesarErrorCritico(e)) mostrarAlertaError("Error ejecutando acción.");
                });
                return null;
            });
    }

    private void finalizarAccionExito() {
        Platform.runLater(() -> {
            mostrarAlertaExito(AppConstants.MESSAGE_SUCCESS_ADMIN_ACTION);
            loadReports();
        });
    }

    private String formatearFecha(String iso) {
        try {
            return ZonedDateTime.parse(iso).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        } catch (Exception e) {
            return iso;
        }
    }

    private void mostrarAlertaError(String mensaje) {
        CustomAlertHelper.mostrarError(mensaje);
    }
    
    private void mostrarAlertaExito(String mensaje) {
        CustomAlertHelper.mostrarExito(mensaje);
    }
}