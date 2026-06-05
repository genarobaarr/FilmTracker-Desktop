package com.src.filmtracker.controllers.shows;

import com.src.filmtracker.App;
import com.src.filmtracker.models.shows.Show;
import com.src.filmtracker.utils.AppConstants;
import com.src.filmtracker.utils.ImageHelper;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class ShowCardController {

    private static final String TEXT_DESCONOCIDO = "Desconocido";

    @FXML private VBox cardContainer;
    @FXML private ImageView posterImageView;
    @FXML private Label tituloLabel;
    @FXML private Label ratingLabel;
    
    private Show showData;

    public ShowCardController() {
        // Constructor por defecto
    }

    @FXML
    public void initialize() {
        // La carga en caché de se delegó a ImageHelper
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
            tituloLabel.setText(TEXT_DESCONOCIDO);
        }
        
        String rating = AppConstants.MESSAGE_RATING_NA;
        
        if (show.rating() != null && show.rating().average() != null) {
            rating = String.valueOf(show.rating().average());
        }
        
        ratingLabel.setText("⭐ " + rating);

        if (show.image() != null && show.image().medium() != null) {
            ImageHelper.cargarImagenConRespaldo(show.image().medium(), posterImageView);
        }
    }
}