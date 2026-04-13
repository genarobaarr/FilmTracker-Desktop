package com.src.filmtracker.controllers;

import com.src.filmtracker.models.Show;
import com.src.filmtracker.services.TvMazeApiService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML private HBox carruselDestacados;
    @FXML private HBox carruselMejorPuntuadas;
    @FXML private HBox carruselRecientes;
    @FXML private HBox carruselTerminadas;

    private final TvMazeApiService apiService = new TvMazeApiService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cargarDatosHome();
    }

    private void cargarDatosHome() {
        apiService.getHomeData().thenAccept(homeResponse -> {
            Platform.runLater(() -> {
                homeResponse.featured().forEach(show -> agregarTarjeta(show, carruselDestacados));
                homeResponse.topRated().forEach(show -> agregarTarjeta(show, carruselMejorPuntuadas));
                homeResponse.recent().forEach(show -> agregarTarjeta(show, carruselRecientes));
                homeResponse.ended().forEach(show -> agregarTarjeta(show, carruselTerminadas));
            });
        }).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });
    }

    private void agregarTarjeta(Show show, HBox contenedor) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/src/filmtracker/views/FXMLShowCard.fxml"));
            VBox card = loader.load();
            
            ShowCardController controller = loader.getController();
            controller.setData(show);
            
            contenedor.getChildren().add(card);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}