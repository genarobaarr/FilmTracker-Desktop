package com.src.filmtracker.controllers.friends;

import com.src.filmtracker.App;
import com.src.filmtracker.models.friends.FriendItemDto;
import com.src.filmtracker.models.friends.FriendPaginationResponse;
import com.src.filmtracker.models.friends.FriendRequestItemDto;
import com.src.filmtracker.models.users.UserDto;
import com.src.filmtracker.services.friends.FriendsService;
import com.src.filmtracker.services.friends.IFriendsService;
import com.src.filmtracker.services.users.IUserService;
import com.src.filmtracker.services.users.UserService;
import com.src.filmtracker.utils.AppConstants;
import com.src.filmtracker.utils.CustomAlertHelper;
import com.src.filmtracker.utils.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class FriendsManagerController {
    
    private static final String STYLE_BTN_DARK = "-fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;";
    private static final String STYLE_BTN_GREEN = "-fx-background-color: #4caf50; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;";
    private static final String STYLE_BTN_RED = "-fx-background-color: #e50914; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;";
    private static final String STYLE_CARD_BG = "-fx-background-color: #1e1e1e; -fx-padding: 15; -fx-background-radius: 8;";
    private static final String STYLE_BTN_CARRUSEL = "-fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-font-size: 16px; -fx-cursor: hand;";
    private static final String STYLE_NAME_LINK = "-fx-font-weight: bold; -fx-font-size: 14px; -fx-cursor: hand; -fx-underline: true;";
    private static final String STYLE_USER_LINK = "-fx-font-size: 12px; -fx-cursor: hand; -fx-underline: true;";
    private static final String STYLE_IV_HAND = "-fx-cursor: hand;";
    
    private static final String MSG_NO_AMIGOS = "No tienes amigos agregados aún.";
    private static final String MSG_NO_SOLICITUDES = "No hay solicitudes recibidas.";
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
        cargarSolicitudesRecibidas(1);
        cargarSolicitudesEnviadas(1);
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
        
        friendsService.getFriends(authId, page)
            .thenAccept(res -> Platform.runLater(() -> procesarPaginacionAmigos(res)))
            .exceptionally(e -> {
                Platform.runLater(() -> {
                    if (!App.procesarErrorCritico(e)) mostrarVacio(friendsListSection, AppConstants.MESSAGE_ERROR_API);
                });
                return null;
            });
    }

    private void procesarPaginacionAmigos(FriendPaginationResponse response) {
        if (response == null || response.data() == null || response.data().isEmpty()) {
            mostrarVacio(friendsListSection, MSG_NO_AMIGOS);
            return;
        }

        HBox content = new HBox(15);
        content.setPadding(new Insets(10));
        
        for (FriendItemDto item : response.data()) {
            resolverAmigoYAgregar(item, content);
        }
        
        dibujarContenedorAmigos(content);
    }

    private void resolverAmigoYAgregar(FriendItemDto item, HBox content) {
        if (item == null || item.friendAuthId() == null || item.friendAuthId().isEmpty()) {
            return;
        }
        
        userService.getUserById(item.friendAuthId()).thenAccept(fullUser -> {
            if (fullUser != null) {
                Platform.runLater(() -> content.getChildren().add(buildFriendCard(fullUser)));
            }
        });
    }

    private void dibujarContenedorAmigos(HBox content) {
        ScrollPane sp = new ScrollPane(content);
        sp.setFitToHeight(true);
        sp.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        Button bI = new Button("<"); 
        bI.setStyle(STYLE_BTN_CARRUSEL);
        
        Button bD = new Button(">"); 
        bD.setStyle(STYLE_BTN_CARRUSEL);
        
        bI.setOnAction(e -> sp.setHvalue(Math.max(0, sp.getHvalue() - 0.2))); 
        bD.setOnAction(e -> sp.setHvalue(Math.min(1, sp.getHvalue() + 0.2)));
        
        BorderPane bp = new BorderPane(sp); 
        bp.setLeft(bI); 
        bp.setRight(bD);
        
        BorderPane.setAlignment(bI, Pos.CENTER); 
        BorderPane.setAlignment(bD, Pos.CENTER);
        
        friendsListSection.getChildren().clear();
        friendsListSection.getChildren().add(bp);
    }

    private String obtenerUrlAvatar(UserDto user) {
        if (user.profileImage() != null && !user.profileImage().isEmpty()) {
            return user.profileImage();
        }
        String nameParam = user.username() != null ? user.username() : "User";
        return AVATAR_API_BASE + nameParam;
    }

    private VBox buildFriendCard(UserDto friend) {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);
        box.setStyle(STYLE_CARD_BG + " -fx-cursor: hand;");
        box.setPrefWidth(150);
        box.setMaxWidth(150);

        ImageView iv = new ImageView(new Image(obtenerUrlAvatar(friend), true));
        iv.setFitWidth(80);
        iv.setFitHeight(80);

        String nombreSeguro = friend.name() != null ? friend.name() : "Desconocido";
        Label name = new Label(nombreSeguro);
        name.setTextFill(Color.WHITE);
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        String userSeguro = friend.username() != null ? friend.username() : "usuario";
        Label user = new Label("@" + userSeguro);
        user.setTextFill(Color.web(AppConstants.COLOR_ACCENT));
        user.setStyle("-fx-font-size: 12px;");

        box.getChildren().addAll(iv, name, user);
        box.setOnMouseClicked(e -> abrirPerfilAmigo(friend));

        return box;
    }

    private void abrirPerfilAmigo(UserDto amigo) {
        if (amigo != null) App.showProfileView(amigo);
    }

    private void cargarSolicitudesRecibidas(int page) {
        incomingBox.getChildren().clear();
        friendsService.getIncomingRequests(page).thenAccept(res -> {
            if (res == null || res.data() == null || res.data().isEmpty()) {
                Platform.runLater(() -> mostrarVacio(incomingBox, MSG_NO_SOLICITUDES));
                return;
            }
            for (FriendRequestItemDto req : res.data()) {
                resolverUsuarioRecibido(req);
            }
        }).exceptionally(e -> {
            App.procesarErrorCritico(e);
            return null;
        });
    }

    private void resolverUsuarioRecibido(FriendRequestItemDto req) {
        String requesterId = req.getSafeRequester();
        if (requesterId != null && !requesterId.isEmpty()) {
            userService.getUserById(requesterId).thenAccept(user -> {
                if (user != null) {
                    Platform.runLater(() -> incomingBox.getChildren().add(buildIncomingCard(req, user)));
                }
            }).exceptionally(e -> {
                App.procesarErrorCritico(e);
                return null;
            });
        }
    }

    private HBox buildIncomingCard(FriendRequestItemDto req, UserDto user) {
        HBox box = new HBox(15);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setStyle(STYLE_CARD_BG);

        ImageView iv = new ImageView(new Image(obtenerUrlAvatar(user), true));
        iv.setFitWidth(50);
        iv.setFitHeight(50);

        VBox infoBox = new VBox(5);
        Label name = new Label(user.name());
        name.setTextFill(Color.WHITE);

        Label username = new Label("@" + user.username());
        username.setTextFill(Color.web(AppConstants.COLOR_ACCENT));

        configurarInformacionClicable(iv, name, username, user);
        infoBox.getChildren().addAll(name, username);

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button acceptBtn = new Button("Aceptar");
        acceptBtn.setStyle(STYLE_BTN_GREEN);
        acceptBtn.setOnAction(e -> procesarAceptar(req.getSafeId()));

        Button rejectBtn = new Button("Rechazar");
        rejectBtn.setStyle(STYLE_BTN_DARK);
        rejectBtn.setOnAction(e -> procesarRechazar(req.getSafeId()));

        box.getChildren().addAll(iv, infoBox, spacer, acceptBtn, rejectBtn);
        return box;
    }

    private void procesarAceptar(Integer reqId) {
        friendsService.acceptFriendRequest(reqId).thenRun(() -> 
            Platform.runLater(() -> {
                mostrarAlertaExito(AppConstants.MESSAGE_SUCCESS_ACCEPTED);
                cargarDatos();
            })
        ).exceptionally(e -> {
            Platform.runLater(() -> {
                if (!App.procesarErrorCritico(e)) mostrarAlertaError(AppConstants.MESSAGE_ERROR_API);
            });
            return null;
        });
    }

    private void procesarRechazar(Integer reqId) {
        friendsService.rejectFriendRequest(reqId).thenRun(() -> 
            Platform.runLater(() -> {
                mostrarAlertaExito(AppConstants.MESSAGE_SUCCESS_REJECTED);
                cargarDatos();
            })
        ).exceptionally(e -> {
            Platform.runLater(() -> {
                if (!App.procesarErrorCritico(e)) mostrarAlertaError(AppConstants.MESSAGE_ERROR_API);
            });
            return null;
        });
    }

    private void cargarSolicitudesEnviadas(int page) {
        outgoingBox.getChildren().clear();
        friendsService.getOutgoingRequests(page).thenAccept(res -> {
            if (res == null || res.data() == null || res.data().isEmpty()) {
                Platform.runLater(() -> mostrarVacio(outgoingBox, "No hay solicitudes enviadas."));
                return;
            }
            for (FriendRequestItemDto req : res.data()) {
                resolverUsuarioEnviado(req);
            }
        }).exceptionally(e -> {
            App.procesarErrorCritico(e);
            return null;
        });
    }

    private void resolverUsuarioEnviado(FriendRequestItemDto req) {
        String receiverId = req.getSafeReceiver();
        if (receiverId != null && !receiverId.isEmpty()) {
            userService.getUserById(receiverId).thenAccept(user -> {
                if (user != null) {
                    Platform.runLater(() -> outgoingBox.getChildren().add(buildOutgoingCard(req, user)));
                }
            }).exceptionally(e -> {
                App.procesarErrorCritico(e);
                return null;
            });
        }
    }

    private HBox buildOutgoingCard(FriendRequestItemDto req, UserDto user) {
        HBox box = new HBox(15);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setStyle(STYLE_CARD_BG);

        ImageView iv = new ImageView(new Image(obtenerUrlAvatar(user), true));
        iv.setFitWidth(50);
        iv.setFitHeight(50);

        VBox infoBox = new VBox(5);
        Label name = new Label(user.name());
        name.setTextFill(Color.WHITE);

        Label username = new Label("@" + user.username());
        username.setTextFill(Color.web(AppConstants.COLOR_ACCENT));

        configurarInformacionClicable(iv, name, username, user);
        infoBox.getChildren().addAll(name, username);

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button cancelBtn = new Button("Cancelar Solicitud");
        cancelBtn.setStyle(STYLE_BTN_RED);
        cancelBtn.setOnAction(e -> procesarCancelar(req.getSafeId()));

        box.getChildren().addAll(iv, infoBox, spacer, cancelBtn);
        return box;
    }
    
    private void configurarInformacionClicable(ImageView iv, Label name, Label username, UserDto user) {
        name.setStyle(STYLE_NAME_LINK);
        username.setStyle(STYLE_USER_LINK);
        iv.setStyle(STYLE_IV_HAND);
        
        name.setOnMouseClicked(e -> abrirPerfilAmigo(user));
        username.setOnMouseClicked(e -> abrirPerfilAmigo(user));
        iv.setOnMouseClicked(e -> abrirPerfilAmigo(user));
    }

    private void procesarCancelar(Integer reqId) {
        friendsService.cancelFriendRequest(reqId).thenRun(() -> 
            Platform.runLater(() -> {
                mostrarAlertaExito(AppConstants.MESSAGE_SUCCESS_CANCELED);
                cargarDatos();
            })
        ).exceptionally(e -> {
            Platform.runLater(() -> {
                if (!App.procesarErrorCritico(e)) mostrarAlertaError(AppConstants.MESSAGE_ERROR_API);
            });
            return null;
        });
    }

    private void mostrarVacio(VBox section, String msj) {
        Label lbl = new Label(msj);
        lbl.setTextFill(Color.GRAY);
        
        section.getChildren().clear();
        section.getChildren().add(lbl);
    }

    private void mostrarAlertaError(String mensaje) {
        CustomAlertHelper.mostrarError(mensaje);
    }

    private void mostrarAlertaExito(String mensaje) {
        CustomAlertHelper.mostrarExito(mensaje);
    }
}