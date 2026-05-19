package com.src.filmtracker.controllers.admin;

import com.google.gson.GsonBuilder;
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
        statusMapUiToBackend.put("Pendiente", "PENDING");
        statusMapUiToBackend.put("Descartado", "DISMISSED");
        statusMapUiToBackend.put("Acción Tomada", "ACTION_TAKEN");
        statusMapUiToBackend.put("Todos", "ALL");

        durationMapUiToBackend.put("1 día", "1_DAY");
        durationMapUiToBackend.put("3 días", "3_DAYS");
        durationMapUiToBackend.put("7 días", "7_DAYS");
        durationMapUiToBackend.put("30 días", "30_DAYS");

        targetTypeMap.put("USER", "Usuario");
        targetTypeMap.put("REVIEW", "Reseña");
        targetTypeMap.put("COMMENT", "Comentario");

        actionMapBackendToUi.put("DISMISS_REPORT", "Descartar Reporte");
        actionMapBackendToUi.put("SUSPEND_USER", "Suspender Usuario");
        actionMapBackendToUi.put("BAN_USER", "Banear Usuario");
        actionMapBackendToUi.put("DELETE_REVIEW", "Eliminar Reseña");
        actionMapBackendToUi.put("DELETE_COMMENT", "Eliminar Comentario");
        actionMapBackendToUi.put("REMOVE_PROFILE_IMAGE", "Eliminar Foto Perfil");
        actionMapBackendToUi.put("REMOVE_REVIEW_IMAGE", "Eliminar Foto Reseña");
        actionMapBackendToUi.put("REMOVE_COMMENT_IMAGE", "Eliminar Foto Comentario");
    }

    @FXML
    private void loadStats() {
        if (statsCardsContainer == null) {
            return;
        }
        
        statsCardsContainer.getChildren().clear();

        adminService.getAuthStats().thenAccept(stats -> {
            Platform.runLater(() -> renderAuthStats(stats));
        }).exceptionally(e -> null);

        adminService.getReviewStats().thenAccept(stats -> {
            Platform.runLater(() -> renderReviewStats(stats));
        }).exceptionally(e -> null);

        adminService.getModerationStats().thenAccept(stats -> {
            Platform.runLater(() -> renderModerationStats(stats));
        }).exceptionally(e -> null);
    }
    
    @FXML private void handleBack() { 
        App.setRoot(AppConstants.FXML_DASHBOARD); 
    }
    
    @FXML private void handleMinimize() { 
        ((Stage) searchUserField.getScene().getWindow()).setIconified(true); 
    }
    
    @FXML private void handleClose() { 
        Platform.exit(); System.exit(0); 
    }

    @FXML
    private void handleSearchUsers() {
        String query = searchUserField.getText().trim();
        
        if (query.isEmpty()) {
            return;
        }

        usersListContainer.getChildren().clear();
        userDetailPane.getChildren().clear();

        adminService.searchUsers(query).thenAccept(users -> {
            Platform.runLater(() -> {
                if (users != null) {
                    for (UserDto user : users) {
                        usersListContainer.getChildren().add(construirFilaUsuario(user));
                    }
                }
            });
        }).exceptionally(e -> null);
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
        if (stats == null) {
            return;
        }
        
        statsCardsContainer.getChildren().add(crearTarjetaMétrica("Usuarios Registrados", String.valueOf(stats.totalUsers()), "#4caf50"));

        if (stats.byStatus() != null) {
            Integer activos = stats.byStatus().getOrDefault("ACTIVE", 0);
            Integer suspendidos = stats.byStatus().getOrDefault("SUSPENDED", 0);
            Integer baneados = stats.byStatus().getOrDefault("BANNED", 0);
            
            statsCardsContainer.getChildren().add(crearTarjetaMétrica("Usuarios Activos", String.valueOf(activos), "#4caf50"));
            statsCardsContainer.getChildren().add(crearTarjetaMétrica("Cuentas Suspendidas", String.valueOf(suspendidos), "#ff9800"));
            statsCardsContainer.getChildren().add(crearTarjetaMétrica("Cuentas Baneadas", String.valueOf(baneados), "#e50914"));
        }
    }

    private void renderReviewStats(ReviewStatsDto stats) {
        if (stats == null) {
            return;
        }
        
        if (stats.totals() != null) {
            Number reviews = stats.totals().getOrDefault("reviews", 0);
            Number comments = stats.totals().getOrDefault("comments", 0);
            Number likes = stats.totals().getOrDefault("likes", 0);
            
            statsCardsContainer.getChildren().add(crearTarjetaMétrica("Reseñas Globales", String.valueOf(reviews), "#2196f3"));
            statsCardsContainer.getChildren().add(crearTarjetaMétrica("Comentarios Globales", String.valueOf(comments), "#2196f3"));
            statsCardsContainer.getChildren().add(crearTarjetaMétrica("Likes Repartidos", String.valueOf(likes), "#e91e63"));
        }
    }

    private void renderModerationStats(ModerationStatsDto stats) {
        if (stats == null) {
            return;
        }
        
        statsCardsContainer.getChildren().add(crearTarjetaMétrica("Reportes Históricos", String.valueOf(stats.totalReports()), "#9c27b0"));
        statsCardsContainer.getChildren().add(crearTarjetaMétrica("Reportes Pendientes", String.valueOf(stats.pendingReports()), "#ff9800"));
        statsCardsContainer.getChildren().add(crearTarjetaMétrica("Reportes Resueltos", String.valueOf(stats.resolvedReports()), "#4caf50"));
    }

    private VBox crearTarjetaMétrica(String titulo, String valor, String colorHex) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPrefSize(220, 110);
        card.setStyle("-fx-background-color: #1a1a1a; -fx-padding: 15; -fx-background-radius: 8; -fx-border-color: " + colorHex + "; -fx-border-width: 0 0 0 5;");
        
        Label titleLbl = new Label(titulo);
        titleLbl.setTextFill(Color.GRAY);
        titleLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        
        Label valLbl = new Label(valor);
        valLbl.setTextFill(Color.WHITE);
        valLbl.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");
        
        card.getChildren().add(titleLbl);
        card.getChildren().add(valLbl);
        
        return card;
    }

    private String traducirRazon(String reasonCode) {
        if (reasonCode == null) {
            return "Otro";
        }
        
        switch (reasonCode) {
            case "SPAM": return "Spam o comercial";
            case "OFFENSIVE_CONTENT": return "Contenido ofensivo";
            case "HARASSMENT": return "Acoso";
            case "HATE_SPEECH": return "Discurso de odio";
            case "SEXUAL_CONTENT": return "Contenido sexual";
            case "VIOLENCE": return "Violencia";
            case "SPOILER": return "Spoiler sin aviso";
            case "FAKE_PROFILE": return "Perfil falso";
            case "INAPPROPRIATE_IMAGE": return "Imagen inapropiada";
            default: return "Otro";
        }
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
        name.setStyle("-fx-font-weight: bold;");

        Label email = new Label(user.email());
        email.setTextFill(Color.GRAY);

        row.getChildren().add(name);
        row.getChildren().add(email);

        row.setOnMouseClicked(e -> {
            cargarDetalleUsuario(user);
        });

        return row;
    }

    private void cargarDetalleUsuario(UserDto user) {
        userDetailPane.getChildren().clear();
        String authId = user.getSafeAuthId();

        CompletableFuture<AccountStatusDto> statusFuture = adminService.getAccountStatus(authId);
        CompletableFuture<AdminUserDetailDto> detailsFuture = adminService.getAdminUserDetails(authId);

        statusFuture.thenAcceptBoth(detailsFuture, (status, details) -> {
            Platform.runLater(() -> {
                dibujarPanelDetalleUsuario(user, status, details, authId);
            });
        }).exceptionally(e -> null);
    }

    private void dibujarPanelDetalleUsuario(UserDto user, AccountStatusDto status, AdminUserDetailDto details, String authId) {
        userDetailPane.getChildren().clear();

        Label header = new Label("Detalles de Usuario: @" + user.username());
        header.setTextFill(Color.WHITE);
        header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        VBox infoBox = new VBox(5);
        
        if (details != null) {
            Label lblName = new Label("Nombre: " + details.name());
            lblName.setTextFill(Color.LIGHTGRAY);
            
            String verificadoStr = details.isEmailVerified() ? " (Verificado)" : " (No Verificado)";
            Label lblEmail = new Label("Email: " + details.email() + verificadoStr);
            lblEmail.setTextFill(Color.LIGHTGRAY);
            
            Label lblRole = new Label("Rol: " + details.role());
            lblRole.setTextFill(Color.LIGHTGRAY);
            
            Label lblCreated = new Label("Miembro desde: " + formatearFecha(details.createdAt()));
            lblCreated.setTextFill(Color.LIGHTGRAY);
            
            infoBox.getChildren().addAll(lblName, lblEmail, lblRole, lblCreated);
        }

        String stStr = (status != null) ? status.accountStatus() : "Desconocido";
        Label statusLbl = new Label("Estado: " + traducirEstadoUsuario(stStr));
        statusLbl.setTextFill(Color.web(AppConstants.COLOR_ACCENT));
        statusLbl.setStyle("-fx-font-weight: bold;");

        userDetailPane.getChildren().addAll(header, infoBox, statusLbl);

        if ("SUSPENDED".equals(stStr)) {
            if (status != null) {
                if (status.suspendedUntil() != null) {
                    Label suspLbl = new Label("Suspendido hasta: " + formatearFecha(status.suspendedUntil()));
                    suspLbl.setTextFill(Color.ORANGE);
                    userDetailPane.getChildren().add(suspLbl);
                }
            }
        }

        FlowPane actions = new FlowPane();
        actions.setHgap(10);
        actions.setVgap(10);
        
        if ("BANNED".equals(stStr)) {
            Button unban = new Button("Desbanear");
            unban.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; -fx-cursor: hand;");
            
            unban.setOnAction(e -> {
                procesarAccionUsuario(adminService.unbanUser(authId), user);
            });
            
            actions.getChildren().add(unban);
        } else {
            Button ban = new Button("Banear Permanente");
            ban.setStyle("-fx-background-color: #e50914; -fx-text-fill: white; -fx-cursor: hand;");
            
            ban.setOnAction(e -> {
                procesarAccionUsuario(adminService.banUser(authId, "Violación a los términos (Admin)"), user);
            });

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

        if (details != null) {
            if (details.profileImage() != null) {
                Button rmPhoto = new Button("Quitar Foto Perfil");
                rmPhoto.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-cursor: hand;");
                
                rmPhoto.setOnAction(e -> {
                    adminService.removeProfilePhotoDirectly(authId).thenRun(() -> {
                        Platform.runLater(() -> {
                            mostrarAlertaExito("Foto de perfil eliminada correctamente.");
                            cargarDetalleUsuario(user);
                        });
                    }).exceptionally(err -> {
                        Platform.runLater(() -> mostrarAlertaError("Error al eliminar la foto."));
                        return null;
                    });
                });
                
                actions.getChildren().add(rmPhoto);
            }
        }

        userDetailPane.getChildren().add(actions);
    }

    private String traducirEstadoUsuario(String status) {
        if ("ACTIVE".equals(status)) {
            return "Activo";
        }
        
        if ("SUSPENDED".equals(status)) {
            return "Suspendido";
        }
        
        if ("BANNED".equals(status)) {
            return "Baneado";
        }
        
        return "Desconocido";
    }

    private void procesarAccionUsuario(java.util.concurrent.CompletableFuture<Void> futuro, UserDto user) {
        futuro.thenRun(() -> {
            Platform.runLater(() -> {
                mostrarAlertaExito(AppConstants.MESSAGE_SUCCESS_ADMIN_ACTION);
                cargarDetalleUsuario(user);
            });
        }).exceptionally(e -> {
            Platform.runLater(() -> mostrarAlertaError("Error al modificar estado."));
            return null;
        });
    }

    private void peticionReportes(int page) {
        String filterUi = reportStatusFilter.getValue();
        String filterBackend = statusMapUiToBackend.get(filterUi);

        adminService.getAdminReports(filterBackend, page).thenAccept(res -> {
            Platform.runLater(() -> {
                if (res != null) {
                    if (res.reports() != null) {
                        for (AdminReportDto r : res.reports()) {
                            reportsListContainer.getChildren().add(construirFilaReporte(r));
                        }
                    }
                }
            });
        }).exceptionally(e -> null);
    }

    private HBox construirFilaReporte(AdminReportDto r) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: #2a2a2a; -fx-padding: 10; -fx-background-radius: 5; -fx-cursor: hand;");

        String translatedType = targetTypeMap.getOrDefault(r.targetType(), r.targetType());
        Label type = new Label("[" + translatedType + "]");
        type.setTextFill(Color.web(AppConstants.COLOR_ACCENT));
        type.setStyle("-fx-font-weight: bold;");

        Label reason = new Label(traducirRazon(r.reason()));
        reason.setTextFill(Color.WHITE);

        row.getChildren().add(type);
        row.getChildren().add(reason);

        row.setOnMouseClicked(e -> {
            dibujarDetalleReporte(r);
        });

        return row;
    }

    private void dibujarDetalleReporte(AdminReportDto r) {
        reportDetailPane.getChildren().clear();

        String translatedStatus = "Desconocido";
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

        TextArea noteArea = new TextArea();
        noteArea.setPromptText("Nota administrativa (opcional)...");
        noteArea.setPrefRowCount(2);
        noteArea.setStyle("-fx-control-inner-background: #2a2a2a; -fx-text-inner-color: white;");

        FlowPane actionsPane = new FlowPane();
        actionsPane.setHgap(10);
        actionsPane.setVgap(10);

        inyectarBotonesDeAccionReporte(r, actionsPane, noteArea);

        reportDetailPane.getChildren().add(header);
        reportDetailPane.getChildren().add(explanationBox);
        reportDetailPane.getChildren().add(noteArea);
        reportDetailPane.getChildren().add(actionsPane);
    }

    private void resolverYDibujarExplicacion(AdminReportDto r, VBox container) {
        Label lbl = new Label("Construyendo informe del reporte...");
        lbl.setTextFill(Color.LIGHTGRAY);
        lbl.setWrapText(true);
        container.getChildren().add(lbl);

        userService.getUserById(r.reporterAuthId()).thenAccept(reporter -> {
            String reporterName = (reporter != null) ? "@" + reporter.username() : "Usuario desconocido";
            
            if ("REVIEW".equals(r.targetType())) {
                procesarExplicacionResena(r, reporterName, lbl);
                return;
            }
            
            if ("USER".equals(r.targetType())) {
                procesarExplicacionUsuario(r, reporterName, lbl);
                return;
            }

            if ("COMMENT".equals(r.targetType())) {
                procesarExplicacionComentario(r, reporterName, lbl);
                return;
            }

            Platform.runLater(() -> lbl.setText(String.format("El usuario %s reporta un elemento de tipo %s por %s.", 
                    reporterName, r.targetType(), traducirRazon(r.reason()))));
        });
    }

    private void procesarExplicacionResena(AdminReportDto r, String repName, Label lbl) {
        Map<String, Object> snap = r.targetSnapshot();
        String content = String.valueOf(snap.getOrDefault("content", "Sin contenido"));
        Object tvIdObj = snap.get("tvmazeId");
        
        String base = String.format("El usuario %s reporta una RESEÑA por %s justificando que \"%s\".\n\nContenido original: \"%s\"",
                repName, traducirRazon(r.reason()), r.description(), content);

        if (tvIdObj != null) {
            Integer tvId = ((Double) tvIdObj).intValue();
            showService.getFullShowDetails(tvId).thenAccept(full -> {
                String series = (full != null && full.show() != null) ? full.show().name() : "Serie desconocida";
                Platform.runLater(() -> lbl.setText(base + "\n\nEn la serie: " + series));
            });
            return;
        }
        
        Platform.runLater(() -> lbl.setText(base));
    }

    private void procesarExplicacionUsuario(AdminReportDto r, String repName, Label lbl) {
        Map<String, Object> snap = r.targetSnapshot();
        String targetUser = String.valueOf(snap.getOrDefault("username", "desconocido"));
        
        String text = String.format("El usuario %s reporta al perfil de @%s por %s justificando que \"%s\".",
                repName, targetUser, traducirRazon(r.reason()), r.description());
                
        Platform.runLater(() -> lbl.setText(text));
    }

    private void procesarExplicacionComentario(AdminReportDto r, String repName, Label lbl) {
        Map<String, Object> snap = r.targetSnapshot();
        String content = String.valueOf(snap.getOrDefault("content", "Sin contenido"));
        
        String text = String.format("El usuario %s reporta un COMENTARIO por %s justificando que \"%s\".\n\nContenido original: \"%s\"",
                repName, traducirRazon(r.reason()), r.description(), content);
                
        Platform.runLater(() -> lbl.setText(text));
    }

    private void inyectarBotonesDeAccionReporte(AdminReportDto r, FlowPane container, TextArea noteArea) {
        if ("PENDING".equals(r.status())) {
            if (r.availableActions() != null) {
                for (String actBackend : r.availableActions()) {
                    String actUi = actionMapBackendToUi.getOrDefault(actBackend, actBackend);
                    Button btn = new Button(actUi);
                    btn.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-cursor: hand;");
                    
                    btn.setOnAction(e -> {
                        procesarClicAccionReporte(r, actBackend, noteArea.getText());
                    });
                    
                    container.getChildren().add(btn);
                }
            }
        }
    }

    private void procesarClicAccionReporte(AdminReportDto r, String actionBackend, String note) {
        if ("SUSPEND_USER".equals(actionBackend)) {
            solicitarDuracionSuspension(r.id(), actionBackend, note);
            return;
        }
        
        if ("DISMISS_REPORT".equals(actionBackend)) {
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
                Platform.runLater(() -> mostrarAlertaError("Error descartando reporte."));
                return null;
            });
    }

    private boolean esAccionDeBypass(String action) {
        if ("DELETE_REVIEW".equals(action)) { 
            return true; 
        }
        
        if ("DELETE_COMMENT".equals(action)) { 
            return true; 
        }
        
        if ("REMOVE_REVIEW_IMAGE".equals(action)) { 
            return true; 
        }
        
        if ("REMOVE_COMMENT_IMAGE".equals(action)) { 
            return true; 
        }
        
        return false;
    }

    private void ejecutarAccionConBypass(AdminReportDto r, String action, String note) {
        String targetId = r.targetId();
        java.util.concurrent.CompletableFuture<Void> future = null;
        String defaultNote = "";

        if ("DELETE_REVIEW".equals(action)) {
            future = adminService.deleteReviewDirectly(targetId);
            defaultNote = "Reseña eliminada administrativamente.";
        } else if ("DELETE_COMMENT".equals(action)) {
            future = adminService.deleteCommentDirectly(targetId);
            defaultNote = "Comentario eliminado administrativamente.";
        } else if ("REMOVE_REVIEW_IMAGE".equals(action)) {
            future = adminService.removeReviewImageDirectly(targetId);
            defaultNote = "Imagen de reseña eliminada administrativamente.";
        } else if ("REMOVE_COMMENT_IMAGE".equals(action)) {
            future = adminService.removeCommentImageDirectly(targetId);
            defaultNote = "Imagen de comentario eliminada administrativamente.";
        }

        if (future != null) {
            String finalNote = (note == null || note.trim().isEmpty()) ? defaultNote : note;
            future.whenComplete((res, ex) -> {
                adminService.dismissReport(String.valueOf(r.id()), finalNote)
                    .thenRun(this::finalizarAccionExito)
                    .exceptionally(e2 -> {
                        Platform.runLater(() -> mostrarAlertaError("Error cerrando el reporte."));
                        return null;
                    });
            });
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
                Platform.runLater(() -> mostrarAlertaError("Error ejecutando acción."));
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
            ZonedDateTime zdt = ZonedDateTime.parse(iso);
            return zdt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
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