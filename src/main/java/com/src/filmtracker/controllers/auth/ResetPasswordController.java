package com.src.filmtracker.controllers.auth;

import com.src.filmtracker.App;
import com.src.filmtracker.models.auth.ResetPasswordRequest;
import com.src.filmtracker.services.auth.AuthService;
import com.src.filmtracker.services.auth.IAuthService;
import com.src.filmtracker.utils.AppConstants;
import com.src.filmtracker.utils.CustomAlertHelper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class ResetPasswordController {
    
    @FXML private TextField tokenField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmField;
    @FXML private Label errorLabel;
    private final IAuthService authService = new AuthService();

    @FXML
    private void handleReset() {
        String token = tokenField.getText().trim();
        String pass = passwordField.getText();
        String confirm = confirmField.getText();

        if (token.isEmpty() || pass.isEmpty()) {
            mostrarError(AppConstants.MESSAGE_ERROR_FIELDS);
            return;
        }
        if (!pass.equals(confirm)) {
            mostrarError(AppConstants.MESSAGE_ERROR_PASSWORD_MISMATCH);
            return;
        }

        authService.resetPassword(new ResetPasswordRequest(token, pass)).thenAccept(res -> {
            Platform.runLater(() -> {
                mostrarExito();
                App.setRoot(AppConstants.FXML_LOGIN);
            });
        }).exceptionally(e -> {
            Platform.runLater(() -> mostrarError(AppConstants.MESSAGE_ERROR_API));
            return null;
        });
    }
    
    @FXML private void handleBack() { 
        App.setRoot(AppConstants.FXML_LOGIN); 
    }

    private void mostrarError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void mostrarExito() {
        CustomAlertHelper.mostrarInformacion(AppConstants.MESSAGE_SUCCESS_RESET);
    }
}