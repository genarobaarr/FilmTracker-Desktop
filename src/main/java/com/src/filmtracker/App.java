package com.src.filmtracker;

import com.src.filmtracker.controllers.users.ProfileController;
import com.src.filmtracker.controllers.shows.ShowDetailController;
import com.src.filmtracker.models.shows.Show;
import com.src.filmtracker.models.users.UserDto;
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
    private static long ultimoErrorRed = 0;
    private static int fallosConsecutivosRed = 0;
    
    private static final Stack<Object> navigationHistory = new Stack<>();
    private static Object currentViewState = AppConstants.FXML_LOGIN;

    @Override
    public void start(Stage stage) throws IOException {
        com.src.filmtracker.utils.SessionManager.getInstance().setOnExpirationCallback(() -> {
            forzarCierreSesionPorExpiracion();
        });
        
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
        if (AppConstants.FXML_DASHBOARD.equals(fxml)) {
            navigationHistory.clear();
        } else {
            if (currentViewState != null) {
                if (!currentViewState.equals(fxml)) {
                    navigationHistory.push(currentViewState);
                }
            }
        }
        
        currentViewState = fxml;
        
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource(fxml));
            scene.setRoot(loader.load());
        } catch (IOException e) {
        }
    }

    public static void showShowDetail(Show show) {
        if (currentViewState != null) {
            if (currentViewState != show) {
                navigationHistory.push(currentViewState);
            }
        }
        
        currentViewState = show;
        loadShowDetailTemplate(show);
    }

    public static void showShowDetailFromProfile(Show show, UserDto profile) {
        showShowDetail(show);
    }

    public static void showProfileView(UserDto user) {
        if (currentViewState != null) {
            if (currentViewState != user) {
                navigationHistory.push(currentViewState);
            }
        }
        
        currentViewState = user;
        loadProfileTemplate(user);
    }

    public static void goBackFromDetail() {
        goBackUniversal();
    }
    
    public static void goBackUniversal() {
        if (navigationHistory.isEmpty()) {
            setRoot(AppConstants.FXML_DASHBOARD);
            return;
        }
        
        Object previousState = navigationHistory.pop();
        currentViewState = previousState;
        
        if (previousState instanceof Show) {
            loadShowDetailTemplate((Show) previousState);
            return;
        }
        
        if (previousState instanceof UserDto) {
            loadProfileTemplate((UserDto) previousState);
            return;
        }
        
        if (previousState instanceof String) {
            String fxml = (String) previousState;
            
            try {
                FXMLLoader loader = new FXMLLoader(App.class.getResource(fxml));
                scene.setRoot(loader.load());
            } catch (IOException e) {
            }
        }
    }
    
    public static void forzarCierreSesionPorExpiracion() {
        javafx.application.Platform.runLater(() -> {
            com.src.filmtracker.utils.SessionManager.getInstance().logout();
            navigationHistory.clear();
            currentViewState = AppConstants.FXML_LOGIN;
            
            try {
                FXMLLoader loader = new FXMLLoader(App.class.getResource(AppConstants.FXML_LOGIN));
                scene.setRoot(loader.load());
            } catch (IOException e) {
            }
            
            com.src.filmtracker.utils.CustomAlertHelper.mostrarInformacion(AppConstants.MESSAGE_ERROR_SESSION_EXPIRED);
        });
    }
    
    public static void handleNetworkError() {
        javafx.application.Platform.runLater(() -> {
            if (AppConstants.FXML_DASHBOARD.equals(currentViewState)) {
                com.src.filmtracker.utils.CustomAlertHelper.mostrarError("Error de conexión con el servicio solicitado. Operación interrumpida.");
                return;
            }
            
            setRoot(AppConstants.FXML_DASHBOARD);
            com.src.filmtracker.utils.CustomAlertHelper.mostrarError(AppConstants.MESSAGE_ERROR_SERVER_DOWN_HOME);
        });
    }

    public static boolean procesarErrorCritico(Throwable e) {
        if (e == null) {
            return false;
        }
        
        String errorStr = e.toString().toLowerCase();
        
        boolean fallaRed = errorStr.contains("connectexception") || 
                           errorStr.contains("connection refused") || 
                           errorStr.contains("timeout") || 
                           errorStr.contains("httptimeoutexception") || 
                           errorStr.contains("502") || 
                           errorStr.contains("503");
                           
        if (fallaRed) {
            long tiempoActual = System.currentTimeMillis();
            
            if (tiempoActual - ultimoErrorRed > 2000) {
                if (tiempoActual - ultimoErrorRed > 60000) {
                    fallosConsecutivosRed = 1;
                } else {
                    fallosConsecutivosRed++;
                }
                
                ultimoErrorRed = tiempoActual;
                
                if (fallosConsecutivosRed >= 3) {
                    fallosConsecutivosRed = 0;
                    javafx.application.Platform.runLater(() -> ejecutarCaidaCritica());
                } else {
                    handleNetworkError();
                }
            }
            
            return true;
        }
        
        return false;
    }

    private static void ejecutarCaidaCritica() {
        com.src.filmtracker.utils.SessionManager.getInstance().logout();
        navigationHistory.clear();
        currentViewState = AppConstants.FXML_LOGIN;
        
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource(AppConstants.FXML_LOGIN));
            scene.setRoot(loader.load());
        } catch (IOException e) {
        }
        
        com.src.filmtracker.utils.CustomAlertHelper.mostrarError(AppConstants.MESSAGE_ERROR_SERVER_DOWN_LOGIN);
    }

    private static void loadShowDetailTemplate(Show show) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource(AppConstants.FXML_SHOW_DETAIL));
            Parent root = loader.load();

            ShowDetailController controller = loader.getController();
            controller.initData(show);

            scene.setRoot(root);
        } catch (IOException e) {
        }
    }

    private static void loadProfileTemplate(UserDto user) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource(AppConstants.FXML_PROFILE));
            Parent root = loader.load();

            ProfileController controller = loader.getController();
            controller.initData(user);

            scene.setRoot(root);
        } catch (IOException e) {
        }
    }
}