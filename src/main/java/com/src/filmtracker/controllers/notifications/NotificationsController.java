package com.src.filmtracker.controllers.notifications;

import com.src.filmtracker.App;
import com.src.filmtracker.controllers.shows.ShowDetailController;
import com.src.filmtracker.controllers.users.ProfileController;
import com.src.filmtracker.models.notifications.NotificationDto;
import com.src.filmtracker.models.notifications.NotificationResponse;
import com.src.filmtracker.services.notifications.INotificationService;
import com.src.filmtracker.services.notifications.NotificationService;
import com.src.filmtracker.services.shows.IShowService;
import com.src.filmtracker.services.shows.ShowService;
import com.src.filmtracker.services.users.IUserService;
import com.src.filmtracker.services.users.UserService;
import com.src.filmtracker.utils.AppConstants;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class NotificationsController {

    @FXML private VBox notificationsContainer;

    private final INotificationService notificationService = new NotificationService();
    private final IShowService showService = new ShowService();
    private final IUserService userService = new UserService();
    private int currentPage = 1;

    @FXML
    public void initialize() {
        cargarNotificaciones(1);
    }
    
    @FXML private void handleBack() { App.setRoot(AppConstants.FXML_DASHBOARD); }
    @FXML private void handleMinimize() { ((Stage) notificationsContainer.getScene().getWindow()).setIconified(true); }
    @FXML private void handleClose() { Platform.exit(); System.exit(0); }
    
    @FXML
    private void handleMarkAllRead() {
        notificationService.markAllAsRead().thenRun(() -> {
            Platform.runLater(() -> {
                mostrarAlertaExito(AppConstants.MESSAGE_SUCCESS_READ_ALL);
                cargarNotificaciones(1);
            });
        }).exceptionally(e -> {
            Platform.runLater(() -> {
                mostrarAlertaError(AppConstants.MESSAGE_ERROR_NOTIFICATIONS);
            });
            return null;
        });
    }

    private void cargarNotificaciones(int page) {
        notificationService.getNotifications(page).thenAccept(res -> {
            Platform.runLater(() -> {
                procesarRespuesta(res, page);
            });
        }).exceptionally(e -> {
            Platform.runLater(() -> {
                mostrarAlertaError(AppConstants.MESSAGE_ERROR_NOTIFICATIONS);
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

        if (res == null) {
            return;
        }

        if (res.data() == null) {
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
        
        if (notif.isRead()) {
            card.setStyle("-fx-background-color: #1a1a1a; -fx-padding: 15; -fx-background-radius: 8; -fx-border-color: #333;");
        } else {
            card.setStyle("-fx-background-color: #2a1a1a; -fx-padding: 15; -fx-background-radius: 8; -fx-border-color: #e50914; -fx-border-width: 0 0 0 4;");
        }

        VBox contentBox = new VBox(5);
        HBox.setHgrow(contentBox, Priority.ALWAYS);

        Label titleLbl = new Label(notif.title());
        titleLbl.setTextFill(Color.WHITE);
        titleLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 15px;");

        Label bodyLbl = new Label(notif.body());
        bodyLbl.setTextFill(Color.LIGHTGRAY);
        bodyLbl.setWrapText(true);

        Label dateLbl = new Label(formatearFecha(notif.createdAt()));
        dateLbl.setTextFill(Color.GRAY);
        dateLbl.setStyle("-fx-font-size: 11px;");

        contentBox.getChildren().add(titleLbl);
        contentBox.getChildren().add(bodyLbl);
        contentBox.getChildren().add(dateLbl);

        Button deleteBtn = new Button("🗑");
        deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #e50914; -fx-cursor: hand;");

        configurarEventosTarjeta(card, deleteBtn, notif);

        card.getChildren().add(contentBox);
        card.getChildren().add(deleteBtn);

        return card;
    }

    private void configurarEventosTarjeta(HBox card, Button deleteBtn, NotificationDto notif) {
        deleteBtn.setOnAction(e -> {
            e.consume();
            notificationService.deleteNotification(notif.id()).thenRun(() -> {
                Platform.runLater(() -> {
                    cargarNotificaciones(1);
                });
            });
        });

        card.setOnMouseClicked(e -> {
            if (!notif.isRead()) {
                notificationService.markAsRead(notif.id()).thenRun(() -> {
                    Platform.runLater(() -> {
                        procesarRedireccion(notif);
                    });
                });
                return;
            }
            procesarRedireccion(notif);
        });
        
        card.setStyle(card.getStyle() + " -fx-cursor: hand;");
    }

    private void procesarRedireccion(NotificationDto notif) {
        if (notif.type() != null) {
            if (notif.type().startsWith("moderation.")) {
                return;
            }
        }

        if (notif.metadata() == null) {
            return;
        }

        if (notif.metadata().tvmazeId() != null) {
            abrirDetalleSerie(notif.metadata().tvmazeId());
            return;
        }

        if (notif.actorAuthId() != null) {
            abrirPerfilUsuario(notif.actorAuthId());
            return;
        }
    }

    private void abrirDetalleSerie(Integer tvmazeId) {
        showService.getFullShowDetails(tvmazeId).thenAccept(fullData -> {
            Platform.runLater(() -> {
                try {
                    FXMLLoader loader = new FXMLLoader(App.class.getResource(AppConstants.FXML_SHOW_DETAIL));
                    Parent root = loader.load();
                    ShowDetailController controller = loader.getController();
                    
                    if (fullData != null) {
                        if (fullData.show() != null) {
                            controller.initData(fullData.show());
                            ((Stage) notificationsContainer.getScene().getWindow()).getScene().setRoot(root);
                        }
                    }
                } catch (Exception ex) {
                }
            });
        }).exceptionally(e -> null);
    }

    private void abrirPerfilUsuario(String authId) {
        userService.getUserById(authId).thenAccept(user -> {
            Platform.runLater(() -> {
                try {
                    FXMLLoader loader = new FXMLLoader(App.class.getResource(AppConstants.FXML_PROFILE));
                    Parent root = loader.load();
                    ProfileController controller = loader.getController();
                    
                    if (user != null) {
                        controller.initData(user);
                        ((Stage) notificationsContainer.getScene().getWindow()).getScene().setRoot(root);
                    }
                } catch (Exception ex) {
                }
            });
        }).exceptionally(e -> null);
    }

    private void evaluarBotonCargarMas(NotificationResponse res) {
        if (res.pagination() == null) {
            return;
        }

        if (res.pagination().hasNextPage() != null) {
            if (res.pagination().hasNextPage()) {
                Button btn = new Button("Cargar más");
                btn.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-cursor: hand;");
                
                btn.setOnAction(e -> {
                    currentPage++;
                    cargarNotificaciones(currentPage);
                });
                
                HBox box = new HBox(btn);
                box.setAlignment(Pos.CENTER);
                notificationsContainer.getChildren().add(box);
            }
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
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    
    private void mostrarAlertaExito(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Éxito");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}