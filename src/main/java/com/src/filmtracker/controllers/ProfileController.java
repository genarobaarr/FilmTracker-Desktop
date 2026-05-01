package com.src.filmtracker.controllers;

import com.src.filmtracker.App;
import com.src.filmtracker.models.*;
import com.src.filmtracker.services.*;
import com.src.filmtracker.utils.AppConstants;
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
    
    @FXML private VBox favoritesSection;
    @FXML private VBox watchlistSection;
    @FXML private VBox reviewsSection;

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
        emailLabel.setText(user.email());
        roleLabel.setText(user.role());
        
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

        cargarFavoritos();
        cargarWatchlist();
        
        reviewsSection.getChildren().clear();
        cargarResenasPropias(1);
    }

    private void cargarFavoritos() {
        libraryService.getFavorites().thenAccept(list -> {
            Platform.runLater(() -> {
                if (list == null) {
                    mostrarVacio(favoritesSection, "No tienes series en favoritos.");
                    return;
                }
                if (list.isEmpty()) {
                    mostrarVacio(favoritesSection, "No tienes series en favoritos.");
                    return;
                }
                
                List<Integer> ids = new ArrayList<>();
                for (LibraryItemDto item : list) {
                    ids.add(item.tvmaze_id());
                }
                cargarSeriesEnCarrusel(ids, favoritesSection);
            });
        }).exceptionally(e -> {
            Platform.runLater(() -> {
                mostrarVacio(favoritesSection, AppConstants.MESSAGE_ERROR_API);
            });
            return null;
        });
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
                    ids.add(item.tvmaze_id());
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

    private void cargarResenasPropias(int page) {
        String authId = currentUserProfile.id();
        if (currentUserProfile.authId() != null) {
            authId = currentUserProfile.authId();
        }
        
        reviewService.getUserReviews(authId, page).thenAccept(res -> {
            Platform.runLater(() -> {
                procesarPaginacionResenas(res);
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

    private void procesarPaginacionResenas(ReviewPaginationResponse response) {
        if (response == null) {
            if (currentReviewPage == 1) {
                mostrarVacio(reviewsSection, "No has escrito ninguna reseña.");
            }
            return;
        }
        
        List<ReviewDto> reviews = response.reviews();
        if (reviews == null) {
            if (currentReviewPage == 1) {
                mostrarVacio(reviewsSection, "No has escrito ninguna reseña.");
            }
            return;
        }
        if (reviews.isEmpty()) {
            if (currentReviewPage == 1) {
                mostrarVacio(reviewsSection, "No has escrito ninguna reseña.");
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
                    agregarBotonCargarMas();
                }
            }
        }
    }

    private void agregarBotonCargarMas() {
        Button btnMore = new Button("Cargar más reseñas");
        btnMore.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 4; -fx-padding: 8 15;");
        
        btnMore.setOnAction(e -> {
            currentReviewPage++;
            cargarResenasPropias(currentReviewPage);
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
            ((ShowCardController) l.getController()).setData(s);
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

        card.getChildren().addAll(seriesLabel, title, content);
        return card;
    }

    @FXML private void handleBack() {
        App.setRoot(AppConstants.FXML_DASHBOARD);
    }
}