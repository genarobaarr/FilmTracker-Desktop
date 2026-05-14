package com.src.filmtracker.controllers.shows;

import com.src.filmtracker.App;
import com.src.filmtracker.controllers.users.UserCardController;
import com.src.filmtracker.models.shows.Show;
import com.src.filmtracker.models.users.UserDto;
import com.src.filmtracker.services.notifications.INotificationService;
import com.src.filmtracker.services.notifications.NotificationService;
import com.src.filmtracker.services.shows.IShowService;
import com.src.filmtracker.services.users.IUserService;
import com.src.filmtracker.services.shows.ShowService;
import com.src.filmtracker.services.users.UserService;
import com.src.filmtracker.utils.AppConstants;
import com.src.filmtracker.utils.SessionManager;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class DashboardController implements Initializable {

    @FXML private HBox carruselDestacados;
    @FXML private HBox carruselMejorPuntuadas;
    @FXML private HBox carruselRecientes;
    @FXML private HBox carruselTerminadas;
    
    @FXML private TextField searchField;
    @FXML private VBox resultadosContainer;
    @FXML private HBox carruselResultados;
    @FXML private Label labelResultados;
    
    @FXML private ScrollPane scrollResultados;
    @FXML private ScrollPane scrollDestacados;
    @FXML private ScrollPane scrollMejorPuntuadas;
    @FXML private ScrollPane scrollRecientes;
    @FXML private ScrollPane scrollTerminadas;
    
    @FXML private Label unreadBadgeLabel;

    private final IShowService showService;
    private final IUserService userService;
    private final INotificationService notificationService = new NotificationService();
    private static final double SCROLL_STEP = 0.3;

    public DashboardController() {
        this.showService = new ShowService();
        this.userService = new UserService();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cargarDatosHome();
        cargarConteoNotificaciones();
    }
    
    @FXML
    private void handleSearch() {
        String query = searchField.getText().trim();

        if (query.isEmpty()) {
            resultadosContainer.setVisible(false);
            resultadosContainer.setManaged(false);
            return;
        }

        String userQuery = query.replace(" ", "_");

        CompletableFuture<List<Show>> showsFuture = showService.searchShows(query)
            .exceptionally(e -> {
                return new ArrayList<>();
            });

        CompletableFuture<UserDto> userFuture = userService.getUserByUsername(userQuery)
            .exceptionally(e -> {
                return null;
            });

        CompletableFuture.allOf(showsFuture, userFuture).thenAccept(v -> {
            Platform.runLater(() -> {
                procesarResultadosBusqueda(query, showsFuture, userFuture);
            });
        });
    }

    @FXML
    private void handleVerPerfil() {
        userService.getProfile().thenAccept(user -> {
            Platform.runLater(() -> {
                App.showProfileView(user);
            });
        }).exceptionally(e -> {
            return null;
        });
    }
    
    @FXML 
    private void handleOpenFriendsManager() {
        App.setRoot(AppConstants.FXML_FRIENDS_MANAGER);
    }
    
    @FXML
    private void handleOpenMyReports() {
        App.setRoot(AppConstants.FXML_MY_REPORTS);
    }
    
    @FXML
    private void handleOpenNotifications() {
        App.setRoot(AppConstants.FXML_NOTIFICATIONS);
    }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        
        App.setRoot(AppConstants.FXML_LOGIN);
    }
    
    @FXML
    private void handleClose() {
        Platform.exit();
        System.exit(0);
    }

    @FXML
    private void handleMinimize() {
        Stage stage = (Stage) carruselDestacados.getScene().getWindow();
        stage.setIconified(true);
    }
    
    @FXML
    private void scrollIzqResultados() {
        moverCarrusel(scrollResultados, -SCROLL_STEP);
    }

    @FXML
    private void scrollDerResultados() {
        moverCarrusel(scrollResultados, SCROLL_STEP);
    }
    
    @FXML
    private void scrollIzqDestacados() {
        moverCarrusel(scrollDestacados, -SCROLL_STEP);
    }

    @FXML
    private void scrollDerDestacados() {
        moverCarrusel(scrollDestacados, SCROLL_STEP);
    }

    @FXML
    private void scrollIzqMejorPuntuadas() {
        moverCarrusel(scrollMejorPuntuadas, -SCROLL_STEP);
    }

    @FXML
    private void scrollDerMejorPuntuadas() {
        moverCarrusel(scrollMejorPuntuadas, SCROLL_STEP);
    }

    @FXML
    private void scrollIzqRecientes() {
        moverCarrusel(scrollRecientes, -SCROLL_STEP);
    }

    @FXML
    private void scrollDerRecientes() {
        moverCarrusel(scrollRecientes, SCROLL_STEP);
    }

    @FXML
    private void scrollIzqTerminadas() {
        moverCarrusel(scrollTerminadas, -SCROLL_STEP);
    }

    @FXML
    private void scrollDerTerminadas() {
        moverCarrusel(scrollTerminadas, SCROLL_STEP);
    }
    
    @FXML
    private void handleOpenLeaderboards() {
        App.setRoot(AppConstants.FXML_LEADERBOARDS);
    }

    private void procesarResultadosBusqueda(String query, CompletableFuture<List<Show>> showsFuture, CompletableFuture<UserDto> userFuture) {
        carruselResultados.getChildren().clear();
        scrollResultados.setHvalue(0.0);

        UserDto user = null;
        try {
            user = userFuture.join();
        } catch (Exception ex) {
        }

        List<Show> shows = null;
        try {
            shows = showsFuture.join();
        } catch (Exception ex) {
        }

        boolean foundAnything = iterarResultados(user, shows);

        if (!foundAnything) {
            labelResultados.setText("No se encontraron resultados para: " + query);
        } else {
            labelResultados.setText("Resultados para: " + query);
        }

        resultadosContainer.setVisible(true);
        resultadosContainer.setManaged(true);
    }

    private boolean iterarResultados(UserDto user, List<Show> shows) {
        boolean found = false;

        if (user != null) {
            if (user.id() != null) {
                agregarTarjetaUsuario(user, carruselResultados);
                found = true;
            }
        }

        if (shows != null) {
            if (!shows.isEmpty()) {
                for (Show show : shows) {
                    agregarTarjeta(show, carruselResultados);
                }
                found = true;
            }
        }

        return found;
    }
    
    private void cargarConteoNotificaciones() {
        notificationService.getUnreadCount().thenAccept(res -> {
            Platform.runLater(() -> {
                actualizarBadgeNotificaciones(res);
            });
        }).exceptionally(e -> null);
    }

    private void actualizarBadgeNotificaciones(com.src.filmtracker.models.notifications.UnreadCountResponse res) {
        if (res != null) {
            if (res.unreadCount() != null) {
                if (res.unreadCount() > 0) {
                    unreadBadgeLabel.setText(String.valueOf(res.unreadCount()));
                    unreadBadgeLabel.setVisible(true);
                    unreadBadgeLabel.setManaged(true);
                    return;
                }
            }
        }
        
        unreadBadgeLabel.setVisible(false);
        unreadBadgeLabel.setManaged(false);
    }

    private void cargarDatosHome() {
        showService.getHomeData().thenAccept(homeResponse -> {
            Platform.runLater(() -> {
                poblarCarrusel(homeResponse.featured(), carruselDestacados);
                poblarCarrusel(homeResponse.topRated(), carruselMejorPuntuadas);
                poblarCarrusel(homeResponse.recent(), carruselRecientes);
                poblarCarrusel(homeResponse.ended(), carruselTerminadas);
            });
        }).exceptionally(e -> {
            Platform.runLater(() -> {
                mostrarErrorDeRed(e);
            });
            return null;
        });
    }

    private void poblarCarrusel(List<Show> shows, HBox contenedor) {
        if (shows == null) {
            return;
        }
        
        for (Show show : shows) {
            agregarTarjeta(show, contenedor);
        }
    }

    private void agregarTarjeta(Show show, HBox contenedor) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(AppConstants.FXML_SHOW_CARD));
            VBox card = loader.load();
            
            ShowCardController controller = loader.getController();
            controller.setData(show);
            
            contenedor.getChildren().add(card);
        } catch (IOException e) {
        }
    }

    private void agregarTarjetaUsuario(UserDto user, HBox contenedor) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(AppConstants.FXML_USER_CARD));
            VBox card = loader.load();

            UserCardController controller = loader.getController();
            controller.setData(user);

            contenedor.getChildren().add(card);
        } catch (IOException e) {
        }
    }

    private void mostrarErrorDeRed(Throwable error) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error de Conexión");
        alert.setHeaderText(AppConstants.MESSAGE_ERROR_API);
        alert.setContentText("Detalles: " + error.getMessage());
        alert.showAndWait();
    }
    
    private void moverCarrusel(ScrollPane scrollPane, double cantidad) {
        double newValue = scrollPane.getHvalue() + cantidad;
        
        scrollPane.setHvalue(Math.max(0.0, Math.min(newValue, 1.0)));
    }
}