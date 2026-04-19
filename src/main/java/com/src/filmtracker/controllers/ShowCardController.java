package com.src.filmtracker.controllers;

import com.src.filmtracker.models.Show;
import com.src.filmtracker.utils.AppConstants;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ShowCardController {

    @FXML private ImageView posterImageView;
    @FXML private Label tituloLabel;
    @FXML private Label ratingLabel;

    public void setData(Show show) {
        if (show == null) return; // Programación defensiva

        tituloLabel.setText(show.name() != null ? show.name() : "Desconocido");
        
        String rating = (show.rating() != null && show.rating().average() != null) 
                        ? String.valueOf(show.rating().average()) 
                        : AppConstants.MESSAGE_RATING_NA;
        
        ratingLabel.setText("⭐ " + rating);

        if (show.image() != null && show.image().medium() != null) {
            // Descarga de imagen en segundo plano (backgroundLoading = true)
            Image poster = new Image(show.image().medium(), true);
            posterImageView.setImage(poster);
        }
    }
}