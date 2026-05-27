package com.src.filmtracker.controllers.moderation;

import com.src.filmtracker.models.moderation.ReportRequest;
import com.src.filmtracker.services.moderation.IModerationService;
import com.src.filmtracker.services.moderation.ModerationService;
import com.src.filmtracker.utils.AppConstants;
import com.src.filmtracker.utils.CustomAlertHelper;
import com.src.filmtracker.utils.InputValidator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.util.LinkedHashMap;
import java.util.Map;

public class ReportController {

    @FXML private ComboBox<String> reasonComboBox;
    @FXML private TextArea descriptionArea;
    @FXML private Label errorLabel;

    private final IModerationService moderationService = new ModerationService();
    private final Map<String, String> reasonMap = new LinkedHashMap<>();
    
    private String targetType;
    private String targetId;

    @FXML
    public void initialize() {
        reasonMap.put("Spam o contenido comercial no deseado", "SPAM");
        reasonMap.put("Contenido ofensivo o abusivo", "OFFENSIVE_CONTENT");
        reasonMap.put("Acoso o intimidación", "HARASSMENT");
        reasonMap.put("Incitación al odio o discriminación", "HATE_SPEECH");
        reasonMap.put("Contenido sexual o explícito", "SEXUAL_CONTENT");
        reasonMap.put("Violencia o daño físico", "VIOLENCE");
        reasonMap.put("Spoiler sin advertencia previa", "SPOILER");
        reasonMap.put("Perfil falso o suplantación de identidad", "FAKE_PROFILE");
        reasonMap.put("Imagen de perfil o contenido inapropiado", "INAPPROPRIATE_IMAGE");
        reasonMap.put("Otro motivo", "OTHER");

        for (String key : reasonMap.keySet()) {
            reasonComboBox.getItems().add(key);
        }
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
        String selectedReasonText = reasonComboBox.getValue();
        
        if (selectedReasonText == null) {
            mostrarError("Debes seleccionar un motivo para el reporte.");
            return;
        }
        
        if (!validateInputs()) {
            return;
        }

        String reasonCode = reasonMap.get(selectedReasonText);
        String description = descriptionArea.getText().trim();
                
        ReportRequest request = new ReportRequest(targetType, targetId, reasonCode, description);

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
    
    private boolean validateInputs() {
        errorLabel.setVisible(false);

        if (reasonComboBox.getValue() == null) {
            mostrarError(AppConstants.MESSAGE_ERROR_SELECT_OPTION);
            return false;
        }

        if (descriptionArea != null) {
            if (InputValidator.exceedsMaxLength(descriptionArea.getText(), 250)) {
                mostrarError(AppConstants.MESSAGE_ERROR_MAX_LENGTH);
                return false;
            }
        }

        return true;
    }

    private void manejarErrorReporte(Throwable e) {
        String errorMsg = e.getMessage();
        
        if (e.getCause() != null) {
            errorMsg = e.getCause().getMessage();
        }
        
        if (errorMsg != null) {
            if (errorMsg.contains("409")) {
                mostrarError(AppConstants.MESSAGE_ERROR_REPORT_DUPLICATE);
                return;
            }
        }
        
        mostrarError(AppConstants.MESSAGE_ERROR_REPORT);
    }

    private void mostrarError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void mostrarAlertaExito(String mensaje) {
        CustomAlertHelper.mostrarExito(mensaje);
    }
}