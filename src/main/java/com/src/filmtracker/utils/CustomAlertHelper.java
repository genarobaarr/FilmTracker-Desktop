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

    private CustomAlertHelper() {
    }

    public static void mostrarExito(String mensaje) {
        mostrarModal("Éxito", mensaje, "✔️", false, null);
    }

    public static void mostrarError(String mensaje) {
        mostrarModal("Error", mensaje, "❌", false, null);
    }
    
    public static void mostrarInformacion(String mensaje) {
        mostrarModal("Información", mensaje, "ℹ️️", false, null);
    }
    
    public static boolean mostrarPrecaucion(String titulo, String mensaje, String btnConfirmar, String btnCancelar, Window owner) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/com/src/filmtracker/views/FXMLCustomAlert.fxml"));
            Parent root = loader.load();
            
            CustomAlertController controller = loader.getController();
            controller.configurarAlerta(titulo, mensaje, "⚠️", true);
            controller.configurarTextosBotones(btnConfirmar, btnCancelar);
            
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
            return false;
        }
    }

    public static boolean mostrarConfirmacion(String titulo, String mensaje, Window owner) {
        return mostrarModal(titulo, mensaje, "⚠️", true, owner);
    }

    private static boolean mostrarModal(String titulo, String mensaje, String icono, boolean esConfirmacion, Window owner) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/com/src/filmtracker/views/FXMLCustomAlert.fxml"));
            Parent root = loader.load();
            
            CustomAlertController controller = loader.getController();
            controller.configurarAlerta(titulo, mensaje, icono, esConfirmacion);
            
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
            return false;
        }
    }
}