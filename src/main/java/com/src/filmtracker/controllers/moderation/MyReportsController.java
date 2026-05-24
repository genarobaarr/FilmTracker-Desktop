package com.src.filmtracker.controllers.moderation;

import com.src.filmtracker.App;
import com.src.filmtracker.models.moderation.MyReportsResponse;
import com.src.filmtracker.models.moderation.ReportDto;
import com.src.filmtracker.services.moderation.IModerationService;
import com.src.filmtracker.services.moderation.ModerationService;
import com.src.filmtracker.utils.AppConstants;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class MyReportsController {

    @FXML private VBox reportsContainer;

    private final IModerationService moderationService = new ModerationService();
    private int currentPage = 1;

    @FXML
    public void initialize() {
        cargarReportes(1);
    }
    
    @FXML private void handleBack() { 
        App.setRoot(AppConstants.FXML_DASHBOARD); 
    }
    
    @FXML private void handleMinimize() { 
        ((Stage) reportsContainer.getScene().getWindow()).setIconified(true); 
    }
    
    @FXML private void handleClose() { 
        Platform.exit(); System.exit(0); 
    }

    private void cargarReportes(int page) {
        moderationService.getMyReports(page).thenAccept(res -> {
            Platform.runLater(() -> {
                procesarRespuesta(res, page);
            });
        }).exceptionally(e -> {
            Platform.runLater(() -> {
                if (!App.procesarErrorCritico(e)) {
                    mostrarVacio("No se pudieron cargar tus reportes.");
                }
            });
            return null;
        });
    }

    private void procesarRespuesta(MyReportsResponse res, int page) {
        if (page == 1) {
            reportsContainer.getChildren().clear();
        } else {
            removerBotonCargarMas();
        }

        if (res == null) {
            manejarListaVacia(page);
            return;
        }

        if (res.reports() == null) {
            manejarListaVacia(page);
            return;
        }

        if (res.reports().isEmpty()) {
            manejarListaVacia(page);
            return;
        }

        for (ReportDto report : res.reports()) {
            reportsContainer.getChildren().add(buildReportCard(report));
        }

        evaluarBotonCargarMas(res);
    }

    private void manejarListaVacia(int page) {
        if (page == 1) {
            mostrarVacio("No has emitido ningún reporte aún.");
        }
    }

    private VBox buildReportCard(ReportDto report) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: #1e1e1e; -fx-padding: 15; -fx-background-radius: 8; -fx-border-color: #333;");

        Label header = new Label("Reporte #" + report.id() + " - " + report.targetType());
        header.setTextFill(Color.web(AppConstants.COLOR_ACCENT));
        header.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label reason = new Label("Motivo: " + traducirMotivo(report.reason()));
        reason.setTextFill(Color.WHITE);
        
        Label status = new Label("Estado: " + traducirEstado(report.status()));
        status.setTextFill(obtenerColorEstado(report.status()));
        status.setStyle("-fx-font-weight: bold;");

        Label desc = new Label(report.description() != null ? report.description() : "Sin descripción.");
        desc.setTextFill(Color.LIGHTGRAY);
        desc.setWrapText(true);

        Label date = new Label("Fecha: " + formatearFecha(report.createdAt()));
        date.setTextFill(Color.GRAY);
        date.setStyle("-fx-font-size: 11px;");

        card.getChildren().addAll(header, reason, status, desc, date);
        
        inyectarNotaAdminSiAplica(report, card);
        
        return card;
    }

    private void inyectarNotaAdminSiAplica(ReportDto report, VBox card) {
        if ("PENDING".equals(report.status())) {
            return;
        }
        
        if (report.adminNote() != null) {
            if (!report.adminNote().trim().isEmpty()) {
                VBox noteBox = new VBox(5);
                noteBox.setStyle("-fx-background-color: #2a2a2a; -fx-padding: 10; -fx-background-radius: 5; -fx-border-color: #4caf50; -fx-border-width: 0 0 0 4;");
                
                Label noteTitle = new Label("Nota del Administrador:");
                noteTitle.setTextFill(Color.web("#4caf50"));
                noteTitle.setStyle("-fx-font-weight: bold;");
                
                Label noteContent = new Label(report.adminNote());
                noteContent.setTextFill(Color.WHITE);
                noteContent.setWrapText(true);
                
                noteBox.getChildren().add(noteTitle);
                noteBox.getChildren().add(noteContent);
                
                card.getChildren().add(noteBox);
            }
        }
    }

    private String traducirMotivo(String code) {
        if (code == null) {
            return "Otro";
        }
        
        switch (code) {
            case "SPAM": return "Spam o contenido comercial no deseado";
            case "OFFENSIVE_CONTENT": return "Contenido ofensivo o abusivo";
            case "HARASSMENT": return "Acoso o intimidación";
            case "HATE_SPEECH": return "Incitación al odio o discriminación";
            case "SEXUAL_CONTENT": return "Contenido sexual o explícito";
            case "VIOLENCE": return "Violencia o daño físico";
            case "SPOILER": return "Spoiler sin advertencia previa";
            case "FAKE_PROFILE": return "Perfil falso o suplantación de identidad";
            case "Imagen de perfil o contenido inapropiado": return "Imagen inapropiada";
            default: return "Otro motivo";
        }
    }

    private String traducirEstado(String status) {
        if ("PENDING".equals(status)) {
            return "Pendiente";
        }
        
        if ("DISMISSED".equals(status)) {
            return "Descartado";
        }
        
        if ("ACTION_TAKEN".equals(status)) {
            return "Acción Tomada";
        }
        
        return status;
    }

    private Color obtenerColorEstado(String status) {
        if ("PENDING".equals(status)) {
            return Color.YELLOW;
        }
        
        if ("ACTION_TAKEN".equals(status)) {
            return Color.LIGHTGREEN;
        }
        
        return Color.GRAY;
    }

    private String formatearFecha(String iso) {
        try {
            ZonedDateTime zdt = ZonedDateTime.parse(iso);
            return zdt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        } catch (Exception e) {
            return iso;
        }
    }

    private void evaluarBotonCargarMas(MyReportsResponse res) {
        if (res.pagination() == null) {
            return;
        }

        if (res.pagination().hasNextPage() != null) {
            if (res.pagination().hasNextPage()) {
                Button btn = new Button("Cargar más");
                btn.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-cursor: hand;");
                
                btn.setOnAction(e -> {
                    currentPage++;
                    cargarReportes(currentPage);
                });
                
                HBox box = new HBox(btn);
                box.setAlignment(Pos.CENTER);
                reportsContainer.getChildren().add(box);
            }
        }
    }

    private void removerBotonCargarMas() {
        if (!reportsContainer.getChildren().isEmpty()) {
            int lastIndex = reportsContainer.getChildren().size() - 1;
            if (reportsContainer.getChildren().get(lastIndex) instanceof HBox) {
                reportsContainer.getChildren().remove(lastIndex);
            }
        }
    }

    private void mostrarVacio(String msj) {
        Label lbl = new Label(msj);
        lbl.setTextFill(Color.GRAY);
        reportsContainer.getChildren().clear();
        reportsContainer.getChildren().add(lbl);
    }
}