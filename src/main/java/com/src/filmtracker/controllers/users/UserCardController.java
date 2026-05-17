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

    @FXML private VBox cardContainer;
    @FXML private ImageView avatarImageView;
    @FXML private Label nameLabel;
    @FXML private Label usernameLabel;

    private UserDto userData;
    private Image fallbackErrorImage;

    public UserCardController() {
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
            nameLabel.setText("Desconocido");
        }

        if (user.username() != null) {
            usernameLabel.setText("@" + user.username());
        } else {
            usernameLabel.setText("@usuario");
        }

        String nameParam = "User";
        
        if (user.username() != null) {
            nameParam = user.username();
        }

        String imageUrl = "https://ui-avatars.com/api/?name=" + nameParam + "&background=e50914&color=fff";
        
        if (user.profileImage() != null) {
            if (!user.profileImage().isEmpty()) {
                imageUrl = user.profileImage();
            }
        }

        cargarImagenConRespaldo(imageUrl, avatarImageView);
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