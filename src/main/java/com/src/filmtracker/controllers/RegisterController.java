package com.src.filmtracker.controllers;

import com.src.filmtracker.App;
import com.src.filmtracker.models.LoginRequest;
import com.src.filmtracker.models.RegisterRequest;
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

public class RegisterController {
    @FXML private TextField usernameField;
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label errorLabel;

    private final IAuthService authService = new AuthService();

    @FXML private void handleClose() { 
        Platform.exit(); System.exit(0); 
    }
    @FXML private void handleMinimize() { 
        ((Stage)nameField.getScene().getWindow()).setIconified(true); 
    }
    @FXML private void goToLogin() { 
        App.setRoot(AppConstants.FXML_LOGIN); 
    }

    @FXML
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String pass = passwordField.getText().trim();
        String confirmPass = confirmPasswordField.getText().trim();

        if (username.isEmpty() || name.isEmpty() || email.isEmpty() || pass.isEmpty() || confirmPass.isEmpty()) {
            showError(AppConstants.MESSAGE_ERROR_FIELDS);
            return;
        }
        
        if (!username.matches("^[a-zA-Z0-9]+$")) {
            showError(AppConstants.MESSAGE_ERROR_USERNAME_INVALID);
            return;
        }

        if (!pass.equals(confirmPass)) {
            showError(AppConstants.MESSAGE_ERROR_PASSWORD_MISMATCH);
            return;
        }

        RegisterRequest regRequest = new RegisterRequest(username, name, email, pass);
        
        authService.register(regRequest).thenCompose(regResponse -> {
            LoginRequest loginReq = new LoginRequest(email, pass);
            return authService.login(loginReq);
        }).thenAccept(authResponse -> {
            Platform.runLater(() -> {
                SessionManager.getInstance().login(authResponse);
                App.setRoot(AppConstants.FXML_DASHBOARD); 
            });
        }).exceptionally(e -> {
            Platform.runLater(() -> showError(AppConstants.MESSAGE_ERROR_REG_FAILED));
            return null;
        });
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
}