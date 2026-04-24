package com.src.filmtracker;

import com.src.filmtracker.controllers.ProfileController;
import com.src.filmtracker.controllers.ShowDetailController;
import com.src.filmtracker.models.Show;
import com.src.filmtracker.models.UserDto;
import com.src.filmtracker.utils.AppConstants;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.Stack;

public class App extends Application {

    private static Scene scene;
    
    private static final Stack<Show> historyStack = new Stack<>();
    private static Show currentViewedShow = null;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(AppConstants.FXML_LOGIN));
        Parent root = loader.load();
        
        scene = new Scene(root);
        
        stage.initStyle(StageStyle.UNDECORATED); 
        stage.setMaximized(true);
        stage.setTitle(AppConstants.APP_TITLE);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
    
    public static void setRoot(String fxml) {
        if (fxml.equals(AppConstants.FXML_DASHBOARD) || fxml.equals(AppConstants.FXML_LOGIN)) {
            historyStack.clear();
            currentViewedShow = null;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource(fxml));
            Parent root = loader.load();
            scene.setRoot(root);
        } catch (IOException e) {
            System.err.println("Error crítico al intentar cargar la vista: " + fxml + " | " + e.getMessage());
        }
    }

    public static void showDetailView(Show show) {
        if (currentViewedShow != null && !currentViewedShow.tvmazeId().equals(show.tvmazeId())) {
            historyStack.push(currentViewedShow);
        }
        
        currentViewedShow = show;
        loadShowDetailTemplate(show);
    }

    public static void goBackFromDetail() {
        if (!historyStack.isEmpty()) {
            Show previousShow = historyStack.pop();
            currentViewedShow = previousShow;
            loadShowDetailTemplate(previousShow);
        } else {
            setRoot(AppConstants.FXML_DASHBOARD);
        }
    }

    private static void loadShowDetailTemplate(Show show) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource(AppConstants.FXML_SHOW_DETAIL));
            Parent root = loader.load();

            ShowDetailController controller = loader.getController();
            controller.initData(show);

            scene.setRoot(root);
        } catch (IOException e) {
            System.err.println("Error crítico al intentar cargar la vista de detalle para: " + show.name() + " | " + e.getMessage());
        }
    }

    public static void showProfileView(UserDto user) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource(AppConstants.FXML_PROFILE));
            Parent root = loader.load();

            ProfileController controller = loader.getController();
            controller.initData(user);

            scene.setRoot(root);
        } catch (IOException e) {
            System.err.println("Error al cargar vista de perfil: " + e.getMessage());
        }
    }
}