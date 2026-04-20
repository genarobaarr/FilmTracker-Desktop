package com.src.filmtracker.controllers;

import com.src.filmtracker.App;
import com.src.filmtracker.models.Show;
import com.src.filmtracker.utils.AppConstants;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class ShowCardController {

    @FXML private VBox cardContainer;
    @FXML private ImageView posterImageView;
    @FXML private Label tituloLabel;
    @FXML private Label ratingLabel;
    
    private Show showData;

    @FXML
    private void onCardClicked() {
        App.showDetailView(showData);
    }

    public void setData(Show show) {
        if (show == null) return;
        
        this.showData = show;

        tituloLabel.setText(show.name() != null ? show.name() : "Desconocido");
        
        String rating = (show.rating() != null && show.rating().average() != null) 
                        ? String.valueOf(show.rating().average()) 
                        : AppConstants.MESSAGE_RATING_NA;
        
        ratingLabel.setText("⭐ " + rating);

        if (show.image() != null && show.image().medium() != null) {
            Image poster = new Image(show.image().medium(), true);
            posterImageView.setImage(poster);
        }
    }
}