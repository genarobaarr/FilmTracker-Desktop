package com.src.filmtracker.controllers;

import com.src.filmtracker.App;
import com.src.filmtracker.models.*;
import com.src.filmtracker.services.ApiService;
import com.src.filmtracker.services.IShowService;
import com.src.filmtracker.utils.AppConstants;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ShowDetailController {

    @FXML private Label titleLabel, ratingLabel, statusLabel, genresLabel, summaryLabel;
    @FXML private ImageView posterDetail;
    @FXML private HBox castContainer;
    @FXML private VBox episodesContainer;
    @FXML private ScrollPane scrollCast;

    private final IShowService apiService = new ApiService();
    private static final double SCROLL_STEP = 0.3;
    
    @FXML 
    private void scrollIzqCast() { 
        moverCarrusel(scrollCast, -SCROLL_STEP); 
    }
    
    @FXML 
    private void scrollDerCast() { 
        moverCarrusel(scrollCast, SCROLL_STEP); 
    }

    @FXML
    private void handleBack() {
        App.setRoot(AppConstants.FXML_DASHBOARD);
    }

    public void initData(Show basicShow) {
        cargarDatosBasicos(basicShow);

        apiService.getFullShowDetails(basicShow.tvmazeId()).thenAccept(fullData -> {
            Platform.runLater(() -> {
                cargarDatosBasicos(fullData.show()); 
                
                castContainer.getChildren().clear();
                scrollCast.setHvalue(0.0);
                
                castContainer.getChildren().clear();
                for (CastDto member : fullData.cast()) {
                    castContainer.getChildren().add(createPersonBox(member));
                }

                episodesContainer.getChildren().clear();
                for (SeasonDto season : fullData.seasons()) {
                    episodesContainer.getChildren().add(createSeasonRow(season));
                }
            });
        }).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });
    }

    private void cargarDatosBasicos(Show show) {
        titleLabel.setText(show.name() != null ? show.name() : "Desconocido");
        statusLabel.setText("Status: " + (show.status() != null ? show.status() : "N/A"));
        
        if (show.genres() != null) {
            genresLabel.setText("Géneros: " + String.join(", ", show.genres()));
        }
        
        String rating = (show.rating() != null && show.rating().average() != null) 
                ? String.valueOf(show.rating().average()) : AppConstants.MESSAGE_RATING_NA;
        ratingLabel.setText("⭐ " + rating);

        if (show.summary() != null) {
            summaryLabel.setText(show.summary().replaceAll("<[^>]*>", ""));
        }

        if (show.image() != null && show.image().original() != null) {
            posterDetail.setImage(new Image(show.image().original(), true));
        }
    }

    private VBox createPersonBox(CastDto member) {
        VBox box = new VBox(5);
        box.setAlignment(javafx.geometry.Pos.CENTER);
        
        ImageView iv = new ImageView();
        iv.setFitHeight(150);
        iv.setFitWidth(110);
        if (member.person().image() != null && member.person().image().medium() != null) {
            iv.setImage(new Image(member.person().image().medium(), true));
        }
        
        Label name = new Label(member.person().name());
        name.setTextFill(javafx.scene.paint.Color.WHITE);
        name.setStyle("-fx-font-weight: bold;");
        
        Label character = new Label(member.character().name());
        character.setTextFill(javafx.scene.paint.Color.GRAY);
        
        box.getChildren().addAll(iv, name, character);
        return box;
    }

    private HBox createSeasonRow(SeasonDto season) {
        HBox row = new HBox(15);
        row.setStyle("-fx-background-color: #1e1e1e; -fx-padding: 10; -fx-background-radius: 5;");
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Label number = new Label("Temp " + season.number());
        number.setTextFill(javafx.scene.paint.Color.web("#e50914"));
        number.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        number.setMinWidth(70);
        
        Label info = new Label(season.episodeOrder() + " Episodios | " + 
                              (season.premiereDate() != null ? season.premiereDate() : "N/A"));
        info.setTextFill(javafx.scene.paint.Color.WHITE);
        
        row.getChildren().addAll(number, info);
        return row;
    }

    private void moverCarrusel(ScrollPane scrollPane, double cantidad) {
        double newValue = scrollPane.getHvalue() + cantidad;
        scrollPane.setHvalue(Math.max(0.0, Math.min(newValue, 1.0)));
    }
}