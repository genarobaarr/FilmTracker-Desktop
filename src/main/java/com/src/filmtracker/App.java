package com.src.filmtracker;

import com.src.filmtracker.utils.AppConstants;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class App extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(AppConstants.FXML_DASHBOARD));
        Parent root = loader.load();
        
        Scene scene = new Scene(root);
        
        stage.initStyle(StageStyle.UNDECORATED); 
        
        stage.setMaximized(true);
        
        stage.setTitle(AppConstants.APP_TITLE);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}