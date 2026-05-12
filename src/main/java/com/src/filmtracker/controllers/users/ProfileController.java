package com.src.filmtracker.controllers.users;

import com.src.filmtracker.App;
import com.src.filmtracker.models.users.UserDto;
import com.src.filmtracker.models.library.LibraryItemDto;
import com.src.filmtracker.models.reviews.ReviewDto;
import com.src.filmtracker.models.reviews.ReviewPaginationResponse;
import com.src.filmtracker.models.reviews.ReviewSummaryDto;
import com.src.filmtracker.models.shows.Show;
import com.src.filmtracker.models.shows.ShowFullResponse;
import com.src.filmtracker.models.users.UpdateProfileRequest;
import com.src.filmtracker.models.friends.FriendItemDto;
import com.src.filmtracker.models.friends.FriendPaginationResponse;
import com.src.filmtracker.models.friends.FriendStatusResponse;
import com.src.filmtracker.models.friends.SendFriendRequest;
import com.src.filmtracker.services.library.ILibraryService;
import com.src.filmtracker.services.library.LibraryService;
import com.src.filmtracker.services.reviews.IReviewService;
import com.src.filmtracker.services.reviews.ReviewService;
import com.src.filmtracker.services.shows.IShowService;
import com.src.filmtracker.services.shows.ShowService;
import com.src.filmtracker.services.users.IUserService;
import com.src.filmtracker.services.users.UserService;
import com.src.filmtracker.services.friends.IFriendsService;
import com.src.filmtracker.services.friends.FriendsService;
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
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ProfileController {
    
    @FXML private ImageView avatarView;
    @FXML private Button editAvatarBtn;
    @FXML private Label nameLabel;
    @FXML private Label usernameLabel;
    @FXML private Label emailLabel;
    @FXML private Label roleLabel;
    @FXML private Label dateLabel;
    @FXML private Label reviewsCountLabel;
    @FXML private Label likesReceivedLabel;
    @FXML private Label friendsCountLabel;
    @FXML private VBox privateInfoContainer;
    
    @FXML private VBox friendsListContainer;
    @FXML private VBox friendsListSection;

    @FXML private HBox friendActionsBox;
    @FXML private Button addFriendBtn;
    @FXML private Button removeFriendBtn;

    @FXML private VBox favoritesContainer;
    @FXML private VBox favoritesSection;
    @FXML private Label favoritesTitleLabel;
    @FXML private VBox watchlistContainer;
    @FXML private VBox watchlistSection;
    @FXML private VBox reviewsSection;
    @FXML private Label reviewsTitleLabel;

    @FXML private HBox nameDisplayBox;
    @FXML private HBox nameEditBox;
    @FXML private TextField nameField;
    @FXML private Button editNameBtn;
    @FXML private HBox usernameDisplayBox;
    @FXML private HBox usernameEditBox;
    @FXML private TextField usernameField;
    @FXML private Button editUsernameBtn;

    private final ILibraryService libraryService = new LibraryService();
    private final IShowService showService = new ShowService();
    private final IReviewService reviewService = new ReviewService();
    private final IUserService userService = new UserService();
    private final IFriendsService friendsService = new FriendsService();
    
    private int currentReviewPage = 1;
    private UserDto currentUserProfile;
    
    @FXML private void handleEditName() { 
        toggleNameEdit(true); 
    }
    
    @FXML private void handleCancelName() { 
        toggleNameEdit(false); 
    }
    
    @FXML private void handleEditUsername() { 
        toggleUsernameEdit(true); 
    }
    
    @FXML private void handleCancelUsername() { 
        toggleUsernameEdit(false); 
    }
    
    @FXML private void handleChangePassword() { 
        App.setRoot(AppConstants.FXML_CHANGE_PASSWORD); 
    }
    
    @FXML private void handleBack() { 
        App.setRoot(AppConstants.FXML_DASHBOARD); 
    }
    
    @FXML private void handleMinimize() { 
        ((Stage) nameLabel.getScene().getWindow()).setIconified(true); 
    }
    
    @FXML private void handleClose() { 
        Platform.exit(); System.exit(0); 
    }

    @FXML private void handleSaveName() {
        String newName = nameField.getText().trim();
        
        if (newName.isEmpty()) {
            return;
        }
        
        UpdateProfileRequest req = new UpdateProfileRequest(newName, currentUserProfile.username(), currentUserProfile.profileImage());
        ejecutarActualizacion(req);
        toggleNameEdit(false);
    }

    @FXML private void handleSaveUsername() {
        String newUsername = usernameField.getText().trim();
        
        if (newUsername.isEmpty()) {
            return;
        }
        
        UpdateProfileRequest req = new UpdateProfileRequest(currentUserProfile.name(), newUsername, currentUserProfile.profileImage());
        ejecutarActualizacion(req);
        toggleUsernameEdit(false);
    }
    
    @FXML
    private void handleEditAvatar() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Seleccionar Foto de Perfil");
        
        javafx.stage.FileChooser.ExtensionFilter imageFilter = new javafx.stage.FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg");
        fileChooser.getExtensionFilters().add(imageFilter);
        
        java.io.File selectedFile = fileChooser.showOpenDialog(nameLabel.getScene().getWindow());
        
        if (selectedFile != null) {
            subirFotoPerfil(selectedFile);
        }
    }

    @FXML
    private void handleAddFriend() {
        String receiverId = currentUserProfile.getSafeAuthId();
        
        if (receiverId == null) {
            mostrarAlertaError(AppConstants.MESSAGE_ERROR_API);
            return;
        }
        
        if (receiverId.isEmpty()) {
            mostrarAlertaError(AppConstants.MESSAGE_ERROR_API);
            return;
        }

        SendFriendRequest req = new SendFriendRequest(receiverId);
        
        friendsService.sendFriendRequest(req).thenRun(() -> {
            Platform.runLater(() -> {
                mostrarAlertaExito(AppConstants.MESSAGE_SUCCESS_FRIEND_ADD);
                cargarEstadoAmistad(receiverId);
            });
        }).exceptionally(e -> {
            Platform.runLater(() -> {
                mostrarAlertaError(AppConstants.MESSAGE_ERROR_FRIEND_ACTION);
            });
            return null;
        });
    }

    @FXML
    private void handleRemoveFriend() {
        String friendId = currentUserProfile.getSafeAuthId();
        
        if (friendId == null) {
            mostrarAlertaError(AppConstants.MESSAGE_ERROR_API);
            return;
        }
        
        if (friendId.isEmpty()) {
            mostrarAlertaError(AppConstants.MESSAGE_ERROR_API);
            return;
        }

        friendsService.removeFriend(friendId).thenRun(() -> {
            Platform.runLater(() -> {
                mostrarAlertaExito(AppConstants.MESSAGE_SUCCESS_FRIEND_REMOVE);
                cargarEstadoAmistad(friendId);
                cargarEstadisticas(friendId);
            });
        }).exceptionally(e -> {
            Platform.runLater(() -> {
                mostrarAlertaError(AppConstants.MESSAGE_ERROR_FRIEND_ACTION);
            });
            return null;
        });
    }

    @FXML
    private void handleShowFriends() {
        if (!esUsuarioActual(currentUserProfile)) {
            return; 
        }
        
        boolean isVisible = friendsListContainer.isVisible();
        friendsListContainer.setVisible(!isVisible);
        friendsListContainer.setManaged(!isVisible);

        if (!isVisible) {
            cargarAmigosPropios(1);
        }
    }

    public void initData(UserDto user) {
        if (user == null) {
            return;
        }
        
        this.currentUserProfile = user;
        
        actualizarEtiquetasBasicas(user);
        configurarVisibilidadPublica(esUsuarioActual(user), user);
        cargarEstadisticas(user.getSafeAuthId());
        
        reviewsSection.getChildren().clear();
        cargarResenasPropias(1, esUsuarioActual(user));
    }

    private void actualizarEtiquetasBasicas(UserDto user) {
        nameLabel.setText(user.name());
        usernameLabel.setText("@" + user.username());
        
        if (user.email() != null) {
            emailLabel.setText(user.email());
        }
        
        if (user.role() != null) {
            roleLabel.setText(user.role());
        }
        
        if (user.createdAt() != null) {
            ZonedDateTime dt = ZonedDateTime.parse(user.createdAt());
            dateLabel.setText("Miembro desde: " + dt.format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
        }
        
        String imageUrl = "https://ui-avatars.com/api/?name=" + user.username() + "&background=e50914&color=fff";
        
        if (user.profileImage() != null) {
            if (!user.profileImage().isEmpty()) {
                imageUrl = user.profileImage();
            }
        }
        
        avatarView.setImage(new Image(imageUrl, true));
    }

    private boolean esUsuarioActual(UserDto user) {
        UserDto loggedInUser = SessionManager.getInstance().getCurrentUser();
        
        if (loggedInUser == null) {
            return false;
        }
        
        if (user == null) {
            return false;
        }
        
        if (compararUsernameExacto(user, loggedInUser)) {
            return true;
        }
        
        if (compararAuthIdExacto(user, loggedInUser)) {
            return true;
        }
        
        return false;
    }

    private boolean compararUsernameExacto(UserDto user, UserDto loggedInUser) {
        if (user.username() != null) {
            if (loggedInUser.username() != null) {
                if (user.username().trim().equalsIgnoreCase(loggedInUser.username().trim())) {
                    return true;
                }
            }
        }
        
        return false;
    }

    private boolean compararAuthIdExacto(UserDto user, UserDto loggedInUser) {
        String currentAuthId = loggedInUser.getSafeAuthId();
        String profileAuthId = user.getSafeAuthId();
        
        if (currentAuthId != null) {
            if (profileAuthId != null) {
                if (!currentAuthId.isEmpty()) {
                    if (currentAuthId.equals(profileAuthId)) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }

    private void configurarVisibilidadPublica(boolean isCurrentUser, UserDto user) {
        watchlistContainer.setVisible(isCurrentUser);
        watchlistContainer.setManaged(isCurrentUser);
        privateInfoContainer.setVisible(isCurrentUser);
        privateInfoContainer.setManaged(isCurrentUser);
        editNameBtn.setVisible(isCurrentUser);
        editNameBtn.setManaged(isCurrentUser);
        editUsernameBtn.setVisible(isCurrentUser);
        editUsernameBtn.setManaged(isCurrentUser);
        
        editAvatarBtn.setVisible(isCurrentUser);
        editAvatarBtn.setManaged(isCurrentUser);

        if (isCurrentUser) {
            friendActionsBox.setVisible(false);
            friendActionsBox.setManaged(false);
            reviewsTitleLabel.setText("Mis Reseñas Publicadas");
            favoritesTitleLabel.setText("Mis Series Favoritas");
            cargarFavoritos(true);
            cargarWatchlist();
        } else {
            friendActionsBox.setVisible(true);
            friendActionsBox.setManaged(true);
            
            ocultarBotonesAmistad();
            
            reviewsTitleLabel.setText("Reseñas de @" + user.username());
            favoritesTitleLabel.setText("Favoritos de @" + user.username());
            cargarFavoritos(false);
            cargarEstadoAmistad(user.getSafeAuthId());
        }
    }

    private void ocultarBotonesAmistad() {
        addFriendBtn.setVisible(false);
        addFriendBtn.setManaged(false);
        removeFriendBtn.setVisible(false);
        removeFriendBtn.setManaged(false);
    }

    private void configurarBotonAgregar(String texto, boolean deshabilitado) {
        addFriendBtn.setText(texto);
        addFriendBtn.setDisable(deshabilitado);
        addFriendBtn.setVisible(true);
        addFriendBtn.setManaged(true);
    }

    private void configurarBotonEliminar() {
        removeFriendBtn.setVisible(true);
        removeFriendBtn.setManaged(true);
    }

    private void cargarEstadoAmistad(String otherAuthId) {
        friendsService.getRelationshipStatus(otherAuthId).thenAccept(res -> {
            Platform.runLater(() -> {
                procesarEstadoAmistad(res);
            });
        }).exceptionally(e -> {
            return null;
        });
    }

    private void procesarEstadoAmistad(FriendStatusResponse res) {
        ocultarBotonesAmistad();

        if (res == null) {
            return;
        }
        
        String status = res.status();
        
        if (status == null) {
            return;
        }

        String safeStatus = status.trim().toUpperCase();

        if (safeStatus.equals("NONE")) {
            configurarBotonAgregar("Agregar amigo", false);
        } else if (safeStatus.equals("FRIENDS")) {
            configurarBotonEliminar();
        } else if (safeStatus.equals("PENDING_OUTGOING")) {
            configurarBotonAgregar("Solicitud enviada", true);
        } else if (safeStatus.equals("PENDING_INCOMING")) {
            configurarBotonAgregar("Responder solicitud", true);
        }
    }

    private void cargarAmigosPropios(int page) {
        if (currentUserProfile == null) {
            return;
        }
        
        String authId = currentUserProfile.getSafeAuthId();
        
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
        bI.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 8 15; -fx-cursor: hand;");
        
        Button bD = new Button(">"); 
        bD.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 8 15; -fx-cursor: hand;");
        
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
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(AppConstants.FXML_PROFILE));
            javafx.scene.Parent root = loader.load();
            
            ProfileController controller = loader.getController();
            controller.initData(amigo);
            
            nameLabel.getScene().setRoot(root);
        } catch (Exception e) {
        }
    }

    private void cargarEstadisticas(String authId) {
        if (authId == null) {
            return;
        }
        
        if (authId.isEmpty()) {
            return;
        }
        
        reviewService.getUserSummary(authId).thenAccept(summary -> {
            Platform.runLater(() -> {
                if (summary != null) {
                    reviewsCountLabel.setText(String.valueOf(summary.reviewsCount()));
                    likesReceivedLabel.setText(String.valueOf(summary.totalLikesReceived()));
                }
            });
        });

        friendsService.getUserSummary(authId).thenAccept(summary -> {
            Platform.runLater(() -> {
                if (summary != null) {
                    friendsCountLabel.setText(String.valueOf(summary.friendsCount()));
                }
            });
        });
    }

    private void toggleNameEdit(boolean editing) {
        nameDisplayBox.setVisible(!editing);
        nameDisplayBox.setManaged(!editing);
        nameEditBox.setVisible(editing);
        nameEditBox.setManaged(editing);
        
        if (editing) {
            nameField.setText(currentUserProfile.name());
        }
    }

    private void toggleUsernameEdit(boolean editing) {
        usernameDisplayBox.setVisible(!editing);
        usernameDisplayBox.setManaged(!editing);
        usernameEditBox.setVisible(editing);
        usernameEditBox.setManaged(editing);
        
        if (editing) {
            usernameField.setText(currentUserProfile.username());
        }
    }

    private void ejecutarActualizacion(UpdateProfileRequest req) {
        userService.updateProfile(req).thenAccept(updatedUser -> {
            Platform.runLater(() -> {
                SessionManager.getInstance().updateUser(updatedUser);
                initData(updatedUser);
            });
        }).exceptionally(e -> {
            Platform.runLater(() -> {
                mostrarAlertaError(AppConstants.MESSAGE_ERROR_API);
            });
            return null;
        });
    }
    
    

    private void subirFotoPerfil(java.io.File file) {
        userService.uploadProfilePhoto(file).thenAccept(updatedUser -> {
            Platform.runLater(() -> {
                SessionManager.getInstance().updateUser(updatedUser);
                initData(updatedUser);
                mostrarAlertaExito(AppConstants.MESSAGE_SUCCESS_PHOTO);
            });
        }).exceptionally(e -> {
            Platform.runLater(() -> {
                mostrarAlertaError(AppConstants.MESSAGE_ERROR_PHOTO);
            });
            return null;
        });
    }

    private void cargarFavoritos(boolean isCurrentUser) {
        CompletableFuture<List<LibraryItemDto>> future;
        
        if (isCurrentUser) {
            future = libraryService.getFavorites();
        } else {
            future = libraryService.getFavoritesByUser(currentUserProfile.getSafeAuthId());
        }

        future.thenAccept(list -> {
            Platform.runLater(() -> {
                procesarListaFavoritos(list);
            });
        }).exceptionally(e -> {
            Platform.runLater(() -> {
                mostrarVacio(favoritesSection, AppConstants.MESSAGE_ERROR_API);
            });
            return null;
        });
    }

    private void procesarListaFavoritos(List<LibraryItemDto> list) {
        if (list == null) {
            mostrarVacio(favoritesSection, "No hay series en favoritos.");
            return;
        }
        
        if (list.isEmpty()) {
            mostrarVacio(favoritesSection, "No hay series en favoritos.");
            return;
        }
        
        List<Integer> ids = new ArrayList<>();
        
        for (LibraryItemDto item : list) {
            if (item.tvmazeId() != null) {
                ids.add(item.tvmazeId());
            }
        }
        
        cargarSeriesEnCarrusel(ids, favoritesSection);
    }

    private void cargarWatchlist() {
        libraryService.getWatchlist().thenAccept(list -> {
            Platform.runLater(() -> {
                if (list == null) {
                    mostrarVacio(watchlistSection, "No tienes series en tu Watchlist.");
                    return;
                }
                
                if (list.isEmpty()) {
                    mostrarVacio(watchlistSection, "No tienes series en tu Watchlist.");
                    return;
                }
                
                List<Integer> ids = new ArrayList<>();
                
                for (LibraryItemDto item : list) {
                    if (item.tvmazeId() != null) {
                        ids.add(item.tvmazeId());
                    }
                }
                
                cargarSeriesEnCarrusel(ids, watchlistSection);
            });
        }).exceptionally(e -> {
            Platform.runLater(() -> {
                mostrarVacio(watchlistSection, AppConstants.MESSAGE_ERROR_API);
            });
            return null;
        });
    }

    private void cargarResenasPropias(int page, boolean isCurrentUser) {
        String authId = currentUserProfile.getSafeAuthId();
        
        if (authId == null) {
            mostrarVacio(reviewsSection, "ID de usuario no disponible.");
            return;
        }
        
        if (authId.isEmpty()) {
            mostrarVacio(reviewsSection, "ID de usuario no disponible.");
            return;
        }
        
        reviewService.getUserReviews(authId, page).thenAccept(res -> {
            Platform.runLater(() -> {
                procesarPaginacionResenas(res, isCurrentUser);
            });
        }).exceptionally(e -> {
            Platform.runLater(() -> {
                mostrarVacio(reviewsSection, AppConstants.MESSAGE_ERROR_API);
            });
            return null;
        });
    }

    private void procesarPaginacionResenas(ReviewPaginationResponse response, boolean isCurrentUser) {
        if (response == null) {
            if (currentReviewPage == 1) {
                mostrarVacio(reviewsSection, "No hay reseñas publicadas.");
            }
            return;
        }
        
        if (response.reviews() == null) {
            if (currentReviewPage == 1) {
                mostrarVacio(reviewsSection, "No hay reseñas publicadas.");
            }
            return;
        }
        
        if (response.reviews().isEmpty()) {
            if (currentReviewPage == 1) {
                mostrarVacio(reviewsSection, "No hay reseñas publicadas.");
            }
            return;
        }
        
        removerBotonCargarMas();
        
        for (ReviewDto r : response.reviews()) {
            VBox cardBox = buildReviewCard(r);
            reviewsSection.getChildren().add(cardBox);
        }
        
        if (response.pagination() != null) {
            if (response.pagination().hasNextPage() != null) {
                if (response.pagination().hasNextPage()) {
                    agregarBotonCargarMas(isCurrentUser);
                }
            }
        }
    }

    private void removerBotonCargarMas() {
        if (!reviewsSection.getChildren().isEmpty()) {
            int lastIndex = reviewsSection.getChildren().size() - 1;
            
            if (reviewsSection.getChildren().get(lastIndex) instanceof HBox) {
                reviewsSection.getChildren().remove(lastIndex);
            }
        }
    }

    private void agregarBotonCargarMas(boolean isCurrentUser) {
        Button btn = new Button("Cargar más reseñas");
        btn.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 4; -fx-padding: 8 15;");
        
        btn.setOnAction(e -> {
            currentReviewPage++;
            cargarResenasPropias(currentReviewPage, isCurrentUser);
        });
        
        HBox box = new HBox(btn);
        box.setAlignment(Pos.CENTER);
        
        reviewsSection.getChildren().add(box);
    }

    private void cargarSeriesEnCarrusel(List<Integer> ids, VBox container) {
        List<CompletableFuture<ShowFullResponse>> futures = new ArrayList<>();
        
        for (Integer id : ids) {
            futures.add(showService.getFullShowDetails(id));
        }
        
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenApply(v -> {
            List<Show> shows = new ArrayList<>();
            
            for (CompletableFuture<ShowFullResponse> f : futures) {
                try {
                    ShowFullResponse res = f.join();
                    
                    if (res != null) {
                        if (res.show() != null) {
                            shows.add(res.show());
                        }
                    }
                } catch (Exception e) {
                }
            }
            
            return shows;
        }).thenAccept(shows -> {
            Platform.runLater(() -> {
                dibujarCarrusel(shows, container);
            });
        });
    }

    private void dibujarCarrusel(List<Show> shows, VBox container) {
        container.getChildren().clear();
        
        if (shows.isEmpty()) {
            mostrarVacio(container, "Sin portadas disponibles.");
            return;
        }
        
        HBox content = new HBox(15);
        content.setPadding(new javafx.geometry.Insets(10));
        
        for (Show s : shows) {
            injectShowCard(s, content);
        }
        
        ScrollPane sp = new ScrollPane(content);
        sp.setFitToHeight(true);
        sp.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        
        Button bI = new Button("<"); 
        bI.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-cursor: hand;");
        
        Button bD = new Button(">"); 
        bD.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-cursor: hand;");
        
        bI.setOnAction(e -> {
            sp.setHvalue(Math.max(0, sp.getHvalue() - 0.2));
        });
        
        bD.setOnAction(e -> {
            sp.setHvalue(Math.min(1, sp.getHvalue() + 0.2));
        });
        
        BorderPane bp = new BorderPane(sp); 
        bp.setLeft(bI); 
        bp.setRight(bD);
        
        container.getChildren().add(bp);
    }

    private void mostrarVacio(VBox section, String msj) {
        Label lbl = new Label(msj);
        lbl.setTextFill(Color.GRAY);
        
        section.getChildren().clear();
        section.getChildren().add(lbl);
    }

    private void injectShowCard(Show s, HBox container) {
        try {
            FXMLLoader l = new FXMLLoader(getClass().getResource(AppConstants.FXML_SHOW_CARD));
            VBox card = l.load();
            
            com.src.filmtracker.controllers.shows.ShowCardController controller = l.getController();
            controller.setData(s);
            
            container.getChildren().add(card);
        } catch (IOException e) {
        }
    }

    private VBox buildReviewCard(ReviewDto review) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: #151515; -fx-padding: 15; -fx-background-radius: 8; -fx-border-color: #333;");
        
        Label seriesLabel = new Label("Cargando Serie...");
        seriesLabel.setTextFill(Color.web(AppConstants.COLOR_ACCENT));
        
        if (review.tvmaze_id() != null) {
            showService.getFullShowDetails(review.tvmaze_id()).thenAccept(res -> {
                if (res != null) {
                    if (res.show() != null) {
                        Platform.runLater(() -> {
                            seriesLabel.setText("Serie: " + res.show().name());
                        });
                    }
                }
            });
        }
        
        String titleText = "Sin título";
        
        if (review.title() != null) {
            titleText = review.title();
        }
        
        Label title = new Label(titleText);
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;"); 
        title.setTextFill(Color.WHITE);
        
        String contentText = "";
        
        if (review.content() != null) {
            contentText = review.content();
        }
        
        Label content = new Label(contentText);
        content.setTextFill(Color.LIGHTGRAY); 
        content.setWrapText(true);
        
        card.getChildren().add(seriesLabel);
        card.getChildren().add(title);
        card.getChildren().add(content);
        
        return card;
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