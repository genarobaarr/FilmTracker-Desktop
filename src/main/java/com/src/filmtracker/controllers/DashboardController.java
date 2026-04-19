package com.src.filmtracker.controllers;

import com.src.filmtracker.models.Show;
import com.src.filmtracker.services.IShowService;
import com.src.filmtracker.services.ApiService;
import com.src.filmtracker.utils.AppConstants;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML private HBox carruselDestacados;
    @FXML private HBox carruselMejorPuntuadas;
    @FXML private HBox carruselRecientes;
    @FXML private HBox carruselTerminadas;
    
    @FXML private ScrollPane scrollDestacados;
    @FXML private ScrollPane scrollMejorPuntuadas;
    @FXML private ScrollPane scrollRecientes;
    @FXML private ScrollPane scrollTerminadas;

    private final IShowService apiService;
    private static final double SCROLL_STEP = 0.3;

    public DashboardController() {
        this.apiService = new ApiService();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cargarDatosHome();
    }
    
    @FXML
    private void handleClose() {
        Platform.exit();
        System.exit(0);
    }

    @FXML
    private void handleMinimize() {
        Stage stage = (Stage) carruselDestacados.getScene().getWindow();
        stage.setIconified(true);
    }
    
    @FXML private void scrollIzqDestacados() { moverCarrusel(scrollDestacados, -SCROLL_STEP); }
    @FXML private void scrollDerDestacados() { moverCarrusel(scrollDestacados, SCROLL_STEP); }

    @FXML private void scrollIzqMejorPuntuadas() { moverCarrusel(scrollMejorPuntuadas, -SCROLL_STEP); }
    @FXML private void scrollDerMejorPuntuadas() { moverCarrusel(scrollMejorPuntuadas, SCROLL_STEP); }

    @FXML private void scrollIzqRecientes() { moverCarrusel(scrollRecientes, -SCROLL_STEP); }
    @FXML private void scrollDerRecientes() { moverCarrusel(scrollRecientes, SCROLL_STEP); }

    @FXML private void scrollIzqTerminadas() { moverCarrusel(scrollTerminadas, -SCROLL_STEP); }
    @FXML private void scrollDerTerminadas() { moverCarrusel(scrollTerminadas, SCROLL_STEP); }

    private void cargarDatosHome() {
        apiService.getHomeData().thenAccept(homeResponse -> {
            Platform.runLater(() -> {
                poblarCarrusel(homeResponse.featured(), carruselDestacados);
                poblarCarrusel(homeResponse.topRated(), carruselMejorPuntuadas);
                poblarCarrusel(homeResponse.recent(), carruselRecientes);
                poblarCarrusel(homeResponse.ended(), carruselTerminadas);
            });
        }).exceptionally(e -> {
            Platform.runLater(() -> mostrarErrorDeRed(e));
            return null;
        });
    }

    private void poblarCarrusel(List<Show> shows, HBox contenedor) {
        if (shows == null) return;
        
        for (Show show : shows) {
            agregarTarjeta(show, contenedor);
        }
    }

    private void agregarTarjeta(Show show, HBox contenedor) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(AppConstants.FXML_SHOW_CARD));
            VBox card = loader.load();
            
            ShowCardController controller = loader.getController();
            controller.setData(show);
            
            contenedor.getChildren().add(card);
        } catch (IOException e) {
            System.err.println("Error al cargar la vista de la tarjeta para: " + show.name());
        }
    }

    private void mostrarErrorDeRed(Throwable error) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error de Conexión");
        alert.setHeaderText(AppConstants.MESSAGE_ERROR_API);
        alert.setContentText("Detalles: " + error.getMessage());
        alert.showAndWait();
    }
    
    private void moverCarrusel(ScrollPane scrollPane, double cantidad) {
        double newValue = scrollPane.getHvalue() + cantidad;
        
        scrollPane.setHvalue(Math.max(0.0, Math.min(newValue, 1.0)));
    }
}