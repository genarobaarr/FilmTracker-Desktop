package com.src.filmtracker.controllers.friends;

import com.src.filmtracker.App;
import com.src.filmtracker.models.friends.FriendRequestItemDto;
import com.src.filmtracker.models.friends.FriendRequestPaginationResponse;
import com.src.filmtracker.models.users.UserDto;
import com.src.filmtracker.services.friends.FriendsService;
import com.src.filmtracker.services.friends.IFriendsService;
import com.src.filmtracker.services.users.IUserService;
import com.src.filmtracker.services.users.UserService;
import com.src.filmtracker.utils.AppConstants;
import com.src.filmtracker.utils.CustomAlertHelper;
import com.src.filmtracker.utils.FriendUIHelper;
import com.src.filmtracker.utils.ImageHelper;
import com.src.filmtracker.utils.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.concurrent.CompletableFuture;

public class FriendsManagerController {
    
    private static final String STYLE_BTN_DARK = "-fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;";
    private static final String STYLE_BTN_GREEN = "-fx-background-color: #4caf50; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;";
    private static final String STYLE_BTN_RED = "-fx-background-color: #e50914; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;";
    private static final String STYLE_CARD_BG = "-fx-background-color: #1e1e1e; -fx-padding: 15; -fx-background-radius: 8;";
    private static final String STYLE_NAME_LINK = "-fx-font-weight: bold; -fx-font-size: 14px; -fx-cursor: hand; -fx-underline: true;";
    private static final String STYLE_USER_LINK = "-fx-font-size: 12px; -fx-cursor: hand; -fx-underline: true;";
    private static final String STYLE_IV_HAND = "-fx-cursor: hand;";
    
    private static final String AVATAR_API_BASE = "https://ui-avatars.com/api/?background=e50914&color=fff&name=";

    @FXML private VBox friendsListSection;
    @FXML private VBox incomingBox;
    @FXML private VBox outgoingBox;

    private final IFriendsService friendsService = new FriendsService();
    private final IUserService userService = new UserService();

    @FXML
    public void initialize() {
        cargarDatos();
    }

    private void cargarDatos() {
        cargarAmigosPropios(1);
        cargarSolicitudes(1, true);
        cargarSolicitudes(1, false);
    }

    @FXML 
    private void handleBack() { 
        App.goBackUniversal(); 
    }
    
    @FXML 
    private void handleMinimize() { 
        ((Stage) incomingBox.getScene().getWindow()).setIconified(true); 
    }
    
    @FXML 
    private void handleClose() { 
        Platform.exit(); 
        System.exit(0); 
    }

    private void cargarAmigosPropios(int page) {
        UserDto currentUser = SessionManager.getInstance().getCurrentUser();
        
        if (currentUser == null) {
            return;
        }
        
        String authId = currentUser.getSafeAuthId();
        
        if (authId == null || authId.isEmpty()) {
            return;
        }
        
        friendsService.getFriends(authId, page).thenAccept(res -> 
            Platform.runLater(() -> FriendUIHelper.procesarPaginacionAmigos(res, friendsListSection, userService, "No tienes amigos agregados aún."))
        ).exceptionally(e -> {
            Platform.runLater(() -> {
                if (!App.procesarErrorCritico(e)) {
                    FriendUIHelper.mostrarVacio(friendsListSection, AppConstants.MESSAGE_ERROR_API);
                }
            });
            return null;
        });
    }

    private void cargarSolicitudes(int page, boolean isIncoming) {
        VBox box = isIncoming ? incomingBox : outgoingBox;
        box.getChildren().clear();
        
        CompletableFuture<FriendRequestPaginationResponse> future = isIncoming 
            ? friendsService.getIncomingRequests(page) 
            : friendsService.getOutgoingRequests(page);
            
        future.thenAccept(res -> {
            if (res == null || res.data() == null || res.data().isEmpty()) {
                Platform.runLater(() -> FriendUIHelper.mostrarVacio(box, isIncoming ? "No hay solicitudes recibidas." : "No hay solicitudes enviadas."));
                return;
            }
            
            for (FriendRequestItemDto req : res.data()) {
                resolverUsuarioSolicitud(req, isIncoming, box);
            }
        }).exceptionally(e -> {
            App.procesarErrorCritico(e);
            return null;
        });
    }

    private void resolverUsuarioSolicitud(FriendRequestItemDto req, boolean isIncoming, VBox box) {
        String targetId = isIncoming ? req.getSafeRequester() : req.getSafeReceiver();
        
        if (targetId != null && !targetId.isEmpty()) {
            userService.getUserById(targetId).thenAccept(user -> {
                if (user != null) {
                    Platform.runLater(() -> box.getChildren().add(isIncoming ? buildIncomingCard(req, user) : buildOutgoingCard(req, user)));
                }
            }).exceptionally(e -> {
                App.procesarErrorCritico(e);
                return null;
            });
        }
    }

    private String obtenerUrlAvatar(UserDto user) {
        if (user.profileImage() != null && !user.profileImage().isEmpty()) {
            return user.profileImage();
        }
        
        String nameParam = user.username() != null ? user.username() : "User";
        return AVATAR_API_BASE + nameParam;
    }

    private void configurarInfoCard(HBox box, UserDto user, VBox infoBox) {
        ImageView iv = new ImageView();
        ImageHelper.cargarImagenConRespaldo(obtenerUrlAvatar(user), iv);
        iv.setFitWidth(50);
        iv.setFitHeight(50);

        Label name = new Label(user.name());
        name.setTextFill(Color.WHITE);

        Label username = new Label("@" + user.username());
        username.setTextFill(Color.web(AppConstants.COLOR_ACCENT));

        name.setStyle(STYLE_NAME_LINK);
        username.setStyle(STYLE_USER_LINK);
        iv.setStyle(STYLE_IV_HAND);
        
        name.setOnMouseClicked(e -> App.showProfileView(user));
        username.setOnMouseClicked(e -> App.showProfileView(user));
        iv.setOnMouseClicked(e -> App.showProfileView(user));

        infoBox.getChildren().addAll(name, username);
        box.getChildren().addAll(iv, infoBox);
    }

    private HBox buildIncomingCard(FriendRequestItemDto req, UserDto user) {
        HBox box = new HBox(15);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setStyle(STYLE_CARD_BG);

        VBox infoBox = new VBox(5);
        configurarInfoCard(box, user, infoBox);

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button acceptBtn = new Button("Aceptar");
        acceptBtn.setStyle(STYLE_BTN_GREEN);
        acceptBtn.setOnAction(e -> procesarSolicitud(friendsService.acceptFriendRequest(req.getSafeId()), AppConstants.MESSAGE_SUCCESS_ACCEPTED));

        Button rejectBtn = new Button("Rechazar");
        rejectBtn.setStyle(STYLE_BTN_DARK);
        rejectBtn.setOnAction(e -> procesarSolicitud(friendsService.rejectFriendRequest(req.getSafeId()), AppConstants.MESSAGE_SUCCESS_REJECTED));

        box.getChildren().addAll(spacer, acceptBtn, rejectBtn);
        return box;
    }

    private HBox buildOutgoingCard(FriendRequestItemDto req, UserDto user) {
        HBox box = new HBox(15);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setStyle(STYLE_CARD_BG);

        VBox infoBox = new VBox(5);
        configurarInfoCard(box, user, infoBox);

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button cancelBtn = new Button("Cancelar Solicitud");
        cancelBtn.setStyle(STYLE_BTN_RED);
        cancelBtn.setOnAction(e -> procesarSolicitud(friendsService.cancelFriendRequest(req.getSafeId()), AppConstants.MESSAGE_SUCCESS_CANCELED));

        box.getChildren().addAll(spacer, cancelBtn);
        return box;
    }

    private void procesarSolicitud(CompletableFuture<Void> action, String successMsg) {
        action.thenRun(() -> 
            Platform.runLater(() -> {
                CustomAlertHelper.mostrarExito(successMsg);
                cargarDatos();
            })
        ).exceptionally(e -> {
            Platform.runLater(() -> {
                if (!App.procesarErrorCritico(e)) {
                    CustomAlertHelper.mostrarError(AppConstants.MESSAGE_ERROR_API);
                }
            });
            return null;
        });
    }
}