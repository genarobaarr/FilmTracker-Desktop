package com.src.filmtracker.utils;

import com.src.filmtracker.App;
import com.src.filmtracker.models.friends.FriendItemDto;
import com.src.filmtracker.models.friends.FriendPaginationResponse;
import com.src.filmtracker.models.users.UserDto;
import com.src.filmtracker.services.users.IUserService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public final class FriendUIHelper {
    
    private FriendUIHelper() {
        // Clase utilitaria
    }

    public static void procesarPaginacionAmigos(FriendPaginationResponse response, VBox container, IUserService userService, String emptyMsg) {
        if (response == null || response.data() == null || response.data().isEmpty()) {
            mostrarVacio(container, emptyMsg);
            return;
        }

        HBox content = new HBox(15);
        content.setPadding(new Insets(10));
        
        for (FriendItemDto item : response.data()) {
            resolverAmigoYAgregar(item, content, userService);
        }
        
        dibujarContenedorAmigos(content, container);
    }

    private static void resolverAmigoYAgregar(FriendItemDto item, HBox content, IUserService userService) {
        if (item == null || item.friendAuthId() == null || item.friendAuthId().isEmpty()) {
            return;
        }
        
        userService.getUserById(item.friendAuthId()).thenAccept(fullUser -> {
            if (fullUser != null) {
                Platform.runLater(() -> content.getChildren().add(buildFriendCard(fullUser)));
            }
        });
    }

    private static void dibujarContenedorAmigos(HBox content, VBox section) {
        ScrollPane sp = new ScrollPane(content);
        sp.setFitToHeight(true);
        sp.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        
        Button bI = crearBotonCarrusel("<"); 
        Button bD = crearBotonCarrusel(">"); 
        
        bI.setOnAction(e -> sp.setHvalue(Math.max(0, sp.getHvalue() - 0.2))); 
        bD.setOnAction(e -> sp.setHvalue(Math.min(1, sp.getHvalue() + 0.2)));
        
        BorderPane bp = new BorderPane(sp); 
        bp.setLeft(bI); 
        bp.setRight(bD);
        BorderPane.setAlignment(bI, Pos.CENTER); 
        BorderPane.setAlignment(bD, Pos.CENTER);
        
        section.getChildren().clear();
        section.getChildren().add(bp);
    }

    private static Button crearBotonCarrusel(String texto) {
        Button btn = new Button(texto);
        btn.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 8 15; -fx-cursor: hand;");
        return btn;
    }

    public static VBox buildFriendCard(UserDto friend) {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-background-color: #1e1e1e; -fx-padding: 15; -fx-background-radius: 8; -fx-cursor: hand;");
        box.setPrefWidth(150);
        box.setMaxWidth(150);
        
        ImageView iv = new ImageView();
        iv.setFitWidth(80);
        iv.setFitHeight(80);
        
        String imageUrl = "https://ui-avatars.com/api/?name=" + friend.username() + "&background=e50914&color=fff";
        if (friend.profileImage() != null && !friend.profileImage().isEmpty()) {
            imageUrl = friend.profileImage();
        }
        
        ImageHelper.cargarImagenConRespaldo(imageUrl, iv);
        
        String nombreSeguro = friend.name() != null ? friend.name() : "Desconocido";
        Label name = new Label(nombreSeguro);
        name.setTextFill(Color.WHITE);
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        String userSeguro = friend.username() != null ? friend.username() : "usuario";
        Label user = new Label("@" + userSeguro);
        user.setTextFill(Color.web(AppConstants.COLOR_ACCENT));
        user.setStyle("-fx-font-size: 12px;");
        
        box.getChildren().addAll(iv, name, user);
        box.setOnMouseClicked(e -> App.showProfileView(friend));
        
        return box;
    }

    public static void mostrarVacio(VBox section, String msj) {
        Label lbl = new Label(msj);
        lbl.setTextFill(Color.GRAY);
        section.getChildren().clear();
        section.getChildren().add(lbl);
    }
}