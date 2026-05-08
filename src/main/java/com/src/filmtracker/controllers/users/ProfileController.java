package com.src.filmtracker.controllers.users;

import com.src.filmtracker.App;
import com.src.filmtracker.models.library.LibraryItemDto;
import com.src.filmtracker.models.reviews.ReviewDto;
import com.src.filmtracker.models.reviews.ReviewPaginationResponse;
import com.src.filmtracker.models.shows.Show;
import com.src.filmtracker.models.shows.ShowFullResponse;
import com.src.filmtracker.models.users.UserDto;
import com.src.filmtracker.services.library.ILibraryService;
import com.src.filmtracker.services.library.LibraryService;
import com.src.filmtracker.services.reviews.IReviewService;
import com.src.filmtracker.services.reviews.ReviewService;
import com.src.filmtracker.services.shows.IShowService;
import com.src.filmtracker.services.shows.ShowService;
import com.src.filmtracker.utils.AppConstants;
import com.src.filmtracker.utils.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javafx.scene.image.ImageView;

public class ProfileController {
    
    @FXML private ImageView avatarView;
    @FXML private Label nameLabel;
    @FXML private Label usernameLabel;
    @FXML private Label emailLabel;
    @FXML private Label roleLabel;
    @FXML private Label dateLabel;
    
    @FXML private VBox privateInfoContainer;
    
    @FXML private VBox favoritesContainer;
    @FXML private VBox favoritesSection;
    @FXML private Label favoritesTitleLabel;
    
    @FXML private VBox watchlistContainer;
    @FXML private VBox watchlistSection;
    
    @FXML private VBox reviewsSection;
    @FXML private Label reviewsTitleLabel;

    private final ILibraryService libraryService = new LibraryService();
    private final IShowService showService = new ShowService();
    private final IReviewService reviewService = new ReviewService();
    
    private int currentReviewPage = 1;
    private UserDto currentUserProfile;

    public void initData(UserDto user) {
        if (user == null) {
            return;
        }
        
        this.currentUserProfile = user;
        
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

        boolean isCurrentUser = esUsuarioActual(user);
        configurarVisibilidadPublica(isCurrentUser, user);
        
        reviewsSection.getChildren().clear();
        cargarResenasPropias(1, isCurrentUser);
    }

    private boolean esUsuarioActual(UserDto user) {
        UserDto loggedInUser = SessionManager.getInstance().getCurrentUser();
        
        if (loggedInUser != null) {
            if (loggedInUser.username() != null) {
                if (loggedInUser.username().equals(user.username())) {
                    return true;
                }
            }
        }
        
        return false;
    }

    private void configurarVisibilidadPublica(boolean isCurrentUser, UserDto user) {
        if (isCurrentUser) {
            configurarVistaPropia();
        } else {
            configurarVistaTercero(user);
        }
    }

    private void configurarVistaPropia() {
        watchlistContainer.setVisible(true);
        watchlistContainer.setManaged(true);
        
        favoritesContainer.setVisible(true);
        favoritesContainer.setManaged(true);
        
        privateInfoContainer.setVisible(true);
        privateInfoContainer.setManaged(true);
        
        reviewsTitleLabel.setText("Mis Reseñas Publicadas");
        favoritesTitleLabel.setText("Mis Series Favoritas");
        
        cargarFavoritos(true);
        cargarWatchlist();
    }

    private void configurarVistaTercero(UserDto user) {
        watchlistContainer.setVisible(false);
        watchlistContainer.setManaged(false);
        
        favoritesContainer.setVisible(true);
        favoritesContainer.setManaged(true);
        
        privateInfoContainer.setVisible(false);
        privateInfoContainer.setManaged(false);
        
        reviewsTitleLabel.setText("Reseñas de @" + user.username());
        favoritesTitleLabel.setText("Favoritos de @" + user.username());
        
        cargarFavoritos(false);
    }

    private void cargarFavoritos(boolean isCurrentUser) {
        if (isCurrentUser) {
            libraryService.getFavorites().thenAccept(list -> {
                Platform.runLater(() -> {
                    procesarListaFavoritos(list);
                });
            }).exceptionally(e -> {
                Platform.runLater(() -> {
                    mostrarVacio(favoritesSection, AppConstants.MESSAGE_ERROR_API);
                });
                return null;
            });
        } else {
            String authId = currentUserProfile.authId();
            
            if (authId == null || authId.isEmpty()) {
                Platform.runLater(() -> {
                    mostrarVacio(favoritesSection, "Privacidad: Favoritos ocultos (Falta authId público desde el servidor).");
                });
                return;
            }
            
            libraryService.getFavoritesByUser(authId).thenAccept(list -> {
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
        String authId = currentUserProfile.authId();
        
        if (!isCurrentUser) {
            if (authId == null || authId.isEmpty()) {
                Platform.runLater(() -> {
                    if (page == 1) {
                        mostrarVacio(reviewsSection, "Privacidad: Reseñas ocultas (Falta authId público desde el servidor).");
                    }
                });
                return;
            }
        } else {
            if (authId == null || authId.isEmpty()) {
                authId = currentUserProfile.id();
            }
        }
        
        reviewService.getUserReviews(authId, page).thenAccept(res -> {
            Platform.runLater(() -> {
                procesarPaginacionResenas(res, isCurrentUser);
            });
        }).exceptionally(e -> {
            Platform.runLater(() -> {
                if (page == 1) {
                    mostrarVacio(reviewsSection, AppConstants.MESSAGE_ERROR_API);
                }
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
        
        List<ReviewDto> reviews = response.reviews();
        
        if (reviews == null) {
            if (currentReviewPage == 1) {
                mostrarVacio(reviewsSection, "No hay reseñas publicadas.");
            }
            return;
        }
        
        if (reviews.isEmpty()) {
            if (currentReviewPage == 1) {
                mostrarVacio(reviewsSection, "No hay reseñas publicadas.");
            }
            return;
        }
        
        if (reviewsSection.getChildren().size() > 0) {
            int lastIndex = reviewsSection.getChildren().size() - 1;
            
            if (reviewsSection.getChildren().get(lastIndex) instanceof Button) {
                reviewsSection.getChildren().remove(lastIndex);
            }
        }
        
        for (ReviewDto r : reviews) {
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

    private void agregarBotonCargarMas(boolean isCurrentUser) {
        Button btnMore = new Button("Cargar más reseñas");
        btnMore.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 4; -fx-padding: 8 15;");
        
        btnMore.setOnAction(e -> {
            currentReviewPage++;
            cargarResenasPropias(currentReviewPage, isCurrentUser);
        });
        
        HBox centerBox = new HBox(btnMore);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setPadding(new Insets(10, 0, 10, 0));
        
        reviewsSection.getChildren().add(centerBox);
    }

    private void cargarSeriesEnCarrusel(List<Integer> ids, VBox container) {
        List<CompletableFuture<ShowFullResponse>> futures = new ArrayList<>();
        
        for (Integer id : ids) {
            futures.add(showService.getFullShowDetails(id));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> {
                return mapearResultadosShows(futures);
            })
            .thenAccept(shows -> {
                Platform.runLater(() -> {
                    dibujarCarrusel(shows, container);
                });
            });
    }

    private List<Show> mapearResultadosShows(List<CompletableFuture<ShowFullResponse>> futures) {
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
    }

    private void dibujarCarrusel(List<Show> shows, VBox container) {
        container.getChildren().clear();
        
        if (shows.isEmpty()) {
            mostrarVacio(container, "Hubo un error al cargar las portadas de las series.");
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

        String btnStyle = "-fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 8 15; -fx-cursor: hand; -fx-background-radius: 3;";
        
        Button bI = new Button("<"); 
        bI.setStyle(btnStyle);
        
        Button bD = new Button(">"); 
        bD.setStyle(btnStyle);
        
        bI.setOnAction(e -> {
            moverCarruselDinamico(sp, -1);
        }); 
        
        bD.setOnAction(e -> {
            moverCarruselDinamico(sp, 1);
        });
        
        BorderPane bp = new BorderPane(sp); 
        bp.setLeft(bI); 
        bp.setRight(bD);
        
        BorderPane.setAlignment(bI, Pos.CENTER); 
        BorderPane.setAlignment(bD, Pos.CENTER);
        
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
            ((com.src.filmtracker.controllers.shows.ShowCardController) l.getController()).setData(s);
            container.getChildren().add(card);
        } catch (IOException e) { 
        }
    }

    private void moverCarruselDinamico(ScrollPane sp, int dir) {
        sp.setHvalue(Math.max(0, Math.min(sp.getHvalue() + (dir * 0.2), 1)));
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
                        if (res.show().name() != null) {
                            Platform.runLater(() -> {
                                seriesLabel.setText("Serie: " + res.show().name());
                            });
                        }
                    }
                }
            });
        }
        
        String tText = "Sin título";
        if (review.title() != null) {
            tText = review.title();
        }
        
        Label title = new Label(tText);
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;"); 
        title.setTextFill(Color.WHITE);
        
        String cText = "";
        if (review.content() != null) {
            cText = review.content();
        }
        
        Label content = new Label(cText);
        content.setTextFill(Color.LIGHTGRAY); 
        content.setWrapText(true);

        card.getChildren().add(seriesLabel);
        card.getChildren().add(title);
        card.getChildren().add(content);
        
        return card;
    }

    @FXML private void handleBack() {
        App.setRoot(AppConstants.FXML_DASHBOARD);
    }
}