package com.src.filmtracker.controllers.admin;

import com.google.gson.GsonBuilder;
import com.src.filmtracker.App;
import com.src.filmtracker.models.admin.AccountStatusDto;
import com.src.filmtracker.models.admin.AdminActionRequest;
import com.src.filmtracker.models.admin.AdminReportDto;
import com.src.filmtracker.models.users.UserDto;
import com.src.filmtracker.services.admin.AdminService;
import com.src.filmtracker.services.admin.IAdminService;
import com.src.filmtracker.utils.AppConstants;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AdminPanelController {

    @FXML private TextField searchUserField;
    @FXML private VBox usersListContainer;
    @FXML private VBox userDetailPane;
    
    @FXML private ComboBox<String> reportStatusFilter;
    @FXML private VBox reportsListContainer;
    @FXML private VBox reportDetailPane;

    private final IAdminService adminService = new AdminService();
    
    private final Map<String, String> statusMapUiToBackend = new LinkedHashMap<>();
    private final Map<String, String> durationMapUiToBackend = new LinkedHashMap<>();
    private final Map<String, String> actionMapBackendToUi = new LinkedHashMap<>();

    @FXML
    public void initialize() {
        inicializarDiccionarios();
        prepararFiltrosReportes();
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
    
    private void inicializarDiccionarios() {
        statusMapUiToBackend.put("Pendiente", "PENDING");
        statusMapUiToBackend.put("Descartado", "DISMISSED");
        statusMapUiToBackend.put("Acción Tomada", "ACTION_TAKEN");
        statusMapUiToBackend.put("Todos", "ALL");

        durationMapUiToBackend.put("1 día", "1_DAY");
        durationMapUiToBackend.put("3 días", "3_DAYS");
        durationMapUiToBackend.put("7 días", "7_DAYS");
        durationMapUiToBackend.put("30 días", "30_DAYS");

        actionMapBackendToUi.put("DISMISS_REPORT", "Descartar Reporte");
        actionMapBackendToUi.put("SUSPEND_USER", "Suspender Usuario");
        actionMapBackendToUi.put("BAN_USER", "Banear Usuario");
        actionMapBackendToUi.put("DELETE_REVIEW", "Eliminar Reseña");
        actionMapBackendToUi.put("DELETE_COMMENT", "Eliminar Comentario");
        actionMapBackendToUi.put("REMOVE_PROFILE_IMAGE", "Eliminar Foto Perfil");
        actionMapBackendToUi.put("REMOVE_REVIEW_IMAGE", "Eliminar Foto Reseña");
        actionMapBackendToUi.put("REMOVE_COMMENT_IMAGE", "Eliminar Foto Comentario");
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

        adminService.getAccountStatus(authId).thenAccept(status -> {
            Platform.runLater(() -> {
                dibujarPanelDetalleUsuario(user, status, authId);
            });
        }).exceptionally(e -> null);
    }

    private void dibujarPanelDetalleUsuario(UserDto user, AccountStatusDto status, String authId) {
        Label header = new Label("Detalles de Usuario: @" + user.username());
        header.setTextFill(Color.WHITE);
        header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        String stStr = "Desconocido";
        
        if (status != null) {
            stStr = status.accountStatus();
        }
        
        Label statusLbl = new Label("Estado: " + traducirEstadoUsuario(stStr));
        statusLbl.setTextFill(Color.web(AppConstants.COLOR_ACCENT));
        
        userDetailPane.getChildren().add(header);
        userDetailPane.getChildren().add(statusLbl);

        if ("SUSPENDED".equals(stStr)) {
            if (status.suspendedUntil() != null) {
                Label suspLbl = new Label("Suspendido hasta: " + formatearFecha(status.suspendedUntil()));
                suspLbl.setTextFill(Color.ORANGE);
                userDetailPane.getChildren().add(suspLbl);
            }
        }

        HBox actions = new HBox(10);
        
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

            actions.getChildren().add(ban);
            actions.getChildren().add(dur);
            actions.getChildren().add(susp);
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
        
        return status;
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

        Label type = new Label("[" + r.targetType() + "]");
        type.setTextFill(Color.web(AppConstants.COLOR_ACCENT));
        type.setStyle("-fx-font-weight: bold;");

        Label reason = new Label(r.reason());
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

        Label header = new Label("Reporte #" + r.id() + " - " + r.status());
        header.setTextFill(Color.WHITE);
        header.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        TextArea snapArea = new TextArea();
        snapArea.setEditable(false);
        snapArea.setPrefRowCount(8);
        snapArea.setStyle("-fx-control-inner-background: #121212; -fx-text-inner-color: #00ff00; -fx-font-family: monospace;");
        
        if (r.targetSnapshot() != null) {
            snapArea.setText(new GsonBuilder().setPrettyPrinting().create().toJson(r.targetSnapshot()));
        } else {
            snapArea.setText("Sin datos adicionales");
        }

        TextArea noteArea = new TextArea();
        noteArea.setPromptText("Nota administrativa (opcional)...");
        noteArea.setPrefRowCount(2);
        noteArea.setStyle("-fx-control-inner-background: #2a2a2a; -fx-text-inner-color: white;");

        FlowPane actionsPane = new FlowPane();
        actionsPane.setHgap(10);
        actionsPane.setVgap(10);

        inyectarBotonesDeAccionReporte(r, actionsPane, noteArea);

        reportDetailPane.getChildren().add(header);
        reportDetailPane.getChildren().add(new Label("Snapshot:"));
        reportDetailPane.getChildren().add(snapArea);
        reportDetailPane.getChildren().add(noteArea);
        reportDetailPane.getChildren().add(actionsPane);
    }

    private void inyectarBotonesDeAccionReporte(AdminReportDto r, FlowPane container, TextArea noteArea) {
        if ("PENDING".equals(r.status())) {
            if (r.availableActions() != null) {
                for (String actBackend : r.availableActions()) {
                    String actUi = actionMapBackendToUi.getOrDefault(actBackend, actBackend);
                    Button btn = new Button(actUi);
                    btn.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-cursor: hand;");
                    
                    btn.setOnAction(e -> {
                        procesarClicAccionReporte(r.id(), actBackend, noteArea.getText());
                    });
                    
                    container.getChildren().add(btn);
                }
            }
        }
    }

    private void procesarClicAccionReporte(Integer id, String actionBackend, String note) {
        if ("SUSPEND_USER".equals(actionBackend)) {
            solicitarDuracionSuspension(id, actionBackend, note);
            return;
        }
        
        if ("DISMISS_REPORT".equals(actionBackend)) {
             adminService.dismissReport(String.valueOf(id), note)
                .thenRun(this::finalizarAccionExito)
                .exceptionally(e -> {
                     Platform.runLater(() -> mostrarAlertaError("Error descartando reporte."));
                     return null;
                });
             return;
        }
        
        ejecutarAccion(id, actionBackend, note, null);
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
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    
    private void mostrarAlertaExito(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Éxito");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}