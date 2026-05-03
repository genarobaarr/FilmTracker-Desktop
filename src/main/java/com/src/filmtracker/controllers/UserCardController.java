package com.src.filmtracker.controllers;

import com.src.filmtracker.App;
import com.src.filmtracker.models.UserDto;
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

        try {
            Image avatar = new Image(imageUrl, true);
            avatarImageView.setImage(avatar);
        } catch (Exception e) {
        }
    }
}