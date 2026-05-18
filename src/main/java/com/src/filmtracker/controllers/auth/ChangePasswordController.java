package com.src.filmtracker.controllers.auth;

import com.src.filmtracker.App;
import com.src.filmtracker.models.auth.ChangePasswordRequest;
import com.src.filmtracker.services.auth.AuthService;
import com.src.filmtracker.services.auth.IAuthService;
import com.src.filmtracker.utils.AppConstants;
import com.src.filmtracker.utils.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

public class ChangePasswordController {

    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label errorLabel;

    private final IAuthService authService = new AuthService();
    
    @FXML
    private void handleBack() {
        App.goBackUniversal();
    }
    
    @FXML private void handleMinimize() { 
        ((Stage) errorLabel.getScene().getWindow()).setIconified(true); 
    }
    
    @FXML private void handleClose() { 
        Platform.exit(); System.exit(0); 
    }

    @FXML
    private void handleUpdate() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        String current = currentPasswordField.getText();
        String nuevo = newPasswordField.getText();
        String confirm = confirmPasswordField.getText();

        if (current.isEmpty() || nuevo.isEmpty() || confirm.isEmpty()) {
            mostrarMensajeError(AppConstants.MESSAGE_ERROR_FIELDS);
            return;
        }

        if (!nuevo.equals(confirm)) {
            mostrarMensajeError(AppConstants.MESSAGE_ERROR_PASSWORD_MISMATCH);
            return;
        }

        procesarCambio(current, nuevo);
    }

    private void procesarCambio(String current, String nuevo) {
        ChangePasswordRequest request = new ChangePasswordRequest(current, nuevo);

        authService.changePassword(request).thenAccept(res -> {
            Platform.runLater(() -> {
                mostrarAlertaExito();
                App.setRoot(AppConstants.FXML_DASHBOARD);
            });
        }).exceptionally(e -> {
            Platform.runLater(() -> {
                if (e.getMessage().contains("401")) {
                    mostrarMensajeError(AppConstants.MESSAGE_ERROR_PASSWORD_CURRENT);
                } else {
                    mostrarMensajeError(AppConstants.MESSAGE_ERROR_API);
                }
            });
            return null;
        });
    }

    private void mostrarMensajeError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void mostrarAlertaExito() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Éxito");
        alert.setHeaderText(null);
        alert.setContentText(AppConstants.MESSAGE_SUCCESS_PASSWORD);
        alert.showAndWait();
    }
}