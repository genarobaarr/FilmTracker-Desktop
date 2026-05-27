package com.src.filmtracker.controllers.auth;

import com.src.filmtracker.App;
import com.src.filmtracker.models.auth.LoginRequest;
import com.src.filmtracker.models.auth.RegisterRequest;
import com.src.filmtracker.services.auth.AuthService;
import com.src.filmtracker.services.auth.IAuthService;
import com.src.filmtracker.utils.AppConstants;
import com.src.filmtracker.utils.InputValidator;
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

        if (!validateInputs()) {
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
    
    private boolean validateInputs() {
        errorLabel.setVisible(false);

        if (!validarCamposLlenos()) {
            return false;
        }

        if (!validarFormatos()) {
            return false;
        }

        if (!validarContrasenas()) {
            return false;
        }

        return true;
    }

    private boolean validarCamposLlenos() {
        if (InputValidator.isNullOrEmpty(usernameField.getText()) || 
                InputValidator.isNullOrEmpty(emailField.getText()) ||
                InputValidator.isNullOrEmpty(nameField.getText()) || 
                InputValidator.isNullOrEmpty(passwordField.getText()) ||
                InputValidator.isNullOrEmpty(confirmPasswordField.getText())) 
        {
            
            showError(AppConstants.MESSAGE_ERROR_EMPTY_FIELDS);
            return false;
        }

        return true;
    }

    private boolean validarFormatos() {
        if (!InputValidator.isValidUsername(usernameField.getText())) {
            showError(AppConstants.MESSAGE_ERROR_INVALID_USERNAME);
            return false;
        }

        if (!InputValidator.isValidEmail(emailField.getText())) {
            showError(AppConstants.MESSAGE_ERROR_INVALID_EMAIL);
            return false;
        }

        return true;
    }

    private boolean validarContrasenas() {
        if (!InputValidator.isValidPassword(passwordField.getText()) || 
                !InputValidator.isValidPassword(confirmPasswordField.getText())) 
        {
            showError(AppConstants.MESSAGE_ERROR_INVALID_PASSWORD);
            return false;
        }

        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            showError(AppConstants.MESSAGE_ERROR_PASSWORD_MISMATCH);
            return false;

        }

        return true;
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
}