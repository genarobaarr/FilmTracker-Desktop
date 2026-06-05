package com.src.filmtracker.utils;

import com.src.filmtracker.App;
import com.src.filmtracker.controllers.utils.CustomAlertController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

public class CustomAlertHelper {

    private static final String ICON_SUCCESS = "✔️";
    private static final String ICON_ERROR = "❌";
    private static final String ICON_INFO = "ℹ️️";
    private static final String ICON_WARNING = "⚠️";

    private CustomAlertHelper() {
        // Constructor privado para clase utilitaria
    }

    public static void mostrarExito(String mensaje) {
        mostrarModal("Éxito", mensaje, ICON_SUCCESS, false, null);
    }

    public static void mostrarError(String mensaje) {
        mostrarModal("Error", mensaje, ICON_ERROR, false, null);
    }
    
    public static void mostrarInformacion(String mensaje) {
        mostrarModal("Información", mensaje, ICON_INFO, false, null);
    }
    
    public static boolean mostrarPrecaucion(String titulo, String mensaje, String btnConfirmar, String btnCancelar, Window owner) {
        return buildAndShowModal(titulo, mensaje, ICON_WARNING, true, btnConfirmar, btnCancelar, owner);
    }

    public static boolean mostrarConfirmacion(String titulo, String mensaje, Window owner) {
        return mostrarModal(titulo, mensaje, ICON_WARNING, true, owner);
    }

    private static boolean mostrarModal(String titulo, String mensaje, String icono, boolean esConfirmacion, Window owner) {
        return buildAndShowModal(titulo, mensaje, icono, esConfirmacion, null, null, owner);
    }

    private static boolean buildAndShowModal(String titulo, String mensaje, String icono, boolean esConfirmacion, String btnConfirmar, String btnCancelar, Window owner) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource(AppConstants.FXML_CUSTOM_ALERT));
            Parent root = loader.load();
            
            CustomAlertController controller = loader.getController();
            controller.configurarAlerta(titulo, mensaje, icono, esConfirmacion);
            
            if (btnConfirmar != null || btnCancelar != null) {
                controller.configurarTextosBotones(btnConfirmar, btnCancelar);
            }
            
            Stage stage = new Stage();
            stage.initStyle(StageStyle.UNDECORATED);
            stage.initModality(Modality.APPLICATION_MODAL);
            
            if (owner != null) {
                stage.initOwner(owner);
            }
            
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.showAndWait();
            
            return controller.isConfirmed();
        } catch (Exception e) {
            // Falla silenciosa intencional: Si falla la carga del FXML o la renderización, 
            // abortamos de forma segura devolviendo false (no confirmado)
            return false;
        }
    }
}