package com.src.filmtracker.controllers.auth;

import com.src.filmtracker.App;
import com.src.filmtracker.models.auth.ForgotPasswordRequest;
import com.src.filmtracker.services.auth.AuthService;
import com.src.filmtracker.services.auth.IAuthService;
import com.src.filmtracker.utils.AppConstants;
import com.src.filmtracker.utils.InputValidator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ForgotPasswordController {
    
    @FXML private TextField emailField;
    @FXML private Label errorLabel;
    
    private final IAuthService authService = new AuthService();

    @FXML
    private void handleSend() {
        String email = emailField.getText().trim();
        
        if (!validateInputs()) {
            return;
        }
        
        authService.forgotPassword(new ForgotPasswordRequest(email)).thenAccept(res -> {
            Platform.runLater(() -> {
                App.setRoot(AppConstants.FXML_RESET_PASSWORD);
            });
        }).exceptionally(e -> {
            Platform.runLater(() -> mostrarError(AppConstants.MESSAGE_ERROR_API));
            return null;
        });
    }

    @FXML private void handleBack() { 
        App.setRoot(AppConstants.FXML_LOGIN); 
    }
    
    @FXML private void handleMinimize() { 
        ((Stage) emailField.getScene().getWindow()).setIconified(true); 
    }
    
    @FXML private void handleClose() { 
        Platform.exit(); System.exit(0); 
    }
    
    private boolean validateInputs() {
        errorLabel.setVisible(false);

        if (InputValidator.isNullOrEmpty(emailField.getText())) {
            mostrarError(AppConstants.MESSAGE_ERROR_EMPTY_FIELDS);
            return false;
        }

        if (!InputValidator.isValidEmail(emailField.getText())) {
            mostrarError(AppConstants.MESSAGE_ERROR_INVALID_EMAIL);
            return false;

        }

        return true;
    }

    private void mostrarError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
}