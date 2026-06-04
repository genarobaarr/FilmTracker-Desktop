package com.src.filmtracker.controllers.notifications;

import com.src.filmtracker.App;
import com.src.filmtracker.models.notifications.NotificationDto;
import com.src.filmtracker.models.notifications.NotificationResponse;
import com.src.filmtracker.models.shows.Show;
import com.src.filmtracker.services.notifications.INotificationService;
import com.src.filmtracker.services.notifications.NotificationService;
import com.src.filmtracker.services.shows.IShowService;
import com.src.filmtracker.services.shows.ShowService;
import com.src.filmtracker.services.users.IUserService;
import com.src.filmtracker.services.users.UserService;
import com.src.filmtracker.utils.AppConstants;
import com.src.filmtracker.utils.CustomAlertHelper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class NotificationsController {

    private static final String STYLE_CARD_READ = "-fx-background-color: #1a1a1a; -fx-padding: 15; -fx-background-radius: 8; -fx-border-color: #333; -fx-cursor: hand;";
    private static final String STYLE_CARD_UNREAD = "-fx-background-color: #2a1a1a; -fx-padding: 15; -fx-background-radius: 8; -fx-border-color: #e50914; -fx-border-width: 0 0 0 4; -fx-cursor: hand;";
    private static final String STYLE_TITLE = "-fx-font-weight: bold; -fx-font-size: 15px;";
    private static final String STYLE_DATE = "-fx-font-size: 11px;";
    private static final String STYLE_DEL_BTN = "-fx-background-color: transparent; -fx-text-fill: #e50914; -fx-cursor: hand;";
    private static final String STYLE_MORE_BTN = "-fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-cursor: hand;";
    private static final String URL_AVATAR_BASE = "https://ui-avatars.com/api/?name=";
    private static final String URL_AVATAR_PARAMS = "&background=e50914&color=fff";
    private static final String DEFAULT_USER = "User";

    @FXML private VBox notificationsContainer;

    private final INotificationService notificationService = new NotificationService();
    private final IShowService showService = new ShowService();
    private final IUserService userService = new UserService();
    
    private int currentPage = 1;

    public NotificationsController() {
        // Constructor por defecto
    }

    @FXML
    public void initialize() {
        cargarNotificaciones(1);
    }
    
    @FXML 
    private void handleBack() { 
        App.setRoot(AppConstants.FXML_DASHBOARD); 
    }
    
    @FXML 
    private void handleMinimize() { 
        Stage stage = (Stage) notificationsContainer.getScene().getWindow();
        stage.setIconified(true); 
    }
    
    @FXML 
    private void handleClose() { 
        Platform.exit(); 
        System.exit(0); 
    }
    
    @FXML
    private void handleMarkAllRead() {
        notificationService.markAllAsRead().thenRun(() -> 
            Platform.runLater(() -> {
                mostrarAlertaExito(AppConstants.MESSAGE_SUCCESS_READ_ALL);
                cargarNotificaciones(1);
            })
        ).exceptionally(e -> {
            Platform.runLater(() -> {
                if (!App.procesarErrorCritico(e)) {
                    mostrarAlertaError(AppConstants.MESSAGE_ERROR_NOTIFICATIONS);
                }
            });
            return null;
        });
    }

    private void cargarNotificaciones(int page) {
        notificationService.getNotifications(page)
            .thenAccept(res -> Platform.runLater(() -> procesarRespuesta(res, page)))
            .exceptionally(e -> {
                Platform.runLater(() -> {
                    if (!App.procesarErrorCritico(e)) {
                        mostrarAlertaError(AppConstants.MESSAGE_ERROR_NOTIFICATIONS);
                    }
                });
                return null;
            });
    }

    private void procesarRespuesta(NotificationResponse res, int page) {
        if (page == 1) {
            notificationsContainer.getChildren().clear();
        } else {
            removerBotonCargarMas();
        }

        if (res == null || res.data() == null) {
            return;
        }

        if (res.data().isEmpty()) {
            if (page == 1) {
                Label lbl = new Label("No tienes notificaciones.");
                lbl.setTextFill(Color.GRAY);
                notificationsContainer.getChildren().add(lbl);
            }
            return;
        }

        for (NotificationDto notif : res.data()) {
            notificationsContainer.getChildren().add(construirTarjeta(notif));
        }

        evaluarBotonCargarMas(res);
    }

    private HBox construirTarjeta(NotificationDto notif) {
        HBox card = new HBox(15);
        card.setAlignment(Pos.CENTER_LEFT);
        
        boolean[] isReadState = {notif.isRead()};
        aplicarEstiloLectura(card, isReadState[0]);

        ImageView imageView = new ImageView();
        imageView.setFitWidth(40);
        imageView.setFitHeight(60);
        imageView.setPreserveRatio(true);

        VBox contentBox = new VBox(5);
        HBox.setHgrow(contentBox, Priority.ALWAYS);

        Label titleLbl = new Label(notif.title());
        titleLbl.setTextFill(Color.WHITE);
        titleLbl.setStyle(STYLE_TITLE);

        Label bodyLbl = new Label(notif.body());
        bodyLbl.setTextFill(Color.LIGHTGRAY);
        bodyLbl.setWrapText(true);

        Label dateLbl = new Label(formatearFecha(notif.createdAt()));
        dateLbl.setTextFill(Color.GRAY);
        dateLbl.setStyle(STYLE_DATE);
        
        cargarDatosAdicionales(notif, imageView, bodyLbl);

        contentBox.getChildren().addAll(titleLbl, bodyLbl, dateLbl);

        Button deleteBtn = new Button("🗑");
        deleteBtn.setStyle(STYLE_DEL_BTN);

        configurarEventosTarjeta(card, deleteBtn, notif, isReadState);

        card.getChildren().addAll(imageView, contentBox, deleteBtn);

        return card;
    }
    
    private void aplicarEstiloLectura(HBox card, boolean isRead) {
        if (isRead) {
            card.setStyle(STYLE_CARD_READ);
        } else {
            card.setStyle(STYLE_CARD_UNREAD);
        }
    }

    private void cargarDatosAdicionales(NotificationDto notif, ImageView imageView, Label bodyLbl) {
        if (notif.metadata() != null && notif.metadata().tvmazeId() != null) {
            showService.getShowDetails(notif.metadata().tvmazeId()).thenAccept(show -> 
                Platform.runLater(() -> actualizarDatosSerie(show, imageView, bodyLbl, notif.type()))
            ).exceptionally(e -> {
                App.procesarErrorCritico(e);
                return null;
            });
            return;
        }
        
        if (notif.actorAuthId() != null && esValidaParaFotoPerfil(notif.type())) {
            userService.getUserById(notif.actorAuthId()).thenAccept(user -> 
                Platform.runLater(() -> actualizarDatosUsuario(user, imageView))
            ).exceptionally(e -> {
                App.procesarErrorCritico(e);
                return null;
            });
        }
    }

    private boolean esValidaParaFotoPerfil(String type) {
        return type != null && !type.startsWith("moderation") && !type.contains("photo");
    }

    private void actualizarDatosUsuario(com.src.filmtracker.models.users.UserDto user, ImageView imageView) {
        if (user == null) {
            return;
        }
        
        String nameParam = user.username() != null ? user.username() : DEFAULT_USER;
        String imageUrl = URL_AVATAR_BASE + nameParam + URL_AVATAR_PARAMS;
        
        if (user.profileImage() != null && !user.profileImage().isEmpty()) {
            imageUrl = user.profileImage();
        }
        
        try {
            imageView.setImage(new Image(imageUrl, true));
            imageView.setFitHeight(40);
            imageView.setFitWidth(40);
        } catch (Exception e) {
            // Falla de renderizado controlada por JavaFX estáticamente
        }
    }

    private void procesarRedireccion(NotificationDto notif) {
        if (notif.type() != null && (notif.type().startsWith("moderation.") || notif.type().contains("photo"))) {
            return;
        }

        if (notif.metadata() != null && notif.metadata().tvmazeId() != null) {
            abrirDetalleSerie(notif.metadata().tvmazeId());
            return;
        }

        if (notif.actorAuthId() != null) {
            abrirPerfilUsuario(notif.actorAuthId());
        }
    }
    
    private void actualizarDatosSerie(Show show, ImageView imageView, Label bodyLbl, String type) {
        if (show == null) {
            return;
        }
        
        if (show.image() != null && show.image().medium() != null) {
            imageView.setImage(new Image(show.image().medium(), true));
        }
        
        if (show.name() != null && type != null && type.contains("library")) {
            String oldText = bodyLbl.getText();
            bodyLbl.setText(oldText + "\nSerie: " + show.name());
        }
    }

    private void configurarEventosTarjeta(HBox card, Button deleteBtn, NotificationDto notif, boolean[] isReadState) {
        deleteBtn.setOnAction(e -> {
            e.consume();
            notificationService.deleteNotification(notif.id()).thenRun(() -> 
                Platform.runLater(() -> cargarNotificaciones(1))
            );
        });

        card.setOnMouseClicked(e -> {
            if (!isReadState[0]) {
                notificationService.markAsRead(notif.id()).thenRun(() -> 
                    Platform.runLater(() -> {
                        isReadState[0] = true;
                        aplicarEstiloLectura(card, true);
                        procesarRedireccion(notif);
                    })
                );
                return;
            }
            
            procesarRedireccion(notif);
        });
    }

    private void abrirDetalleSerie(Integer tvmazeId) {
        showService.getShowDetails(tvmazeId).thenAccept(show -> 
            Platform.runLater(() -> {
                if (show != null) {
                    App.showShowDetail(show);
                }
            })
        ).exceptionally(e -> {
            App.procesarErrorCritico(e);
            return null;
        });
    }

    private void abrirPerfilUsuario(String authId) {
        userService.getUserById(authId).thenAccept(user -> 
            Platform.runLater(() -> {
                if (user != null) {
                    App.showProfileView(user);
                }
            })
        ).exceptionally(e -> {
            App.procesarErrorCritico(e);
            return null;
        });
    }

    private void evaluarBotonCargarMas(NotificationResponse res) {
        if (res.pagination() != null && Boolean.TRUE.equals(res.pagination().hasNextPage())) {
            Button btn = new Button("Cargar más");
            btn.setStyle(STYLE_MORE_BTN);
            
            btn.setOnAction(e -> {
                currentPage++;
                cargarNotificaciones(currentPage);
            });
            
            HBox box = new HBox(btn);
            box.setAlignment(Pos.CENTER);
            notificationsContainer.getChildren().add(box);
        }
    }

    private void removerBotonCargarMas() {
        if (!notificationsContainer.getChildren().isEmpty()) {
            int lastIndex = notificationsContainer.getChildren().size() - 1;
            
            if (notificationsContainer.getChildren().get(lastIndex) instanceof HBox) {
                notificationsContainer.getChildren().remove(lastIndex);
            }
        }
    }

    private String formatearFecha(String iso) {
        try {
            ZonedDateTime zdt = ZonedDateTime.parse(iso);
            return zdt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        } catch (Exception e) {
            return iso;
        }
    }

    private void mostrarAlertaError(String mensaje) {
        CustomAlertHelper.mostrarError(mensaje);
    }
    
    private void mostrarAlertaExito(String mensaje) {
        CustomAlertHelper.mostrarExito(mensaje);
    }
}