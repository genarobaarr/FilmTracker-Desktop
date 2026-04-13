package com.src.filmtracker.controllers;

import com.src.filmtracker.models.Show;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ShowCardController {

    @FXML private ImageView posterImageView;
    @FXML private Label tituloLabel;
    @FXML private Label ratingLabel;

    public void setData(Show show) {
        tituloLabel.setText(show.name());
        
        String rating = (show.rating() != null && show.rating().average() != null) 
                        ? String.valueOf(show.rating().average()) : "N/A";
        ratingLabel.setText("⭐ " + rating);

        if (show.image() != null && show.image().medium() != null) {
            Image poster = new Image(show.image().medium(), true);
            posterImageView.setImage(poster);
        }
    }
}