package com.src.filmtracker.controllers.auth;

import com.src.filmtracker.App;
import com.src.filmtracker.models.auth.ResetPasswordRequest;
import com.src.filmtracker.services.auth.AuthService;
import com.src.filmtracker.services.auth.IAuthService;
import com.src.filmtracker.utils.AppConstants;
import com.src.filmtracker.utils.CustomAlertHelper;
import com.src.filmtracker.utils.InputValidator;
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

        if (!validateInputs()) {
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
    
    private boolean validateInputs() {
        errorLabel.setVisible(false);

        if (InputValidator.isNullOrEmpty(tokenField.getText())) {
            mostrarError(AppConstants.MESSAGE_ERROR_EMPTY_FIELDS);
            return false;
        }

        if (!InputValidator.isValidPassword(passwordField.getText())) {
            mostrarError(AppConstants.MESSAGE_ERROR_INVALID_PASSWORD);
            return false;
        }

        if (!passwordField.getText().equals(confirmField.getText())) {
            mostrarError(AppConstants.MESSAGE_ERROR_PASSWORD_MISMATCH);
            return false;
        }

        return true;
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
