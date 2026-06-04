package com.src.filmtracker.controllers.utils;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class CustomAlertController {

    @FXML private Label iconHeaderLabel;
    @FXML private Label titleLabel;
    @FXML private Label contentLabel;
    @FXML private HBox buttonsContainer;
    @FXML private Button btnCancel;
    @FXML private Button btnConfirm;

    private boolean confirmed;

    public CustomAlertController() {
        this.confirmed = false;
    }

    @FXML
    public void initialize() {
        // Método requerido por FXML, intencionalmente vacío
    }

    @FXML
    private void handleClose() {
        cerrarVentana();
    }

    @FXML
    private void handleCancelAction() {
        this.confirmed = false;
        cerrarVentana();
    }

    @FXML
    private void handleConfirmAction() {
        this.confirmed = true;
        cerrarVentana();
    }

    public void configurarAlerta(String titulo, String mensaje, String icono, boolean esConfirmacion) {
        if (titleLabel != null) {
            titleLabel.setText(titulo);
        }
        
        if (contentLabel != null) {
            contentLabel.setText(mensaje);
        }
        
        if (iconHeaderLabel != null) {
            iconHeaderLabel.setText(icono);
        }
        
        if (btnCancel != null) {
            btnCancel.setVisible(esConfirmacion);
            btnCancel.setManaged(esConfirmacion);
        }
    }
    
    public void configurarTextosBotones(String textoConfirmar, String textoCancelar) {
        if (btnConfirm != null && textoConfirmar != null) {
            btnConfirm.setText(textoConfirmar);
        }
        
        if (btnCancel != null && textoCancelar != null) {
            btnCancel.setText(textoCancelar);
        }
    }

    public boolean isConfirmed() {
        return this.confirmed;
    }

    private void cerrarVentana() {
        Stage stage = (Stage) titleLabel.getScene().getWindow();
        
        if (stage != null) {
            stage.close();
        }
    }
}