package com.src.filmtracker.utils;

import com.src.filmtracker.controllers.moderation.ReportController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public final class ReportModalHelper {

    private ReportModalHelper() {
    }

    public static void openReportModal(String targetType, String targetId) {
        try {
            FXMLLoader loader = new FXMLLoader(ReportModalHelper.class.getResource(AppConstants.FXML_REPORT));
            Parent root = loader.load();
            
            ReportController controller = loader.getController();
            controller.initData(targetType, targetId);
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (Exception e) {
            // Falla silenciosa intencional al intentar cargar o renderizar el componente visual del modal
        }
    }
}