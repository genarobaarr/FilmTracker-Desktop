package com.src.filmtracker.controllers;

import com.src.filmtracker.App;
import com.src.filmtracker.models.LoginRequest;
import com.src.filmtracker.services.AuthService;
import com.src.filmtracker.services.IAuthService;
import com.src.filmtracker.utils.AppConstants;
import com.src.filmtracker.utils.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private final IAuthService authService = new AuthService();

    @FXML private void handleClose() { 
        Platform.exit(); System.exit(0); 
    }
    @FXML private void handleMinimize() { 
        ((Stage)emailField.getScene().getWindow()).setIconified(true); 
    }
    @FXML private void goToRegister() { 
        App.setRoot(AppConstants.FXML_REGISTER); 
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String pass = passwordField.getText().trim();

        if (email.isEmpty() || pass.isEmpty()) {
            showError(AppConstants.MESSAGE_ERROR_FIELDS);
            return;
        }

        LoginRequest request = new LoginRequest(email, pass);
        
        authService.login(request).thenAccept(response -> {
            Platform.runLater(() -> {
                SessionManager.getInstance().login(response);
                App.setRoot(AppConstants.FXML_DASHBOARD);
            });
        }).exceptionally(e -> {
            Platform.runLater(() -> showError(AppConstants.MESSAGE_ERROR_AUTH));
            System.err.println("Login Failed: " + e.getMessage());
            return null;
        });
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
}