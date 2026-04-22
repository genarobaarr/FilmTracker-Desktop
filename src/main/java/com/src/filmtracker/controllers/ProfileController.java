package com.src.filmtracker.controllers;

import com.src.filmtracker.App;
import com.src.filmtracker.models.UserDto;
import com.src.filmtracker.utils.AppConstants;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ProfileController {
    @FXML private ImageView avatarView;
    @FXML private Label nameLabel, emailLabel, roleLabel;

    public void initData(UserDto user) {
        nameLabel.setText(user.name() != null ? user.name() : "Sin nombre");
        emailLabel.setText(user.email());
        roleLabel.setText(user.role());

        if (user.profileImage() != null && !user.profileImage().isEmpty()) {
            avatarView.setImage(new Image(user.profileImage(), true));
        } else {
            avatarView.setImage(new Image("https://ui-avatars.com/api/?name=" + user.email() + "&background=e50914&color=fff", true));
        }
    }

    @FXML
    private void handleBack() {
        App.setRoot(AppConstants.FXML_DASHBOARD);
    }
}