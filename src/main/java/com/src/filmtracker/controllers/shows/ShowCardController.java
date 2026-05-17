package com.src.filmtracker.controllers.shows;

import com.src.filmtracker.App;
import com.src.filmtracker.models.shows.Show;
import com.src.filmtracker.utils.AppConstants;
import javafx.application.Platform;
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
    private Image fallbackErrorImage;

    public ShowCardController() {
    }

    @FXML
    public void initialize() {
        cargarImagenErrorEnCache();
    }

    @FXML
    private void onCardClicked() {
        App.showShowDetail(showData);
    }

    public void setData(Show show) {
        if (show == null) {
            return;
        }
        
        this.showData = show;

        if (show.name() != null) {
            tituloLabel.setText(show.name());
        } else {
            tituloLabel.setText("Desconocido");
        }
        
        String rating = AppConstants.MESSAGE_RATING_NA;
        
        if (show.rating() != null) {
            if (show.rating().average() != null) {
                rating = String.valueOf(show.rating().average());
            }
        }
        
        ratingLabel.setText("⭐ " + rating);

        if (show.image() != null) {
            if (show.image().medium() != null) {
                cargarImagenConRespaldo(show.image().medium(), posterImageView);
            }
        }
    }

    private void cargarImagenConRespaldo(String url, ImageView imageView) {
        try {
            Image img = new Image(url, true);
            
            img.errorProperty().addListener((obs, oldVal, isError) -> {
                if (isError) {
                    Platform.runLater(() -> {
                        ponerImagenError(imageView);
                    });
                }
            });
            
            imageView.setImage(img);
        } catch (Exception e) {
            ponerImagenError(imageView);
        }
    }

    private void cargarImagenErrorEnCache() {
        try {
            String errorPath = getClass().getResource("/com/src/filmtracker/images/error.png").toExternalForm();
            this.fallbackErrorImage = new Image(errorPath, true);
        } catch (Exception ex) {
        }
    }

    private void ponerImagenError(ImageView imageView) {
        if (this.fallbackErrorImage != null) {
            imageView.setImage(this.fallbackErrorImage);
        }
    }
}