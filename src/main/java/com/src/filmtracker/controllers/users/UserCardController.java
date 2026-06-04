package com.src.filmtracker.controllers.users;

import com.src.filmtracker.App;
import com.src.filmtracker.models.users.UserDto;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class UserCardController {

    private static final String TEXT_DESCONOCIDO = "Desconocido";
    private static final String TEXT_USUARIO_DEF = "@usuario";
    private static final String NAME_PARAM_DEF = "User";
    private static final String AVATAR_API_BASE = "https://ui-avatars.com/api/?background=e50914&color=fff&name=";

    @FXML private VBox cardContainer;
    @FXML private ImageView avatarImageView;
    @FXML private Label nameLabel;
    @FXML private Label usernameLabel;

    private UserDto userData;
    private Image fallbackErrorImage;

    public UserCardController() {
        // Constructor por defecto
    }

    @FXML
    public void initialize() {
        cargarImagenErrorEnCache();
    }

    @FXML
    private void onCardClicked() {
        App.showProfileView(userData);
    }

    public void setData(UserDto user) {
        if (user == null) {
            return;
        }

        this.userData = user;

        if (user.name() != null) {
            nameLabel.setText(user.name());
        } else {
            nameLabel.setText(TEXT_DESCONOCIDO);
        }

        if (user.username() != null) {
            usernameLabel.setText("@" + user.username());
        } else {
            usernameLabel.setText(TEXT_USUARIO_DEF);
        }

        String nameParam = NAME_PARAM_DEF;
        
        if (user.username() != null) {
            nameParam = user.username();
        }

        String imageUrl = AVATAR_API_BASE + nameParam;
        
        if (user.profileImage() != null && !user.profileImage().isEmpty()) {
            imageUrl = user.profileImage();
        }

        cargarImagenConRespaldo(imageUrl, avatarImageView);
    }

    private void cargarImagenConRespaldo(String url, ImageView imageView) {
        try {
            Image img = new Image(url, true);
            
            img.errorProperty().addListener((obs, oldVal, isError) -> {
                if (Boolean.TRUE.equals(isError)) {
                    Platform.runLater(() -> ponerImagenError(imageView));
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
            // Falla silenciosa intencional: Si no se encuentra la imagen de error en el sistema de archivos, 
            // el ImageView simplemente quedará en blanco de forma segura.
        }
    }

    private void ponerImagenError(ImageView imageView) {
        if (this.fallbackErrorImage != null) {
            imageView.setImage(this.fallbackErrorImage);
        }
    }
}