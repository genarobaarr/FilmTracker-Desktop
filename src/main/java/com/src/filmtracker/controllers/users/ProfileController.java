package com.src.filmtracker.controllers.users;

import com.src.filmtracker.App;
import com.src.filmtracker.models.admin.AccountStatusDto;
import com.src.filmtracker.models.library.LibraryItemDto;
import com.src.filmtracker.models.reviews.ReviewDto;
import com.src.filmtracker.models.reviews.ReviewPaginationResponse;
import com.src.filmtracker.models.shows.Show;
import com.src.filmtracker.models.shows.ShowFullResponse;
import com.src.filmtracker.models.users.UpdateProfileRequest;
import com.src.filmtracker.models.friends.FriendItemDto;
import com.src.filmtracker.models.friends.FriendPaginationResponse;
import com.src.filmtracker.models.friends.FriendStatusResponse;
import com.src.filmtracker.models.friends.SendFriendRequest;
import com.src.filmtracker.models.users.UserDto;
import com.src.filmtracker.services.admin.AdminService;
import com.src.filmtracker.services.admin.IAdminService;
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

import java.io.File;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

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
    @FXML private Button reportProfileBtn;
    @FXML private MenuButton adminMenu;
    @FXML private MenuItem itemQuitarFoto;
    @FXML private MenuItem itemSuspender;
    @FXML private MenuItem itemBanear;
    @FXML private MenuItem itemDesbanear;

    private final ILibraryService libraryService = new LibraryService();
    private final IShowService showService = new ShowService();
    private final IReviewService reviewService = new ReviewService();
    private final IUserService userService = new UserService();
    private final IFriendsService friendsService = new FriendsService();
    private final IAdminService adminService = new AdminService();
    
    private final Map<Integer, CompletableFuture<Show>> showCache = new ConcurrentHashMap<>();
    private final Map<String, String> translationMap = new LinkedHashMap<>();
    
    private int currentReviewPage = 1;
    private UserDto currentUserProfile;

    public ProfileController() {
    }

    @FXML
    public void initialize() {
        translationMap.put("1 día", "1_DAY");
        translationMap.put("3 días", "3_DAYS");
        translationMap.put("7 días", "7_DAYS");
        translationMap.put("30 días", "30_DAYS");
    }

    @FXML 
    private void handleEditName() { 
        toggleNameEdit(true); 
    }
    
    @FXML 
    private void handleCancelName() { 
        toggleNameEdit(false); 
    }
    
    @FXML 
    private void handleEditUsername() { 
        toggleUsernameEdit(true); 
    }
    
    @FXML 
    private void handleCancelUsername() { 
        toggleUsernameEdit(false); 
    }
    
    @FXML 
    private void handleChangePassword() { 
        App.setRoot(AppConstants.FXML_CHANGE_PASSWORD); 
    }
    
    @FXML 
    private void handleBack() { 
        App.goBackUniversal(); 
    }
    
    @FXML 
    private void handleMinimize() { 
        Stage stage = (Stage) nameLabel.getScene().getWindow();
        stage.setIconified(true); 
    }
    
    @FXML 
    private void handleClose() { 
        Platform.exit(); 
        System.exit(0); 
    }

    @FXML 
    private void handleSaveName() {
        String newName = nameField.getText().trim();
        
        if (newName.isEmpty()) {
            return;
        }
        
        UpdateProfileRequest req = new UpdateProfileRequest(newName, null, null);
        ejecutarActualizacion(req);
        toggleNameEdit(false);
    }

    @FXML 
    private void handleSaveUsername() {
        String newUsername = usernameField.getText().trim();
        
        if (newUsername.isEmpty()) {
            return;
        }
        
        ejecutarActualizacionUsername(newUsername);
        toggleUsernameEdit(false);
    }
    
    @FXML
    private void handleEditAvatar() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Seleccionar Foto de Perfil");
        
        javafx.stage.FileChooser.ExtensionFilter imageFilter = new javafx.stage.FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg");
        fileChooser.getExtensionFilters().add(imageFilter);
        
        File selectedFile = fileChooser.showOpenDialog(nameLabel.getScene().getWindow());
        
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
    
    @FXML
    private void handleReportProfile() {
        if (currentUserProfile == null) {
            return;
        }
        
        String targetId = currentUserProfile.getSafeAuthId();
        
        if (targetId == null) {
            return;
        }
        
        if (!targetId.isEmpty()) {
            com.src.filmtracker.utils.ReportModalHelper.openReportModal("USER", targetId);
        }
    }
    
    @FXML
    private void handleQuitarFoto() {
        if (confirmarAccion("Quitar Foto", "¿Deseas eliminar administrativamente la foto de perfil de este usuario?")) {
            ejecutarAccionAdmin(adminService.removeProfilePhotoDirectly(currentUserProfile.getSafeAuthId()));
        }
    }
    
    @FXML
    private void handleSuspender() {
        List<String> choices = new ArrayList<>(translationMap.keySet());
        ChoiceDialog<String> dialog = new ChoiceDialog<>(choices.get(0), choices);
        
        dialog.setTitle(AppConstants.MESSAGE_TITLE_SUSPEND);
        dialog.setHeaderText(AppConstants.MESSAGE_HEADER_SUSPEND);
        
        Optional<String> result = dialog.showAndWait();
        
        if (result.isPresent()) {
            String backendValue = translationMap.get(result.get());
            String reason = "Suspensión administrativa desde el perfil público.";
            
            ejecutarAccionAdmin(adminService.suspendUser(currentUserProfile.getSafeAuthId(), backendValue, reason));
        }
    }

    @FXML
    private void handleBanear() {
        if (confirmarAccion(AppConstants.MESSAGE_TITLE_BAN, AppConstants.MESSAGE_CONTENT_BAN)) {
            ejecutarAccionAdmin(adminService.banUser(currentUserProfile.getSafeAuthId(), "Violación de términos (Admin)"));
        }
    }

    @FXML
    private void handleDesbanear() {
        if (confirmarAccion(AppConstants.MESSAGE_TITLE_UNBAN, AppConstants.MESSAGE_CONTENT_UNBAN)) {
            ejecutarAccionAdmin(adminService.unbanUser(currentUserProfile.getSafeAuthId()));
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
        showCache.clear(); 
        
        cargarResenasPropias(1, esUsuarioActual(user));
        evaluarAccionesAdmin();
    }

    private CompletableFuture<Show> getShowCached(Integer tvmazeId) {
        if (showCache.containsKey(tvmazeId)) {
            return showCache.get(tvmazeId);
        }
        
        CompletableFuture<Show> future = showService.getShowDetails(tvmazeId);
        showCache.put(tvmazeId, future);
        
        return future;
    }

    private void actualizarEtiquetasBasicas(UserDto user) {
        String[] datosFusionados = fusionarDatosSesion(user);
        
        nameLabel.setText(datosFusionados[0] != null ? datosFusionados[0] : "");
        emailLabel.setText(datosFusionados[1] != null ? datosFusionados[1] : "");
        roleLabel.setText(datosFusionados[2] != null ? datosFusionados[2] : "");
        usernameLabel.setText("@" + (user.username() != null ? user.username() : ""));
        
        configurarFechaMiembro(datosFusionados[4]);
        configurarAvatar(user.username(), datosFusionados[3]);
    }

    private String[] fusionarDatosSesion(UserDto user) {
        String[] datos = new String[]{
            user.name(), 
            user.email(), 
            user.role(), 
            user.profileImage(), 
            user.createdAt()
        };
        
        if (esUsuarioActual(user)) {
            UserDto sessionUser = SessionManager.getInstance().getCurrentUser();
            
            if (sessionUser != null) {
                if (datos[0] == null) {
                    datos[0] = sessionUser.name();
                }
                
                if (datos[1] == null) {
                    datos[1] = sessionUser.email();
                }
                
                if (datos[2] == null) {
                    datos[2] = sessionUser.role();
                }
                
                if (datos[3] == null) {
                    datos[3] = sessionUser.profileImage();
                }
                
                if (datos[4] == null) {
                    datos[4] = sessionUser.createdAt();
                }
            }
        }
        
        return datos;
    }

    private void configurarFechaMiembro(String createdAt) {
        if (createdAt == null) {
            return;
        }
        
        try {
            ZonedDateTime dt = ZonedDateTime.parse(createdAt);
            dateLabel.setText("Miembro desde: " + dt.format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
        } catch (Exception e) {
            dateLabel.setText("Miembro desde: " + createdAt);
        }
    }

    private void configurarAvatar(String username, String profileImage) {
        String usr = username != null ? username : "user";
        String imageUrl = "https://ui-avatars.com/api/?name=" + usr + "&background=e50914&color=fff";
        
        if (profileImage != null) {
            if (!profileImage.isEmpty()) {
                imageUrl = profileImage;
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
            reportProfileBtn.setVisible(false);
            
            reviewsTitleLabel.setText("Mis Reseñas Publicadas");
            favoritesTitleLabel.setText("Mis Series Favoritas");
            
            cargarFavoritosRecursivo(true, 1, new ArrayList<>());
            cargarWatchlistRecursivo(1, new ArrayList<>());
            return;
        } 
        
        friendActionsBox.setVisible(true);
        friendActionsBox.setManaged(true);
        reportProfileBtn.setVisible(true);
        ocultarBotonesAmistad();
        
        reviewsTitleLabel.setText("Reseñas de @" + user.username());
        favoritesTitleLabel.setText("Favoritos de @" + user.username());
        
        cargarFavoritosRecursivo(false, 1, new ArrayList<>());
        cargarEstadoAmistad(user.getSafeAuthId());
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
        
        if (res.status() == null) {
            return;
        }
        
        String safeStatus = res.status().trim().toUpperCase();
        
        if (safeStatus.equals("NONE")) {
            configurarBotonAgregar("Agregar amigo", false);
            return;
        } 
        
        if (safeStatus.equals("FRIENDS")) {
            configurarBotonEliminar();
            return;
        } 
        
        if (safeStatus.equals("PENDING_OUTGOING")) {
            configurarBotonAgregar("Solicitud enviada", true);
            return;
        } 
        
        if (safeStatus.equals("PENDING_INCOMING")) {
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
        
        Button bI = crearBotonCarrusel("<"); 
        Button bD = crearBotonCarrusel(">"); 
        
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

    private Button crearBotonCarrusel(String texto) {
        Button btn = new Button(texto);
        btn.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 8 15; -fx-cursor: hand;");
        
        return btn;
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
            App.showProfileView(friend);
        });
        
        return box;
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
    
    private void evaluarAccionesAdmin() {
        UserDto loggedUser = SessionManager.getInstance().getCurrentUser();
        
        if (loggedUser == null) {
            return;
        }
        
        if (currentUserProfile == null) {
            return;
        }
        
        boolean isAdmin = "ADMIN".equals(loggedUser.role());
        boolean isSameUser = loggedUser.getSafeAuthId().equals(currentUserProfile.getSafeAuthId());
        
        if (isAdmin) {
            if (!isSameUser) {
                adminMenu.setVisible(true);
                adminMenu.setManaged(true);
                verificarEstadoCuentaAdmin();
            }
        }
    }

    private void verificarEstadoCuentaAdmin() {
        adminService.getAccountStatus(currentUserProfile.getSafeAuthId()).thenAccept(status -> {
            Platform.runLater(() -> {
                actualizarVisibilidadMenu(status);
            });
        }).exceptionally(e -> {
            return null;
        });
    }

    private void actualizarVisibilidadMenu(AccountStatusDto status) {
        if (status == null) {
            return;
        }
        
        boolean isBanned = "BANNED".equals(status.accountStatus());
        itemBanear.setVisible(!isBanned);
        itemSuspender.setVisible(!isBanned);
        itemDesbanear.setVisible(isBanned);
    }
    
    private boolean confirmarAccion(String titulo, String contenido) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(contenido);
        
        Optional<ButtonType> result = alert.showAndWait();
        
        if (result.isPresent()) {
            if (result.get() == ButtonType.OK) {
                return true;
            }
        }
        
        return false;
    }

    private void ejecutarAccionAdmin(CompletableFuture<Void> futuro) {
        futuro.thenRun(() -> {
            Platform.runLater(() -> {
                mostrarAlertaExito(AppConstants.MESSAGE_SUCCESS_ADMIN_ACTION);
                verificarEstadoCuentaAdmin();
            });
        }).exceptionally(err -> {
            Platform.runLater(() -> {
                mostrarAlertaError(AppConstants.MESSAGE_ERROR_API);
            });
            return null;
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
                this.currentUserProfile = updatedUser;
                actualizarEtiquetasBasicas(updatedUser);
                mostrarAlertaExito("Perfil actualizado correctamente.");
            });
        }).exceptionally(e -> {
            Platform.runLater(() -> {
                mostrarAlertaError(AppConstants.MESSAGE_ERROR_API);
            });
            return null;
        });
    }
    
    private void ejecutarActualizacionUsername(String newUsername) {
        String url = AppConstants.AUTH_SERVICE_URL + "/username";
        String jsonBody = "{\"username\":\"" + newUsername + "\"}";
        
        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
            .uri(java.net.URI.create(url))
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .header("Authorization", "Bearer " + SessionManager.getInstance().getToken())
            .PUT(java.net.http.HttpRequest.BodyPublishers.ofString(jsonBody))
            .build();
            
        java.net.http.HttpClient.newHttpClient().sendAsync(request, java.net.http.HttpResponse.BodyHandlers.ofString())
            .thenAccept(this::procesarRespuestaUsername)
            .exceptionally(e -> {
                Platform.runLater(() -> {
                    mostrarAlertaError(AppConstants.MESSAGE_ERROR_API);
                });
                return null;
            });
    }

    private void procesarRespuestaUsername(java.net.http.HttpResponse<String> response) {
        if (response.statusCode() >= 400) {
            Platform.runLater(() -> {
                mostrarAlertaError("Error al actualizar usuario. Puede que el nombre ya esté en uso.");
            });
            return;
        }
        
        userService.getProfile().thenAccept(updatedUser -> {
            Platform.runLater(() -> {
                if (updatedUser != null) {
                    SessionManager.getInstance().updateUser(updatedUser);
                    this.currentUserProfile = updatedUser;
                    actualizarEtiquetasBasicas(updatedUser);
                    mostrarAlertaExito("Nombre de usuario actualizado correctamente.");
                }
            });
        }).exceptionally(e -> {
            Platform.runLater(() -> {
                mostrarAlertaError(AppConstants.MESSAGE_ERROR_API);
            });
            return null;
        });
    }

    private void subirFotoPerfil(File file) {
        userService.uploadProfilePhoto(file).thenAccept(updatedUser -> {
            Platform.runLater(() -> {
                SessionManager.getInstance().updateUser(updatedUser);
                this.currentUserProfile = updatedUser;
                actualizarEtiquetasBasicas(updatedUser);
                mostrarAlertaExito(AppConstants.MESSAGE_SUCCESS_PHOTO);
            });
        }).exceptionally(e -> {
            Platform.runLater(() -> {
                mostrarAlertaError(AppConstants.MESSAGE_ERROR_PHOTO);
            });
            return null;
        });
    }

    private void cargarFavoritosRecursivo(boolean isCurrentUser, int page, List<LibraryItemDto> acumulado) {
        CompletableFuture<List<LibraryItemDto>> future;
        
        if (isCurrentUser) {
            future = libraryService.getFavoritesPaged(page);
        } else {
            future = libraryService.getFavoritesByUserPaged(currentUserProfile.getSafeAuthId(), page);
        }
        
        future.thenAccept(list -> {
            if (list != null) {
                if (!list.isEmpty()) {
                    acumulado.addAll(list);
                    if (list.size() == 10) {
                        if (page < 3) {
                            cargarFavoritosRecursivo(isCurrentUser, page + 1, acumulado);
                            return;
                        }
                    }
                }
            }
            
            Platform.runLater(() -> {
                procesarListaBibliotecas(acumulado, favoritesSection, "No hay series en favoritos.");
            });
        }).exceptionally(e -> {
            Platform.runLater(() -> {
                mostrarVacio(favoritesSection, AppConstants.MESSAGE_ERROR_API);
            });
            return null;
        });
    }

    private void cargarWatchlistRecursivo(int page, List<LibraryItemDto> acumulado) {
        libraryService.getWatchlistPaged(page).thenAccept(list -> {
            if (list != null) {
                if (!list.isEmpty()) {
                    acumulado.addAll(list);
                    if (list.size() == 10) {
                        if (page < 3) {
                            cargarWatchlistRecursivo(page + 1, acumulado);
                            return;
                        }
                    }
                }
            }
            
            Platform.runLater(() -> {
                procesarListaBibliotecas(acumulado, watchlistSection, "No tienes series en tu Watchlist.");
            });
        }).exceptionally(e -> {
            Platform.runLater(() -> {
                mostrarVacio(watchlistSection, AppConstants.MESSAGE_ERROR_API);
            });
            return null;
        });
    }

    private void procesarListaBibliotecas(List<LibraryItemDto> list, VBox container, String emptyMsg) {
        if (list == null) {
            mostrarVacio(container, emptyMsg);
            return;
        }
        
        if (list.isEmpty()) {
            mostrarVacio(container, emptyMsg);
            return;
        }
        
        List<Integer> ids = new ArrayList<>();
        int limite = Math.min(list.size(), 30);
        
        for (int i = 0; i < limite; i++) {
            LibraryItemDto item = list.get(i);
            
            if (item.tvmazeId() != null) {
                ids.add(item.tvmazeId());
            }
        }
        
        cargarSeriesEnCarrusel(ids, container);
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
        List<CompletableFuture<Show>> futures = new ArrayList<>();
        
        for (Integer id : ids) {
            futures.add(getShowCached(id));
        }
        
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenApply(v -> {
            List<Show> shows = new ArrayList<>();
            
            for (CompletableFuture<Show> f : futures) {
                try {
                    Show s = f.join();
                    
                    if (s != null) {
                        shows.add(s);
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
        content.setPadding(new Insets(10));
        
        for (Show s : shows) {
            injectShowCard(s, content);
        }
        
        ScrollPane sp = new ScrollPane(content);
        sp.setFitToHeight(true);
        sp.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        
        Button bI = crearBotonPaginacion("<"); 
        Button bD = crearBotonPaginacion(">"); 
        
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
        
        container.getChildren().add(bp);
    }

    private Button crearBotonPaginacion(String texto) {
        Button btn = new Button(texto);
        btn.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-cursor: hand;");
        
        return btn;
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
        card.setStyle("-fx-background-color: #151515; -fx-padding: 15; -fx-background-radius: 8; -fx-border-color: #333; -fx-cursor: hand;");
        
        Label seriesLabel = new Label("Cargando Serie...");
        seriesLabel.setTextFill(Color.web(AppConstants.COLOR_ACCENT));
        
        if (review.tvmaze_id() != null) {
            getShowCached(review.tvmaze_id()).thenAccept(show -> {
                if (show != null) {
                    Platform.runLater(() -> {
                        seriesLabel.setText("Serie: " + show.name());
                    });
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
        
        card.setOnMouseClicked(e -> {
            if (review.tvmaze_id() != null) {
                getShowCached(review.tvmaze_id()).thenAccept(show -> {
                    Platform.runLater(() -> {
                        if (show != null) {
                            App.showShowDetail(show);
                        }
                    });
                }).exceptionally(ex -> {
                    return null;
                });
            }
        });
        
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