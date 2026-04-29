package com.src.filmtracker.controllers;

import com.src.filmtracker.App;
import com.src.filmtracker.models.UserDto;
import com.src.filmtracker.utils.AppConstants;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ProfileController {
    @FXML private ImageView avatarView;
    @FXML private Label nameLabel, usernameLabel, emailLabel, roleLabel, dateLabel;
    
    @FXML private void handleBack() { 
        App.setRoot(AppConstants.FXML_DASHBOARD); 
    }

    public void initData(UserDto user) {
        nameLabel.setText(user.name());
        usernameLabel.setText("@" + user.username());
        emailLabel.setText(user.email());
        roleLabel.setText(user.role());
        
        if (user.createdAt() != null) {
            ZonedDateTime dt = ZonedDateTime.parse(user.createdAt());
            dateLabel.setText("Miembro desde: " + dt.format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
        }

        String imageUrl = (user.profileImage() != null && !user.profileImage().isEmpty()) 
                ? user.profileImage() 
                : "https://ui-avatars.com/api/?name=" + user.username() + "&background=e50914&color=fff";
        
        avatarView.setImage(new Image(imageUrl, true));
    }
}