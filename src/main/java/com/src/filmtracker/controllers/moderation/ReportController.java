package com.src.filmtracker.controllers.moderation;

import com.src.filmtracker.models.moderation.ReportRequest;
import com.src.filmtracker.services.moderation.IModerationService;
import com.src.filmtracker.services.moderation.ModerationService;
import com.src.filmtracker.utils.AppConstants;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class ReportController {

    @FXML private ComboBox<String> reasonComboBox;
    @FXML private TextArea descriptionArea;
    @FXML private Label errorLabel;

    private final IModerationService moderationService = new ModerationService();
    
    private String targetType;
    private String targetId;

    @FXML
    public void initialize() {
        reasonComboBox.getItems().add("SPAM");
        reasonComboBox.getItems().add("OFFENSIVE_CONTENT");
        reasonComboBox.getItems().add("HARASSMENT");
        reasonComboBox.getItems().add("HATE_SPEECH");
        reasonComboBox.getItems().add("SEXUAL_CONTENT");
        reasonComboBox.getItems().add("VIOLENCE");
        reasonComboBox.getItems().add("SPOILER");
        reasonComboBox.getItems().add("FAKE_PROFILE");
        reasonComboBox.getItems().add("INAPPROPRIATE_IMAGE");
        reasonComboBox.getItems().add("OTHER");
    }

    public void initData(String type, String id) {
        this.targetType = type;
        this.targetId = id;
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) reasonComboBox.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleSubmit() {
        String reason = reasonComboBox.getValue();
        
        if (reason == null) {
            mostrarError("Debes seleccionar un motivo para el reporte.");
            return;
        }

        String description = descriptionArea.getText().trim();
        ReportRequest request = new ReportRequest(targetType, targetId, reason, description);

        moderationService.createReport(request).thenRun(() -> {
            Platform.runLater(() -> {
                mostrarAlertaExito(AppConstants.MESSAGE_SUCCESS_REPORT);
                handleClose();
            });
        }).exceptionally(e -> {
            Platform.runLater(() -> {
                manejarErrorReporte(e);
            });
            return null;
        });
    }

    private void manejarErrorReporte(Throwable e) {
        String errorMsg = e.getMessage();
        
        if (e.getCause() != null) {
            errorMsg = e.getCause().getMessage();
        }
        
        if (errorMsg != null && errorMsg.contains("409")) {
            mostrarError(AppConstants.MESSAGE_ERROR_REPORT_DUPLICATE);
        } else {
            mostrarError(AppConstants.MESSAGE_ERROR_REPORT);
        }
    }

    private void mostrarError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void mostrarAlertaExito(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Éxito");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}