package com.src.filmtracker.controllers;

import com.src.filmtracker.App;
import com.src.filmtracker.models.*;
import com.src.filmtracker.services.*;
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
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class ShowDetailController {

    @FXML private Label titleLabel;
    @FXML private Label ratingLabel;
    @FXML private Label statusLabel;
    @FXML private Label genresLabel;
    @FXML private Label summaryLabel;
    @FXML private ImageView posterDetail;
    @FXML private HBox castContainer;
    @FXML private VBox episodesContainer;
    @FXML private VBox similarShowsSection;
    @FXML private VBox reviewsSection; 
    @FXML private ScrollPane scrollCast; 

    private final IShowService apiService = new ShowService();
    private final IReviewService reviewService = new ReviewService();
    private final IUserService userService = new UserService();
    private final ILibraryService libraryService = new LibraryService();
    
    private final Map<Integer, List<EpisodeDto>> seasonEpisodesMap = new ConcurrentHashMap<>();
    private final Map<String, String> usernameCache = new ConcurrentHashMap<>();
    
    private Integer currentTvmazeId;

    @FXML private void handleClose() {
        Platform.exit(); 
        System.exit(0); 
    }
    
    @FXML private void handleMinimize() {
        ((Stage) titleLabel.getScene().getWindow()).setIconified(true); 
    }
    
    @FXML private void handleBack() {
        App.goBackFromDetail(); 
    }
    
    @FXML private void handleHome() {
        App.setRoot(AppConstants.FXML_DASHBOARD); 
    }
    
    @FXML private void scrollIzqCast() {
        moverCarruselDinamico(scrollCast, -1); 
    }
    
    @FXML private void scrollDerCast() {
        moverCarruselDinamico(scrollCast, 1); 
    }
    
    public void initData(Show basicShow) {
        if (basicShow == null) {
            return;
        }
        if (basicShow.tvmazeId() == null) {
            return;
        }
        
        this.currentTvmazeId = basicShow.tvmazeId();
        cargarDatosBasicosUI(basicShow);
        inyectarBotonesLibreria();

        apiService.getFullShowDetails(currentTvmazeId).thenAccept(fullData -> {
            Platform.runLater(() -> {
                if (fullData == null) {
                    return;
                }
                cargarDatosBasicosUI(fullData.show()); 
                actualizarCastUI(fullData.cast());
                actualizarTemporadasUI(fullData.seasons());
                
                if (fullData.show() != null) {
                    if (fullData.show().genres() != null) {
                        cargarSeriesSimilaresMultigenero(fullData.show().genres());
                    }
                }
            });
        }).exceptionally(e -> {
            return null; 
        });

        apiService.getShowEpisodes(currentTvmazeId).thenAccept(episodes -> {
            if (episodes != null) {
                for (EpisodeDto ep : episodes) {
                    seasonEpisodesMap.computeIfAbsent(ep.season(), k -> new ArrayList<>()).add(ep);
                }
            }
        });

        cargarResenas();
    }

    private void cargarDatosBasicosUI(Show show) {
        if (show == null) {
            return; 
        }
        
        titleLabel.setText(show.name() != null ? show.name() : "Desconocido");
        statusLabel.setText("Status: " + (show.status() != null ? show.status() : "N/A"));
        
        if (show.genres() != null) {
            genresLabel.setText("Géneros: " + String.join(", ", show.genres()));
        }
        
        String rating = "N/A";
        if (show.rating() != null) {
            if (show.rating().average() != null) {
                rating = String.valueOf(show.rating().average());
            }
        }
        ratingLabel.setText("⭐ " + rating);
        
        if (show.summary() != null) {
            summaryLabel.setText(show.summary().replaceAll("<[^>]*>", ""));
        }
        
        if (show.image() != null) {
            if (show.image().original() != null) {
                posterDetail.setImage(new Image(show.image().original(), true));
            }
        }
    }

    private void inyectarBotonesLibreria() {
        if (!SessionManager.getInstance().isAuthenticated()) {
            return;
        }
        
        HBox libBox = new HBox(15);
        libBox.setAlignment(Pos.CENTER_LEFT);
        
        Button favBtn = new Button("Cargando...");
        favBtn.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 4;");
        
        Button watchBtn = new Button("Cargando...");
        watchBtn.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 4;");
        
        libBox.getChildren().addAll(favBtn, watchBtn);
        
        VBox infoPadre = (VBox) titleLabel.getParent();
        infoPadre.getChildren().add(libBox);
        
        configurarBtnFavoritos(favBtn);
        configurarBtnWatchlist(watchBtn);
    }

    private void configurarBtnFavoritos(Button btn) {
        libraryService.getFavorites().thenAccept(list -> {
            boolean isFav = false;
            for (LibraryItemDto item : list) {
                if (item.tvmaze_id().equals(currentTvmazeId)) {
                    isFav = true;
                    break;
                }
            }
            final boolean finalFav = isFav;
            Platform.runLater(() -> {
                actualizarAparienciaFav(btn, finalFav);
                btn.setOnAction(e -> {
                    handleToggleFav(btn);
                });
            });
        });
    }

    private void handleToggleFav(Button btn) {
        boolean currFav = btn.getText().contains("Quitar");
        actualizarAparienciaFav(btn, !currFav);
        
        if (currFav) {
            libraryService.removeFavorite(currentTvmazeId).exceptionally(err -> {
                Platform.runLater(() -> {
                    actualizarAparienciaFav(btn, true);
                    mostrarAlertaError(AppConstants.MESSAGE_ERROR_API);
                });
                return null;
            });
        } else {
            libraryService.addFavorite(currentTvmazeId).exceptionally(err -> {
                Platform.runLater(() -> {
                    actualizarAparienciaFav(btn, false);
                    if (err.toString().contains("409")) {
                        mostrarAlertaError(AppConstants.MESSAGE_ERROR_DUPLICATE_LIB);
                    } else {
                        mostrarAlertaError(AppConstants.MESSAGE_ERROR_API);
                    }
                });
                return null;
            });
        }
    }

    private void actualizarAparienciaFav(Button btn, boolean isFav) {
        if (isFav) {
            btn.setText("Quitar de Favoritos (💔)");
            btn.setStyle("-fx-background-color: #e50914; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 4;");
        } else {
            btn.setText("Añadir a Favoritos (️❤️)");
            btn.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 4;");
        }
    }

    private void configurarBtnWatchlist(Button btn) {
        libraryService.getWatchlist().thenAccept(list -> {
            boolean isWatch = false;
            for (LibraryItemDto item : list) {
                if (item.tvmaze_id().equals(currentTvmazeId)) {
                    isWatch = true;
                    break;
                }
            }
            final boolean finalWatch = isWatch;
            Platform.runLater(() -> {
                actualizarAparienciaWatch(btn, finalWatch);
                btn.setOnAction(e -> {
                    handleToggleWatch(btn);
                });
            });
        });
    }

    private void handleToggleWatch(Button btn) {
        boolean currWatch = btn.getText().contains("Quitar");
        actualizarAparienciaWatch(btn, !currWatch);
        
        if (currWatch) {
            libraryService.removeWatchlist(currentTvmazeId).exceptionally(err -> {
                Platform.runLater(() -> {
                    actualizarAparienciaWatch(btn, true);
                    mostrarAlertaError(AppConstants.MESSAGE_ERROR_API);
                });
                return null;
            });
        } else {
            libraryService.addWatchlist(currentTvmazeId).exceptionally(err -> {
                Platform.runLater(() -> {
                    actualizarAparienciaWatch(btn, false);
                    if (err.toString().contains("409")) {
                        mostrarAlertaError(AppConstants.MESSAGE_ERROR_DUPLICATE_LIB);
                    } else {
                        mostrarAlertaError(AppConstants.MESSAGE_ERROR_API);
                    }
                });
                return null;
            });
        }
    }

    private void actualizarAparienciaWatch(Button btn, boolean isWatch) {
        if (isWatch) {
            btn.setText("Quitar de Watchlist (️➖)");
            btn.setStyle("-fx-background-color: #e50914; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 4;");
        } else {
            btn.setText("Añadir a Watchlist (➕)");
            btn.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 4;");
        }
    }

    private void actualizarCastUI(List<CastDto> cast) {
        castContainer.getChildren().clear();
        scrollCast.setHvalue(0.0);
        if (cast != null) {
            for (CastDto m : cast) {
                castContainer.getChildren().add(buildPersonBox(m));
            }
        }
    }

    private void actualizarTemporadasUI(List<SeasonDto> seasons) {
        episodesContainer.getChildren().clear();
        if (seasons != null) {
            for (SeasonDto s : seasons) {
                episodesContainer.getChildren().add(createSeasonAccordion(s));
            }
        }
    }

    private void cargarSeriesSimilaresMultigenero(List<String> genres) {
        if (genres == null) {
            return;
        }
        if (genres.isEmpty()) {
            return;
        }
        
        List<CompletableFuture<List<Show>>> futures = new ArrayList<>();
        for (String g : genres) {
            futures.add(apiService.getShowsByGenre(g));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> {
                return mapearShowsSimilares(futures);
            })
            .thenAccept(combined -> {
                Collections.shuffle(combined);
                List<Show> limited = combined.stream().limit(AppConstants.HOME_CAROUSEL_LIMIT).toList();
                Platform.runLater(() -> {
                    renderSimilarCarousel(limited);
                });
            });
    }

    private List<Show> mapearShowsSimilares(List<CompletableFuture<List<Show>>> futures) {
        List<Show> result = new ArrayList<>();
        Set<Integer> seen = new HashSet<>();
        
        for (CompletableFuture<List<Show>> f : futures) {
            List<Show> list = f.join();
            for (Show s : list) {
                if (s.tvmazeId() != null) {
                    if (!s.tvmazeId().equals(currentTvmazeId)) {
                        if (seen.add(s.tvmazeId())) {
                            result.add(s);
                        }
                    }
                }
            }
        }
        return result;
    }

    private void renderSimilarCarousel(List<Show> shows) {
        similarShowsSection.getChildren().clear();
        if (shows == null) {
            mostrarVacio(similarShowsSection, AppConstants.MESSAGE_INFO_NO_SIMILAR);
            return;
        }
        if (shows.isEmpty()) {
            mostrarVacio(similarShowsSection, AppConstants.MESSAGE_INFO_NO_SIMILAR);
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
        similarShowsSection.getChildren().add(bp);
    }

    private void mostrarVacio(VBox section, String msg) {
        Label lbl = new Label(msg);
        lbl.setTextFill(Color.GRAY);
        section.getChildren().add(lbl);
    }

    private VBox createSeasonAccordion(SeasonDto s) {
        VBox c = new VBox();
        HBox h = new HBox(new Label("Temp " + s.number()));
        h.setStyle("-fx-background-color: #1e1e1e; -fx-padding: 10; -fx-cursor: hand; -fx-text-fill: white;");
        
        VBox eps = new VBox(); 
        eps.setVisible(false); 
        eps.setManaged(false);
        eps.setStyle("-fx-padding: 15; -fx-background-color: #151515; -fx-background-radius: 0 0 5 5;");
        
        h.setOnMouseClicked(e -> {
            eps.setVisible(!eps.isVisible()); 
            eps.setManaged(eps.isVisible());
            if (eps.isVisible()) {
                if (eps.getChildren().isEmpty()) {
                    renderEpisodes(s.number(), eps);
                }
            }
        });
        c.getChildren().addAll(h, eps);
        return c;
    }

    private void renderEpisodes(int num, VBox container) {
        container.getChildren().clear();
        List<EpisodeDto> list = seasonEpisodesMap.get(num);
        
        if (list == null) {
            mostrarVacio(container, "No hay episodios disponibles para esta temporada.");
            return;
        }
        if (list.isEmpty()) {
            mostrarVacio(container, "No hay episodios disponibles para esta temporada.");
            return;
        }

        HBox carouselContent = new HBox(15);
        carouselContent.setPadding(new Insets(10));
        
        for (EpisodeDto ep : list) {
            carouselContent.getChildren().add(buildEpisodeCard(ep));
        }

        ScrollPane sp = new ScrollPane(carouselContent);
        sp.setFitToHeight(true);
        sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        String btnStyle = "-fx-background-color: #1e1e1e; -fx-text-fill: white; -fx-font-size: 18px; -fx-cursor: hand;";
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

        BorderPane interactiveCarousel = new BorderPane(sp);
        interactiveCarousel.setLeft(bI); 
        interactiveCarousel.setRight(bD);
        BorderPane.setAlignment(bI, Pos.CENTER); 
        BorderPane.setAlignment(bD, Pos.CENTER);

        container.getChildren().add(interactiveCarousel);
    }

    private VBox buildEpisodeCard(EpisodeDto ep) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: #222222; -fx-background-radius: 8; -fx-padding: 12;");
        card.setPrefWidth(260); 
        card.setMaxWidth(260);

        ImageView iv = new ImageView();
        iv.setFitWidth(236); 
        iv.setFitHeight(133);
        if (ep.image() != null) {
            if (ep.image().medium() != null) {
                iv.setImage(new Image(ep.image().medium(), true));
            }
        }

        String name = "Desconocido";
        if (ep.name() != null) {
            name = ep.name();
        }
        Label title = new Label(String.format("S%dE%d - %s", ep.season(), ep.number(), name));
        title.setTextFill(Color.WHITE);
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        title.setWrapText(true);

        String infoText = "N/A";
        if (ep.airdate() != null) {
            infoText = ep.airdate();
        }
        if (ep.runtime() != null) {
            infoText += " | " + ep.runtime() + " min";
        }
        Label info = new Label(infoText);
        info.setTextFill(Color.web("#aaaaaa")); 
        info.setStyle("-fx-font-size: 12px;");

        Label summary = new Label();
        if (ep.summary() != null) {
            summary.setText(ep.summary().replaceAll("<[^>]*>", ""));
        } else {
            summary.setText("Sin descripción disponible.");
        }
        summary.setTextFill(Color.web("#cccccc"));
        summary.setWrapText(true);
        summary.setPrefHeight(60); 
        summary.setTextAlignment(TextAlignment.JUSTIFY);

        card.getChildren().addAll(iv, title, info, summary);
        return card;
    }

    private void cargarResenas() {
        reviewService.getShowReviews(currentTvmazeId).thenAccept(reviews -> {
            Platform.runLater(() -> {
                dibujarSeccionResenas(reviews, false);
            });
        }).exceptionally(e -> {
            Platform.runLater(() -> {
                dibujarSeccionResenas(new ArrayList<>(), true);
            });
            return null;
        });
    }

    private void dibujarSeccionResenas(List<ReviewDto> reviews, boolean isServerError) {
        reviewsSection.getChildren().clear();
        
        if (SessionManager.getInstance().isAuthenticated()) {
            reviewsSection.getChildren().add(buildReviewForm());
        }
        
        if (isServerError) {
            Label lbl = new Label("Las reseñas no se pudieron cargar debido a un error del servidor (Error 500).");
            lbl.setTextFill(Color.web(AppConstants.COLOR_ACCENT));
            reviewsSection.getChildren().add(lbl);
            return;
        }
        
        if (reviews == null) {
            mostrarVacio(reviewsSection, "No hay reseñas aún. ¡Sé el primero!");
            return;
        }
        if (reviews.isEmpty()) {
            mostrarVacio(reviewsSection, "No hay reseñas aún. ¡Sé el primero!");
            return;
        }
        
        for (ReviewDto r : reviews) {
            reviewsSection.getChildren().add(buildReviewCard(r));
        }
    }

    private VBox buildReviewForm() {
        VBox form = new VBox(10);
        form.setStyle("-fx-background-color: #1e1e1e; -fx-padding: 15; -fx-background-radius: 8;");
        
        Label errLbl = new Label(); 
        errLbl.setTextFill(Color.web(AppConstants.COLOR_ACCENT)); 
        errLbl.setVisible(false); 
        errLbl.setManaged(false);
        
        TextField titleIn = new TextField(); 
        titleIn.setPromptText("Título de la reseña");
        titleIn.setStyle("-fx-control-inner-background: #2a2a2a; -fx-text-inner-color: white;");
        
        ComboBox<Integer> rateIn = new ComboBox<>(); 
        rateIn.getItems().addAll(1, 2, 3, 4, 5); 
        rateIn.setPromptText("Calificación");
        
        TextArea contIn = new TextArea(); 
        contIn.setPromptText("Tu reseña..."); 
        contIn.setPrefRowCount(3);
        contIn.setStyle("-fx-control-inner-background: #2a2a2a; -fx-text-inner-color: white;");
        
        Button btn = new Button("Publicar"); 
        btn.setStyle("-fx-background-color: #e50914; -fx-text-fill: white; -fx-cursor: hand;");
        
        btn.setOnAction(e -> {
            enviarResena(rateIn, titleIn, contIn, errLbl);
        });
        
        form.getChildren().addAll(new Label("Escribe una reseña:"), errLbl, titleIn, rateIn, contIn, btn);
        return form;
    }

    private void enviarResena(ComboBox<Integer> rateIn, TextField titleIn, TextArea contIn, Label errLbl) {
        if (rateIn.getValue() == null) {
            return;
        }
        if (titleIn.getText().isBlank()) {
            return;
        }
        if (contIn.getText().isBlank()) {
            return;
        }
        
        errLbl.setVisible(false); 
        errLbl.setManaged(false);
        
        ReviewRequest req = new ReviewRequest(currentTvmazeId, rateIn.getValue(), titleIn.getText().trim(), contIn.getText().trim());
        reviewService.createReview(req).thenRun(() -> {
            Platform.runLater(() -> {
                cargarResenas();
            });
        }).exceptionally(err -> {
            Platform.runLater(() -> {
                if (err.toString().contains("409")) {
                    mostrarAlertaError(AppConstants.MESSAGE_ERROR_DUPLICATE_REVIEW);
                } else {
                    mostrarAlertaError(AppConstants.MESSAGE_ERROR_REVIEW_ACTION);
                }
            });
            return null;
        });
    }

    private void mostrarAlertaError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private VBox buildReviewCard(ReviewDto review) {
        String rId = review.getSafeId();
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: #151515; -fx-padding: 15; -fx-background-radius: 8; -fx-border-color: #333;");

        Label author = new Label("@Cargando..."); 
        author.setTextFill(Color.web(AppConstants.COLOR_ACCENT));
        resolverNombreAutor(review.getOwnerId(), author);
        
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

        HBox actions = new HBox(15); 
        actions.setAlignment(Pos.CENTER_LEFT);
        
        Button likeBtn = new Button("Me gusta (" + review.getLikesCount() + ")");
        likeBtn.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 4;");
        likeBtn.setDisable(true);
        configurarBotonLikeResena(rId, likeBtn, review.getLikesCount());

        VBox commContainer = new VBox(10); 
        commContainer.setManaged(false); 
        commContainer.setVisible(false);
        
        Button commBtn = new Button("Comentarios (" + review.getCommentsCount() + ")");
        commBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #aaaaaa; -fx-cursor: hand; -fx-underline: true;");
        commBtn.setOnAction(e -> {
            boolean show = !commContainer.isVisible();
            commContainer.setVisible(show); 
            commContainer.setManaged(show);
            if (show) {
                if (!rId.isEmpty()) {
                    cargarComentariosUI(rId, commContainer);
                }
            }
        });

        actions.getChildren().addAll(likeBtn, commBtn);
        injectOwnerActions(rId, review.getOwnerId(), actions);
        card.getChildren().addAll(author, title, content, actions, commContainer);
        return card;
    }

    private void cargarComentariosUI(String rId, VBox container) {
        container.getChildren().clear();
        container.setStyle("-fx-padding: 0 0 0 20; -fx-border-color: #333; -fx-border-width: 0 0 0 2;");
        
        if (SessionManager.getInstance().isAuthenticated()) {
            TextField in = new TextField(); 
            in.setPromptText("Escribe un comentario...");
            in.setStyle("-fx-control-inner-background: #2a2a2a; -fx-text-inner-color: white;");
            Button b = new Button("Enviar"); 
            b.setStyle("-fx-background-color: #555; -fx-text-fill: white; -fx-cursor: hand;");
            b.setOnAction(e -> {
                if (in.getText().isBlank()) {
                    return;
                }
                reviewService.createComment(rId, new CommentRequest(in.getText().trim()))
                        .thenRun(() -> {
                            Platform.runLater(() -> {
                                cargarComentariosUI(rId, container);
                            });
                        });
            });
            container.getChildren().add(new HBox(10, in, b));
        }
        
        reviewService.getReviewComments(rId).thenAccept(list -> {
            Platform.runLater(() -> {
                if (list != null) {
                    for (CommentDto c : list) {
                        container.getChildren().add(buildCommentItem(c, rId, container));
                    }
                }
            });
        });
    }

    private VBox buildCommentItem(CommentDto c, String rId, VBox parent) {
        String cId = c.getSafeId();
        VBox box = new VBox(5); 
        box.setStyle("-fx-background-color: #1a1a1a; -fx-padding: 8; -fx-background-radius: 5;");
        
        Label user = new Label("@Cargando..."); 
        user.setTextFill(Color.GRAY);
        resolverNombreAutor(c.getOwnerId(), user);
        
        String tText = "";
        if (c.content() != null) {
            tText = c.content();
        }
        Label txt = new Label(tText); 
        txt.setTextFill(Color.WHITE);
        
        HBox actions = new HBox(10);
        Button lk = new Button("Me gusta (" + c.getLikesCount() + ")");
        lk.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 4;");
        lk.setDisable(true);
        configurarBotonLikeComentario(cId, lk, c.getLikesCount(), rId, parent);
        
        actions.getChildren().add(lk);
        injectCommentDelete(cId, c.getOwnerId(), actions, rId, parent);
        
        box.getChildren().addAll(user, txt, actions);
        return box;
    }

    private void configurarBotonLikeResena(String rId, Button likeBtn, int initialCount) {
        if (!SessionManager.getInstance().isAuthenticated()) {
            likeBtn.setDisable(false); 
            return;
        }
        if (rId.isEmpty()) {
            likeBtn.setDisable(false); 
            return;
        }
        
        reviewService.isReviewLikedByMe(rId).thenAccept(isLiked -> {
            Platform.runLater(() -> {
                likeBtn.setDisable(false);
                actualizarAparienciaLike(likeBtn, isLiked, initialCount);
                likeBtn.setOnAction(e -> {
                    handleLikeToggleResena(rId, likeBtn);
                });
            });
        });
    }

    private void handleLikeToggleResena(String rId, Button likeBtn) {
        boolean currState = likeBtn.getText().startsWith("Quitar");
        int count = extraerConteoLike(likeBtn.getText());
        int newCount = count + 1;
        if (currState) {
            newCount = Math.max(0, count - 1);
        }
        
        actualizarAparienciaLike(likeBtn, !currState, newCount);
        reviewService.toggleReviewLike(rId, currState).exceptionally(err -> {
            Platform.runLater(() -> {
                actualizarAparienciaLike(likeBtn, currState, count);
            });
            return null;
        });
    }

    private void configurarBotonLikeComentario(String cId, Button lk, int initialCount, String rId, VBox parent) {
        if (!SessionManager.getInstance().isAuthenticated()) {
            lk.setDisable(false); 
            return;
        }
        if (cId.isEmpty()) {
            lk.setDisable(false); 
            return;
        }
        
        reviewService.isCommentLikedByMe(cId).thenAccept(isLiked -> {
            Platform.runLater(() -> {
                lk.setDisable(false);
                actualizarAparienciaLike(lk, isLiked, initialCount);
                lk.setOnAction(e -> {
                    handleLikeToggleComentario(cId, lk, rId, parent);
                });
            });
        });
    }

    private void handleLikeToggleComentario(String cId, Button lk, String rId, VBox parent) {
        boolean currState = lk.getText().startsWith("Quitar");
        int count = extraerConteoLike(lk.getText());
        int newCount = count + 1;
        if (currState) {
            newCount = Math.max(0, count - 1);
        }

        actualizarAparienciaLike(lk, !currState, newCount);
        reviewService.toggleCommentLike(cId, currState).exceptionally(err -> {
            Platform.runLater(() -> {
                actualizarAparienciaLike(lk, currState, count);
            });
            return null;
        });
    }

    private void actualizarAparienciaLike(Button btn, boolean isLiked, int count) {
        if (isLiked) {
            btn.setText("Quitar Like (" + count + ")");
            btn.setStyle("-fx-background-color: #e50914; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 4;");
        } else {
            btn.setText("Me gusta (" + count + ")");
            btn.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 4;");
        }
    }

    private int extraerConteoLike(String text) {
        try { 
            return Integer.parseInt(text.replaceAll("[^0-9]", "")); 
        } catch (Exception e) { 
            return 0; 
        }
    }

    private void resolverNombreAutor(String ownerId, Label label) {
        if (ownerId == null) { 
            label.setText("@Usuario"); 
            return; 
        }
        if (ownerId.isEmpty()) {
            label.setText("@Usuario"); 
            return; 
        }
        
        if (SessionManager.getInstance().isAuthenticated()) {
            String currAuth = SessionManager.getInstance().getCurrentUser().authId();
                    if (ownerId.equals(currAuth)) {
                        label.setText("@" + SessionManager.getInstance().getCurrentUser().username()); 
                        return;
                    }
            if (ownerId.equals(SessionManager.getInstance().getCurrentUser().id())) {
                        label.setText("@" + SessionManager.getInstance().getCurrentUser().username()); 
                        return;
                    }
                }
        
        if (usernameCache.containsKey(ownerId)) {
            label.setText("@" + usernameCache.get(ownerId)); 
            return;
        }
        
        userService.getUserById(ownerId).thenAccept(user -> {
            if (user != null) {
                if (user.username() != null) {
                    usernameCache.put(ownerId, user.username());
                    Platform.runLater(() -> {
                        label.setText("@" + user.username());
                    });
                    return;
                }
            }
            Platform.runLater(() -> {
                label.setText("@Usuario");
            });
        });
    }

    private void injectOwnerActions(String rId, String ownerId, HBox actions) {
        if (!SessionManager.getInstance().isAuthenticated()) {
            return;
        }
        if (ownerId == null) {
            return;
        }
        if (ownerId.isEmpty()) {
            return;
        }
        
        String currAuth = SessionManager.getInstance().getCurrentUser().authId();
        boolean isOwner = false;
            if (ownerId.equals(currAuth)) {
                isOwner = true;
            }
        if (ownerId.equals(SessionManager.getInstance().getCurrentUser().id())) {
                isOwner = true;
            }
        
        if (isOwner) {
            Button del = new Button("Eliminar");
            del.setStyle("-fx-background-color: transparent; -fx-text-fill: #e50914; -fx-cursor: hand; -fx-underline: true;");
            del.setOnAction(e -> {
                reviewService.deleteReview(rId).thenRun(() -> {
                    Platform.runLater(() -> {
                        cargarResenas();
                    });
                });
            });
            actions.getChildren().add(del);
        }
    }

    private void injectCommentDelete(String cId, String ownerId, HBox actions, String rId, VBox parent) {
        if (!SessionManager.getInstance().isAuthenticated()) {
            return;
        }
        if (ownerId == null) {
            return;
        }
        if (ownerId.isEmpty()) {
            return;
        }
        
        String currAuth = SessionManager.getInstance().getCurrentUser().authId();
        boolean isOwner = false;
        
        if (ownerId.equals(currAuth)) {
            isOwner = true;
        }
        if (ownerId.equals(SessionManager.getInstance().getCurrentUser().id())) {
            isOwner = true;
        }
        
        if (isOwner) {
            Button del = new Button("Eliminar");
            del.setStyle("-fx-background-color: transparent; -fx-text-fill: #e50914; -fx-cursor: hand; -fx-underline: true;");
            del.setOnAction(e -> {
                reviewService.deleteComment(cId).thenRun(() -> {
                    Platform.runLater(() -> {
                        cargarComentariosUI(rId, parent);
                    });
                });
            });
            actions.getChildren().add(del);
        }
    }

    private void injectShowCard(Show s, HBox container) {
        try {
            FXMLLoader l = new FXMLLoader(getClass().getResource(AppConstants.FXML_SHOW_CARD));
            VBox card = l.load();
            ((ShowCardController)l.getController()).setData(s);
            container.getChildren().add(card);
        } catch (IOException e) { 
            System.err.println("Error card"); 
        }
    }

    private VBox buildPersonBox(CastDto m) {
        VBox b = new VBox(5); 
        b.setAlignment(Pos.TOP_CENTER); 
        b.setPrefWidth(120); 
        b.setMaxWidth(120);
        
        ImageView iv = new ImageView(); 
        iv.setFitHeight(150); 
        iv.setFitWidth(110);
        
        if (m.person() != null) {
            if (m.person().image() != null) {
                iv.setImage(new Image(m.person().image().medium(), true));
            }
        }
        
        String actorName = "Desconocido";
        if (m.person() != null) {
            if (m.person().name() != null) {
                actorName = m.person().name();
            }
        }
        Label n = new Label(actorName); 
        n.setTextFill(Color.WHITE); 
        n.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        n.setWrapText(true); 
        n.setTextAlignment(TextAlignment.CENTER);
        
        String charName = "";
        if (m.character() != null) {
            if (m.character().name() != null) {
                charName = m.character().name();
            }
        }
        Label c = new Label(charName);
        c.setTextFill(Color.GRAY); 
        c.setStyle("-fx-font-size: 12px;");
        c.setWrapText(true); 
        c.setTextAlignment(TextAlignment.CENTER);
        
        b.getChildren().addAll(iv, n, c); 
        return b;
    }

    private void moverCarruselDinamico(ScrollPane sp, int dir) {
        sp.setHvalue(Math.max(0, Math.min(sp.getHvalue() + (dir * 0.2), 1)));
    }
}