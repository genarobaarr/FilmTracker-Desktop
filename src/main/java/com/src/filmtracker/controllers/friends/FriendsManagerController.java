package com.src.filmtracker.controllers.friends;

import com.src.filmtracker.App;
import com.src.filmtracker.controllers.users.ProfileController;
import com.src.filmtracker.models.friends.FriendItemDto;
import com.src.filmtracker.models.friends.FriendPaginationResponse;
import com.src.filmtracker.models.friends.FriendRequestItemDto;
import com.src.filmtracker.models.friends.FriendRequestPaginationResponse;
import com.src.filmtracker.models.users.UserDto;
import com.src.filmtracker.services.friends.FriendsService;
import com.src.filmtracker.services.friends.IFriendsService;
import com.src.filmtracker.services.users.IUserService;
import com.src.filmtracker.services.users.UserService;
import com.src.filmtracker.utils.AppConstants;
import com.src.filmtracker.utils.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class FriendsManagerController {

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

    @FXML private void handleBack() { 
        App.setRoot(AppConstants.FXML_DASHBOARD); 
    }
    
    @FXML private void handleMinimize() { 
        ((Stage) incomingBox.getScene().getWindow()).setIconified(true); 
    }
    
    @FXML private void handleClose() { 
        Platform.exit(); 
        System.exit(0); 
    }

    private void cargarAmigosPropios(int page) {
        UserDto currentUser = SessionManager.getInstance().getCurrentUser();
        
        if (currentUser == null) {
            return;
        }
        
        String authId = currentUser.getSafeAuthId();
        
        if (authId == null) {
            return;
        }
        
        if (authId.isEmpty()) {
            return;
        }
        
        friendsService.getFriends(authId, page).thenAccept(res -> {
            Platform.runLater(() -> {
                procesarPaginacionAmigos(res);
            });
        }).exceptionally(e -> {
            Platform.runLater(() -> {
                mostrarVacio(friendsListSection, AppConstants.MESSAGE_ERROR_API);
            });
            return null;
        });
    }

    private void procesarPaginacionAmigos(FriendPaginationResponse response) {
        if (response == null) {
            mostrarVacio(friendsListSection, "No tienes amigos agregados aún.");
            return;
        }
        
        if (response.data() == null) {
            mostrarVacio(friendsListSection, "No tienes amigos agregados aún.");
            return;
        }
        
        if (response.data().isEmpty()) {
            mostrarVacio(friendsListSection, "No tienes amigos agregados aún.");
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
        if (item == null) {
            return;
        }
        
        String targetId = item.friendAuthId();
        
        if (targetId == null) {
            return;
        }
        
        if (targetId.isEmpty()) {
            return;
        }
        
        userService.getUserById(targetId).thenAccept(fullUser -> {
            if (fullUser != null) {
                Platform.runLater(() -> {
                    VBox card = buildFriendCard(fullUser);
                    content.getChildren().add(card);
                });
            }
        });
    }

    private void dibujarContenedorAmigos(HBox content) {
        ScrollPane sp = new ScrollPane(content);
        sp.setFitToHeight(true);
        sp.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        Button bI = new Button("<"); 
        bI.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-font-size: 16px; -fx-cursor: hand;");
        
        Button bD = new Button(">"); 
        bD.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-font-size: 16px; -fx-cursor: hand;");
        
        bI.setOnAction(e -> {
            sp.setHvalue(Math.max(0, sp.getHvalue() - 0.2));
        }); 
        
        bD.setOnAction(e -> {
            sp.setHvalue(Math.min(1, sp.getHvalue() + 0.2));
        });
        
        BorderPane bp = new BorderPane(sp); 
        bp.setLeft(bI); 
        bp.setRight(bD);
        
        BorderPane.setAlignment(bI, Pos.CENTER); 
        BorderPane.setAlignment(bD, Pos.CENTER);
        
        friendsListSection.getChildren().clear();
        friendsListSection.getChildren().add(bp);
    }

    private VBox buildFriendCard(UserDto friend) {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-background-color: #1e1e1e; -fx-padding: 15; -fx-background-radius: 8; -fx-cursor: hand;");
        box.setPrefWidth(150);
        box.setMaxWidth(150);

        ImageView iv = new ImageView();
        iv.setFitWidth(80);
        iv.setFitHeight(80);
        
        String imageUrl = "https://ui-avatars.com/api/?name=" + friend.username() + "&background=e50914&color=fff";
        
        if (friend.profileImage() != null) {
            if (!friend.profileImage().isEmpty()) {
                imageUrl = friend.profileImage();
            }
        }
        
        iv.setImage(new Image(imageUrl, true));

        String nombreSeguro = "Desconocido";
        if (friend.name() != null) {
            nombreSeguro = friend.name();
        }

        Label name = new Label(nombreSeguro);
        name.setTextFill(Color.WHITE);
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        String userSeguro = "usuario";
        if (friend.username() != null) {
            userSeguro = friend.username();
        }

        Label user = new Label("@" + userSeguro);
        user.setTextFill(Color.web(AppConstants.COLOR_ACCENT));
        user.setStyle("-fx-font-size: 12px;");

        box.getChildren().add(iv);
        box.getChildren().add(name);
        box.getChildren().add(user);

        box.setOnMouseClicked(e -> {
            abrirPerfilAmigo(friend);
        });

        return box;
    }

    private void abrirPerfilAmigo(UserDto amigo) {
        if (amigo != null) {
            App.showProfileView(amigo);
        }
    }

    private void cargarSolicitudesRecibidas(int page) {
        incomingBox.getChildren().clear();
        
        friendsService.getIncomingRequests(page).thenAccept(res -> {
            if (res == null) {
                Platform.runLater(() -> mostrarVacio(incomingBox, "No hay solicitudes recibidas."));
                return;
            }
            
            if (res.data() == null) {
                Platform.runLater(() -> mostrarVacio(incomingBox, "No hay solicitudes recibidas."));
                return;
            }
            
            if (res.data().isEmpty()) {
                Platform.runLater(() -> mostrarVacio(incomingBox, "No hay solicitudes recibidas."));
                return;
            }
            
            for (FriendRequestItemDto req : res.data()) {
                resolverUsuarioRecibido(req);
            }
        });
    }

    private void resolverUsuarioRecibido(FriendRequestItemDto req) {
        String requesterId = req.getSafeRequester();
        
        if (requesterId != null) {
            if (!requesterId.isEmpty()) {
                userService.getUserById(requesterId).thenAccept(user -> {
                    if (user != null) {
                        Platform.runLater(() -> {
                            incomingBox.getChildren().add(buildIncomingCard(req, user));
                        });
                    }
                });
            }
        }
    }

    private HBox buildIncomingCard(FriendRequestItemDto req, UserDto user) {
        HBox box = new HBox(15);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setStyle("-fx-background-color: #1e1e1e; -fx-padding: 15; -fx-background-radius: 8;");

        ImageView iv = new ImageView();
        iv.setFitWidth(50);
        iv.setFitHeight(50);
        
        String imageUrl = "https://ui-avatars.com/api/?name=" + user.username() + "&background=e50914&color=fff";
        
        if (user.profileImage() != null) {
            if (!user.profileImage().isEmpty()) {
                imageUrl = user.profileImage();
            }
        }
        
        iv.setImage(new Image(imageUrl, true));

        VBox infoBox = new VBox(5);
        Label name = new Label(user.name());
        name.setTextFill(Color.WHITE);
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label username = new Label("@" + user.username());
        username.setTextFill(Color.web(AppConstants.COLOR_ACCENT));
        username.setStyle("-fx-font-size: 12px;");

        infoBox.getChildren().add(name);
        infoBox.getChildren().add(username);

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button acceptBtn = new Button("Aceptar");
        acceptBtn.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        
        acceptBtn.setOnAction(e -> {
            procesarAceptar(req.getSafeId());
        });

        Button rejectBtn = new Button("Rechazar");
        rejectBtn.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        
        rejectBtn.setOnAction(e -> {
            procesarRechazar(req.getSafeId());
        });

        box.getChildren().add(iv);
        box.getChildren().add(infoBox);
        box.getChildren().add(spacer);
        box.getChildren().add(acceptBtn);
        box.getChildren().add(rejectBtn);

        return box;
    }

    private void procesarAceptar(Integer reqId) {
        friendsService.acceptFriendRequest(reqId).thenRun(() -> {
            Platform.runLater(() -> {
                mostrarAlertaExito(AppConstants.MESSAGE_SUCCESS_ACCEPTED);
                cargarDatos();
            });
        }).exceptionally(e -> {
            Platform.runLater(() -> {
                mostrarAlertaError(AppConstants.MESSAGE_ERROR_API);
            });
            return null;
        });
    }

    private void procesarRechazar(Integer reqId) {
        friendsService.rejectFriendRequest(reqId).thenRun(() -> {
            Platform.runLater(() -> {
                mostrarAlertaExito(AppConstants.MESSAGE_SUCCESS_REJECTED);
                cargarDatos();
            });
        }).exceptionally(e -> {
            Platform.runLater(() -> {
                mostrarAlertaError(AppConstants.MESSAGE_ERROR_API);
            });
            return null;
        });
    }

    private void cargarSolicitudesEnviadas(int page) {
        outgoingBox.getChildren().clear();
        
        friendsService.getOutgoingRequests(page).thenAccept(res -> {
            if (res == null) {
                Platform.runLater(() -> mostrarVacio(outgoingBox, "No hay solicitudes enviadas."));
                return;
            }
            
            if (res.data() == null) {
                Platform.runLater(() -> mostrarVacio(outgoingBox, "No hay solicitudes enviadas."));
                return;
            }
            
            if (res.data().isEmpty()) {
                Platform.runLater(() -> mostrarVacio(outgoingBox, "No hay solicitudes enviadas."));
                return;
            }
            
            for (FriendRequestItemDto req : res.data()) {
                resolverUsuarioEnviado(req);
            }
        });
    }

    private void resolverUsuarioEnviado(FriendRequestItemDto req) {
        String receiverId = req.getSafeReceiver();
        
        if (receiverId != null) {
            if (!receiverId.isEmpty()) {
                userService.getUserById(receiverId).thenAccept(user -> {
                    if (user != null) {
                        Platform.runLater(() -> {
                            outgoingBox.getChildren().add(buildOutgoingCard(req, user));
                        });
                    }
                });
            }
        }
    }

    private HBox buildOutgoingCard(FriendRequestItemDto req, UserDto user) {
        HBox box = new HBox(15);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setStyle("-fx-background-color: #1e1e1e; -fx-padding: 15; -fx-background-radius: 8;");

        ImageView iv = new ImageView();
        iv.setFitWidth(50);
        iv.setFitHeight(50);
        
        String imageUrl = "https://ui-avatars.com/api/?name=" + user.username() + "&background=e50914&color=fff";
        
        if (user.profileImage() != null) {
            if (!user.profileImage().isEmpty()) {
                imageUrl = user.profileImage();
            }
        }
        
        iv.setImage(new Image(imageUrl, true));

        VBox infoBox = new VBox(5);
        Label name = new Label(user.name());
        name.setTextFill(Color.WHITE);
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label username = new Label("@" + user.username());
        username.setTextFill(Color.web(AppConstants.COLOR_ACCENT));
        username.setStyle("-fx-font-size: 12px;");

        infoBox.getChildren().add(name);
        infoBox.getChildren().add(username);

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button cancelBtn = new Button("Cancelar Solicitud");
        cancelBtn.setStyle("-fx-background-color: #e50914; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        
        cancelBtn.setOnAction(e -> {
            procesarCancelar(req.getSafeId());
        });

        box.getChildren().add(iv);
        box.getChildren().add(infoBox);
        box.getChildren().add(spacer);
        box.getChildren().add(cancelBtn);

        return box;
    }

    private void procesarCancelar(Integer reqId) {
        friendsService.cancelFriendRequest(reqId).thenRun(() -> {
            Platform.runLater(() -> {
                mostrarAlertaExito(AppConstants.MESSAGE_SUCCESS_CANCELED);
                cargarDatos();
            });
        }).exceptionally(e -> {
            Platform.runLater(() -> {
                mostrarAlertaError(AppConstants.MESSAGE_ERROR_API);
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