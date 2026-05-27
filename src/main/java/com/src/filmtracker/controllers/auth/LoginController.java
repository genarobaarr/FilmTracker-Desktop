package com.src.filmtracker.controllers.auth;

import com.src.filmtracker.App;
import com.src.filmtracker.models.auth.AccountModeratedException;
import com.src.filmtracker.models.auth.LoginRequest;
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

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class LoginController {
    
    @FXML private TextField emailField; 
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private final IAuthService authService;

    public LoginController() {
        this.authService = new AuthService();
    }

    @FXML
    public void initialize() {
    }

    @FXML 
    private void handleClose() { 
        Platform.exit(); 
        System.exit(0); 
    }
    
    @FXML 
    private void handleMinimize() { 
        Stage stage = (Stage) emailField.getScene().getWindow();
        stage.setIconified(true); 
    }
    
    @FXML 
    private void goToRegister() { 
        App.setRoot(AppConstants.FXML_REGISTER); 
    }
    
    @FXML
    private void handleForgotPassword() {
        App.setRoot(AppConstants.FXML_FORGOT_PASSWORD);
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String pass = passwordField.getText().trim();

        if (!validateInputs()) {
            return;
        }

        LoginRequest request = new LoginRequest(email, pass);
        
        authService.login(request).thenAccept(response -> {
            Platform.runLater(() -> {
                SessionManager.getInstance().login(response);
                App.setRoot(AppConstants.FXML_DASHBOARD);
            });
        }).exceptionally(e -> {
            Platform.runLater(() -> {
                procesarErrorLogin(e);
            });
            return null;
        });
    }

    private void procesarErrorLogin(Throwable e) {
        Throwable cause = e.getCause();

        if (cause instanceof AccountModeratedException) {
            AccountModeratedException modEx = (AccountModeratedException) cause;
            manejarCuentaModerada(modEx);
            return;
        }

        showError(AppConstants.MESSAGE_ERROR_AUTH);
    }

    private void manejarCuentaModerada(AccountModeratedException modEx) {
        String status = modEx.getAccountStatus();

        if ("BANNED".equals(status)) {
            mostrarErrorBaneo(modEx);
            return;
        }

        if ("SUSPENDED".equals(status)) {
            mostrarErrorSuspension(modEx);
            return;
        }

        showError("❌ " + modEx.getMessage());
    }

    private void mostrarErrorBaneo(AccountModeratedException modEx) {
        showError("❌ " + modEx.getMessage());
    }

    private void mostrarErrorSuspension(AccountModeratedException modEx) {
        String untilStr = modEx.getSuspendedUntil();

        if (untilStr == null) {
            showError("⏳ " + modEx.getMessage());
            return;
        }

        if (untilStr.isEmpty()) {
            showError("⏳ " + modEx.getMessage());
            return;
        }

        construirMensajeSuspensionDetallado(modEx, untilStr);
    }

    private void construirMensajeSuspensionDetallado(AccountModeratedException modEx, String untilStr) {
        try {
            ZonedDateTime fechaSuspension = ZonedDateTime.parse(untilStr);
            ZonedDateTime fechaActual = ZonedDateTime.now();
            long diasRestantes = ChronoUnit.DAYS.between(fechaActual, fechaSuspension);

            if (diasRestantes <= 0) {
                calcularHorasRestantes(modEx, fechaSuspension, fechaActual);
                return;
            }

            String fechaFormateada = fechaSuspension.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            String mensaje = "⏳ " + modEx.getMessage() + "\nDisponible el " + fechaFormateada + " (Faltan " + diasRestantes + " días)";
            
            showError(mensaje);
        } catch (Exception ex) {
            showError("⏳ " + modEx.getMessage());
        }
    }

    private void calcularHorasRestantes(AccountModeratedException modEx, ZonedDateTime fechaSuspension, ZonedDateTime fechaActual) {
        long horasRestantes = ChronoUnit.HOURS.between(fechaActual, fechaSuspension);
        
        if (horasRestantes <= 0) {
            horasRestantes = 1;
        }
        
        String fechaFormateada = fechaSuspension.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        String mensaje = "⏳ " + modEx.getMessage() + "\nDisponible el " + fechaFormateada + " (Faltan aprox. " + horasRestantes + " horas)";
        
        showError(mensaje);
    }
    
    private boolean validateInputs() {
        errorLabel.setVisible(false);

        if (InputValidator.isNullOrEmpty(emailField.getText())) {
            showError(AppConstants.MESSAGE_ERROR_EMPTY_FIELDS);
            return false;
        }

        if (InputValidator.isNullOrEmpty(passwordField.getText())) {
            showError(AppConstants.MESSAGE_ERROR_EMPTY_FIELDS);
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