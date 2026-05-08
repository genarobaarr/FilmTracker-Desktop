package com.src.filmtracker.controllers;

import com.src.filmtracker.App;
import com.src.filmtracker.models.ResendVerificationRequest;
import com.src.filmtracker.models.VerifyEmailRequest;
import com.src.filmtracker.services.AuthService;
import com.src.filmtracker.services.IAuthService;
import com.src.filmtracker.utils.AppConstants;
import com.src.filmtracker.utils.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class VerifyEmailController {
    
    @FXML private TextField codeField;
    @FXML private Label errorLabel;
    @FXML private Label successLabel;

    private final IAuthService authService = new AuthService();

    @FXML 
    private void handleClose() { 
        Platform.exit(); 
        System.exit(0); 
    }

    @FXML 
    private void handleMinimize() { 
        Stage stage = (Stage) codeField.getScene().getWindow();
        stage.setIconified(true); 
    }

    @FXML 
    private void handleBack() { 
        App.setRoot(AppConstants.FXML_DASHBOARD); 
    }

    @FXML
    private void handleVerify() {
        ocultarMensajes();
        
        String code = codeField.getText().trim();

        if (code.isEmpty()) {
            mostrarError(AppConstants.MESSAGE_ERROR_CODE_EMPTY);
            return;
        }

        if (SessionManager.getInstance().getCurrentUser() == null) {
            App.setRoot(AppConstants.FXML_LOGIN);
            return;
        }

        String email = SessionManager.getInstance().getCurrentUser().email();
        VerifyEmailRequest request = new VerifyEmailRequest(email, code);
        
        authService.verifyEmail(request).thenAccept(response -> {
            Platform.runLater(() -> {
                procesarExitoVerificacion(response);
            });
        }).exceptionally(e -> {
            Platform.runLater(() -> {
                mostrarError(AppConstants.MESSAGE_ERROR_API);
            });
            
            return null;
        });
    }
    
    private void procesarExitoVerificacion(com.src.filmtracker.models.ProfileResponse response) {
        if (response != null) {
            if (response.data() != null) {
                SessionManager.getInstance().updateUser(response.data());
                mostrarExito(AppConstants.MESSAGE_SUCCESS_VERIFIED);
                
                new Thread(() -> {
                    try {
                        Thread.sleep(2000);
                        Platform.runLater(() -> {
                            App.setRoot(AppConstants.FXML_DASHBOARD);
                        });
                    } catch (InterruptedException ex) {
                    }
                }).start();
            }
        }
    }

    @FXML
    private void handleResend() {
        ocultarMensajes();
        
        if (SessionManager.getInstance().getCurrentUser() == null) {
            App.setRoot(AppConstants.FXML_LOGIN);
            return;
        }

        String email = SessionManager.getInstance().getCurrentUser().email();
        ResendVerificationRequest request = new ResendVerificationRequest(email);

        authService.resendVerification(request).thenAccept(response -> {
            Platform.runLater(() -> {
                mostrarExito(AppConstants.MESSAGE_SUCCESS_RESEND);
            });
        }).exceptionally(e -> {
            Platform.runLater(() -> {
                mostrarError(AppConstants.MESSAGE_ERROR_API);
            });
            
            return null;
        });
    }

    private void mostrarError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void mostrarExito(String message) {
        successLabel.setText(message);
        successLabel.setVisible(true);
        successLabel.setManaged(true);
    }

    private void ocultarMensajes() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        
        successLabel.setVisible(false);
        successLabel.setManaged(false);
    }
}