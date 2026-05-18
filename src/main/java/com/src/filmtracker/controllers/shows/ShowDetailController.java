package com.src.filmtracker.controllers.shows;

import com.src.filmtracker.App;
import com.src.filmtracker.models.common.PaginationDto;
import com.src.filmtracker.models.library.LibraryItemDto;
import com.src.filmtracker.models.reviews.CommentDto;
import com.src.filmtracker.models.reviews.CommentPaginationResponse;
import com.src.filmtracker.models.reviews.CommentRequest;
import com.src.filmtracker.models.reviews.ReviewDto;
import com.src.filmtracker.models.reviews.ReviewPaginationResponse;
import com.src.filmtracker.models.reviews.ReviewRequest;
import com.src.filmtracker.models.shows.CastDto;
import com.src.filmtracker.models.shows.EpisodeDto;
import com.src.filmtracker.models.shows.SeasonDto;
import com.src.filmtracker.models.shows.Show;
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
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
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
    private final IAdminService adminService = new AdminService();
    
    private final Map<Integer, List<EpisodeDto>> seasonEpisodesMap = new ConcurrentHashMap<>();
    private final Map<String, UserDto> userCache = new ConcurrentHashMap<>();
    
    private Integer currentTvmazeId;
    private int currentReviewPage = 1;
    
    private File selectedReviewImage;
    private final Map<String, File> selectedCommentImages = new ConcurrentHashMap<>();
    private final Map<String, File> editReviewImages = new ConcurrentHashMap<>();
    private Image fallbackErrorImage;

    public ShowDetailController() {
    }

    @FXML
    public void initialize() {
        cargarImagenErrorEnCache();
    }

    @FXML 
    private void handleClose() {
        Platform.exit(); 
        System.exit(0); 
    }
    
    @FXML 
    private void handleMinimize() {
        Stage stage = (Stage) titleLabel.getScene().getWindow();
        stage.setIconified(true); 
    }
    
    @FXML 
    private void handleBack() {
        App.goBackUniversal(); 
    }
    
    @FXML 
    private void handleHome() {
        App.setRoot(AppConstants.FXML_DASHBOARD); 
    }
    
    @FXML 
    private void scrollIzqCast() {
        moverCarruselDinamico(scrollCast, -1); 
    }
    
    @FXML 
    private void scrollDerCast() {
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
                procesarDatosDetallados(fullData);
            });
        }).exceptionally(e -> {
            return null;
        });

        apiService.getShowEpisodes(currentTvmazeId).thenAccept(episodes -> {
            if (episodes != null) {
                for (EpisodeDto ep : episodes) {
                    if (ep.season() != null) {
                        seasonEpisodesMap.computeIfAbsent(ep.season(), k -> new ArrayList<>()).add(ep);
                    }
                }
            }
        });

        this.currentReviewPage = 1;
        cargarResenas(this.currentReviewPage);
    }

    private void procesarDatosDetallados(com.src.filmtracker.models.shows.ShowFullResponse fullData) {
        if (fullData != null) {
            if (fullData.show() != null) {
                cargarDatosBasicosUI(fullData.show());
                
                if (fullData.show().genres() != null) {
                    cargarSeriesSimilaresMultigenero(fullData.show().genres());
                }
            }
            
            if (fullData.cast() != null) {
                actualizarCastUI(fullData.cast());
            }
            
            if (fullData.seasons() != null) {
                actualizarTemporadasUI(fullData.seasons());
            }
        }
    }

    private void cargarDatosBasicosUI(Show show) {
        if (show == null) {
            return; 
        }
        
        String nombreSeguro = "Desconocido";
        
        if (show.name() != null) {
            nombreSeguro = show.name();
        }
        
        titleLabel.setText(nombreSeguro);
        
        String statusSeguro = "N/A";
        
        if (show.status() != null) {
            statusSeguro = show.status();
        }
        
        statusLabel.setText("Status: " + statusSeguro);
        
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
                cargarImagenConRespaldo(show.image().original(), posterDetail);
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
        
        libBox.getChildren().add(favBtn);
        libBox.getChildren().add(watchBtn);
        
        VBox infoPadre = (VBox) titleLabel.getParent();
        infoPadre.getChildren().add(libBox);
        
        configurarBtnFavoritos(favBtn);
        configurarBtnWatchlist(watchBtn);
    }

    private void configurarBtnFavoritos(Button btn) {
        buscarEnFavoritosRecursivo(1, btn);
    }

    private void buscarEnFavoritosRecursivo(int page, Button btn) {
        libraryService.getFavoritesPaged(page).thenAccept(list -> {
            if (list == null) {
                finalizarConfiguracionFav(btn, false);
                return;
            }
            
            if (list.isEmpty()) {
                finalizarConfiguracionFav(btn, false);
                return;
            }
            
            boolean encontrado = evaluarListaParaId(list, currentTvmazeId);
            
            if (encontrado) {
                finalizarConfiguracionFav(btn, true);
                return;
            }
            
            if (list.size() == 10) {
                buscarEnFavoritosRecursivo(page + 1, btn);
                return;
            }
            
            finalizarConfiguracionFav(btn, false);
        }).exceptionally(err -> {
            finalizarConfiguracionFav(btn, false);
            return null;
        });
    }

    private void finalizarConfiguracionFav(Button btn, boolean isFav) {
        Platform.runLater(() -> {
            actualizarAparienciaFav(btn, isFav);
            
            btn.setOnAction(e -> {
                handleToggleFav(btn);
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
            btn.setText("Añadir a Favoritos (❤️)");
            btn.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 4;");
        }
    }

    private void configurarBtnWatchlist(Button btn) {
        buscarEnWatchlistRecursivo(1, btn);
    }

    private void buscarEnWatchlistRecursivo(int page, Button btn) {
        libraryService.getWatchlistPaged(page).thenAccept(list -> {
            if (list == null) {
                finalizarConfiguracionWatch(btn, false);
                return;
            }
            
            if (list.isEmpty()) {
                finalizarConfiguracionWatch(btn, false);
                return;
            }
            
            boolean encontrado = evaluarListaParaId(list, currentTvmazeId);
            
            if (encontrado) {
                finalizarConfiguracionWatch(btn, true);
                return;
            }
            
            if (list.size() == 10) {
                buscarEnWatchlistRecursivo(page + 1, btn);
                return;
            }
            
            finalizarConfiguracionWatch(btn, false);
        }).exceptionally(err -> {
            finalizarConfiguracionWatch(btn, false);
            return null;
        });
    }

    private void finalizarConfiguracionWatch(Button btn, boolean isWatch) {
        Platform.runLater(() -> {
            actualizarAparienciaWatch(btn, isWatch);
            
            btn.setOnAction(e -> {
                handleToggleWatch(btn);
            });
        });
    }
    
    private boolean evaluarListaParaId(List<LibraryItemDto> list, Integer targetId) {
        for (LibraryItemDto item : list) {
            if (item.tvmazeId() != null) {
                if (item.tvmazeId().equals(targetId)) {
                    return true;
                }
            }
        }
        
        return false;
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
            btn.setText("Quitar de Watchlist (➖)");
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
                if (s.number() != null) {
                    episodesContainer.getChildren().add(createSeasonAccordion(s));
                }
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
            try {
                List<Show> list = f.join();
                
                if (list != null) {
                    for (Show s : list) {
                        procesarShowSimilar(s, seen, result);
                    }
                }
            } catch (Exception e) { 
            }
        }
        
        return result;
    }

    private void procesarShowSimilar(Show s, Set<Integer> seen, List<Show> result) {
        if (s.tvmazeId() != null) {
            if (!s.tvmazeId().equals(currentTvmazeId)) {
                if (seen.add(s.tvmazeId())) {
                    result.add(s);
                }
            }
        }
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
        
        c.getChildren().add(h);
        c.getChildren().add(eps);
        
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
        
        boolean tieneImagen = false;
        
        if (ep.image() != null) {
            if (ep.image().medium() != null) {
                if (!ep.image().medium().isEmpty()) {
                    cargarImagenConRespaldo(ep.image().medium(), iv);
                    tieneImagen = true;
                }
            }
        }
        
        if (!tieneImagen) {
            ponerImagenError(iv);
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

        card.getChildren().add(iv);
        card.getChildren().add(title);
        card.getChildren().add(info);
        card.getChildren().add(summary);
        
        return card;
    }

    private void cargarResenas(int page) {
        reviewService.getShowReviews(currentTvmazeId, page).thenAccept(res -> {
            Platform.runLater(() -> {
                dibujarSeccionResenas(res, page, false);
            });
        }).exceptionally(e -> {
            Platform.runLater(() -> {
                dibujarSeccionResenas(null, page, true);
            });
            return null;
        });
    }

    private void dibujarSeccionResenas(ReviewPaginationResponse res, int page, boolean isServerError) {
        if (page == 1) {
            reviewsSection.getChildren().clear();
            if (SessionManager.getInstance().isAuthenticated()) {
                reviewsSection.getChildren().add(buildReviewForm());
            }
        } else {
            removerBotonCargarMasResenas();
        }
        
        if (isServerError) {
            Label lbl = new Label("Las reseñas no se pudieron cargar.");
            lbl.setTextFill(Color.web(AppConstants.COLOR_ACCENT));
            reviewsSection.getChildren().add(lbl);
            return;
        }
        
        if (res == null) {
            if (page == 1) {
                mostrarVacio(reviewsSection, "No hay reseñas aún. ¡Sé el primero!");
            }
            return;
        }
        
        if (res.reviews() == null) {
            if (page == 1) {
                mostrarVacio(reviewsSection, "No hay reseñas aún. ¡Sé el primero!");
            }
            return;
        }
        
        if (res.reviews().isEmpty()) {
            if (page == 1) {
                mostrarVacio(reviewsSection, "No hay reseñas aún. ¡Sé el primero!");
            }
            return;
        }
        
        for (ReviewDto r : res.reviews()) {
            reviewsSection.getChildren().add(buildReviewCard(r));
        }
        
        evaluarBotonCargarMasResenas(res.pagination());
    }

    private void removerBotonCargarMasResenas() {
        if (!reviewsSection.getChildren().isEmpty()) {
            int lastIndex = reviewsSection.getChildren().size() - 1;
            
            if (reviewsSection.getChildren().get(lastIndex) instanceof HBox) {
                reviewsSection.getChildren().remove(lastIndex);
            }
        }
    }

    private void evaluarBotonCargarMasResenas(PaginationDto pag) {
        if (pag != null) {
            if (pag.hasNextPage() != null) {
                if (pag.hasNextPage()) {
                    Button btnMore = new Button("Cargar más reseñas");
                    btnMore.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 4; -fx-padding: 8 15;");
                    
                    btnMore.setOnAction(e -> {
                        currentReviewPage++;
                        cargarResenas(currentReviewPage);
                    });
                    
                    HBox centerBox = new HBox(btnMore);
                    centerBox.setAlignment(Pos.CENTER);
                    centerBox.setPadding(new Insets(10, 0, 10, 0));
                    
                    reviewsSection.getChildren().add(centerBox);
                }
            }
        }
    }

    private File openImageChooser() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Seleccionar Imagen");
        
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg");
        fc.getExtensionFilters().add(filter);
        
        return fc.showOpenDialog(titleLabel.getScene().getWindow());
    }

    private void inyectarImagenSiExiste(String url, VBox card) {
        if (url == null) {
            return;
        }
        
        if (url.isEmpty()) {
            return;
        }
        
        ImageView iv = new ImageView();
        iv.setFitWidth(300);
        iv.setPreserveRatio(true);
        iv.setStyle("-fx-background-radius: 5;");
        
        cargarImagenConRespaldo(url, iv);
        
        card.getChildren().add(iv);
    }

    private VBox buildReviewForm() {
        VBox form = new VBox(10);
        form.setStyle("-fx-background-color: #1e1e1e; -fx-padding: 15; -fx-background-radius: 8;");
        
        Label errLbl = new Label(); 
        errLbl.setTextFill(Color.web(AppConstants.COLOR_ACCENT)); 
        errLbl.setVisible(false); 
        errLbl.setManaged(false);
        
        TextField titleIn = new TextField(); 
        titleIn.setPromptText("Título");
        titleIn.setStyle("-fx-control-inner-background: #2a2a2a; -fx-text-inner-color: white;");
        
        ComboBox<Integer> rateIn = new ComboBox<>(); 
        rateIn.getItems().add(1);
        rateIn.getItems().add(2);
        rateIn.getItems().add(3);
        rateIn.getItems().add(4);
        rateIn.getItems().add(5);
        rateIn.setPromptText("Calificación");
        
        TextArea contIn = new TextArea(); 
        contIn.setPromptText("Tu reseña..."); 
        contIn.setPrefRowCount(3);
        contIn.setStyle("-fx-control-inner-background: #2a2a2a; -fx-text-inner-color: white;");
        
        Label fileLabel = new Label("Sin imagen");
        fileLabel.setTextFill(Color.GRAY);
        
        Button attachBtn = new Button("Adjuntar Imagen");
        attachBtn.setStyle("-fx-background-color: #333; -fx-text-fill: white; -fx-cursor: hand;");
        
        attachBtn.setOnAction(e -> {
            File file = openImageChooser();
            if (file != null) {
                selectedReviewImage = file;
                fileLabel.setText(file.getName());
            }
        });

        HBox fileBox = new HBox(10);
        fileBox.setAlignment(Pos.CENTER_LEFT);
        fileBox.getChildren().add(attachBtn);
        fileBox.getChildren().add(fileLabel);

        Button btn = new Button("Publicar"); 
        btn.setStyle("-fx-background-color: #e50914; -fx-text-fill: white; -fx-cursor: hand;");
        
        btn.setOnAction(e -> {
            enviarResena(rateIn, titleIn, contIn, errLbl);
        });
        
        form.getChildren().add(new Label("Escribe una reseña:"));
        form.getChildren().add(errLbl);
        form.getChildren().add(titleIn);
        form.getChildren().add(rateIn);
        form.getChildren().add(contIn);
        form.getChildren().add(fileBox);
        form.getChildren().add(btn);
        
        return form;
    }

    private void enviarResena(ComboBox<Integer> rateIn, TextField titleIn, TextArea contIn, Label errLbl) {
        UserDto currentUser = SessionManager.getInstance().getCurrentUser();
        
        if (currentUser != null) {
            if (!currentUser.isVerified()) {
                mostrarAlertaPrecaucion(AppConstants.MESSAGE_ERROR_UNVERIFIED);
                return;
            }
        }
        
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
        
        reviewService.createReview(req).thenAccept(review -> {
            if (selectedReviewImage != null) {
                if (review != null) {
                    reviewService.uploadReviewImage(review.getSafeId(), selectedReviewImage).thenRun(() -> {
                        limpiarYRefrescarResenas();
                    });
                    return;
                }
            }
            
            limpiarYRefrescarResenas();
            
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
    
    private void injectReviewActions(ReviewDto review, HBox actions, VBox card) {
        if (!SessionManager.getInstance().isAuthenticated()) {
            return;
        }
        
        String ownerId = review.getOwnerId();
        
        if (ownerId == null) {
            return;
        }
        
        if (ownerId.isEmpty()) {
            return;
        }
        
        UserDto currUser = SessionManager.getInstance().getCurrentUser();
        String currAuth = currUser.getSafeAuthId();
        boolean isAdmin = "ADMIN".equals(currUser.role());
        
        if (ownerId.equals(currAuth)) {
            agregarBotonEliminarResena(review.getSafeId(), actions);
            
            if (isWithinEditWindow(review.created_at())) {
                agregarBotonEditarResena(review, actions, card);
            }
            
            return; 
        }
        
        if (isAdmin) {
            agregarBotonesAdminResena(review, actions);
        }
        
        agregarBotonReportar(review.getSafeId(), "REVIEW", actions);
    }

    private void injectCommentActions(CommentDto c, HBox actions, String rId, VBox parent) {
        if (!SessionManager.getInstance().isAuthenticated()) {
            return;
        }
        
        String ownerId = c.getOwnerId();
        
        if (ownerId == null) {
            return;
        }
        
        if (ownerId.isEmpty()) {
            return;
        }
        
        UserDto currUser = SessionManager.getInstance().getCurrentUser();
        String currAuth = currUser.getSafeAuthId();
        boolean isAdmin = "ADMIN".equals(currUser.role());
        String cId = c.getSafeId();
        
        if (ownerId.equals(currAuth)) {
            agregarBotonEliminarComentario(cId, rId, parent, actions);
            return;
        }
        
        if (isAdmin) {
            agregarBotonesAdminComentario(c, rId, parent, actions);
        }
        
        agregarBotonReportar(cId, "COMMENT", actions);
    }

    private void agregarBotonReportar(String targetId, String targetType, HBox actions) {
        Button rep = new Button("Reportar");
        rep.setStyle("-fx-background-color: transparent; -fx-text-fill: #e50914; -fx-cursor: hand; -fx-underline: true;");
        
        rep.setOnAction(e -> {
            com.src.filmtracker.utils.ReportModalHelper.openReportModal(targetType, targetId);
        });
        
        actions.getChildren().add(rep);
    }

    private void limpiarYRefrescarResenas() {
        Platform.runLater(() -> {
            this.selectedReviewImage = null;
            this.currentReviewPage = 1;
            cargarResenas(1);
        });
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
    
    private void mostrarAlertaPrecaucion(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Atención");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        
        ButtonType btnVerify = new ButtonType("Verificar ahora");
        ButtonType btnCancel = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        
        alert.getButtonTypes().clear();
        alert.getButtonTypes().add(btnVerify);
        alert.getButtonTypes().add(btnCancel);
        
        Optional<ButtonType> result = alert.showAndWait();
        
        if (result.isPresent()) {
            if (result.get() == btnVerify) {
                App.setRoot(AppConstants.FXML_VERIFY_EMAIL);
            }
        }
    }

    private String parsearIsoFormato(String isoString) {
        if (isoString == null) {
            return "";
        }
        
        if (isoString.isEmpty()) {
            return "";
        }
        
        try {
            ZonedDateTime zdt = ZonedDateTime.parse(isoString);
            return zdt.format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"));
        } catch (Exception e) {
            return isoString;
        }
    }

    private String formatearFechaVisual(String createdAt, String updatedAt) {
        String created = parsearIsoFormato(createdAt);
        String updated = parsearIsoFormato(updatedAt);
        
        if (created.isEmpty()) {
            return "";
        }
        
        if (!created.equals(updated)) {
            if (!updated.isEmpty()) {
                return created + " (editado " + updated + ")";
            }
        }
        
        return created;
    }

    private HBox buildHeaderWithDate(Label authorLabel, String createdAt, String updatedAt) {
        Label dateLabel = new Label(formatearFechaVisual(createdAt, updatedAt));
        dateLabel.setTextFill(Color.web("#888888"));
        dateLabel.setStyle("-fx-font-size: 11px;");
        
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        header.getChildren().add(authorLabel);
        header.getChildren().add(dateLabel);
        
        return header;
    }

    private VBox buildReviewCard(ReviewDto review) {
        String rId = review.getSafeId();
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: #151515; -fx-padding: 15; -fx-background-radius: 8; -fx-border-color: #333;");

        Label author = new Label("@Cargando..."); 
        resolverNombreAutor(review.getOwnerId(), author);
        
        HBox header = buildHeaderWithDate(author, review.created_at(), review.updated_at());
        
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

        card.getChildren().add(header);
        card.getChildren().add(title);
        card.getChildren().add(content);
        
        inyectarImagenSiExiste(review.getImageUrl(), card);

        HBox actions = new HBox(15); 
        actions.setAlignment(Pos.CENTER_LEFT);
        
        Button likeBtn = new Button("Me gusta (" + review.getLikesCount() + ")");
        likeBtn.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 4;");
        configurarBotonLikeResena(rId, likeBtn, review.getLikesCount(), review.getIsLikedValue());

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
                    cargarComentariosUI(rId, commContainer, 1);
                }
            }
        });

        actions.getChildren().add(likeBtn);
        actions.getChildren().add(commBtn);
        
        injectReviewActions(review, actions, card);
        
        card.getChildren().add(actions);
        card.getChildren().add(commContainer);
        
        return card;
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
        
        if (userCache.containsKey(ownerId)) {
            UserDto cachedUser = userCache.get(ownerId);
            label.setText("@" + cachedUser.username());
            configurarClicAutor(label, cachedUser);
            return;
        }
        
        userService.getUserById(ownerId).thenAccept(user -> {
            Platform.runLater(() -> {
                procesarUsuarioObtenido(user, ownerId, label);
            });
        });
    }

    private void procesarUsuarioObtenido(UserDto user, String ownerId, Label label) {
        if (user != null) {
            if (user.username() != null) {
                userCache.put(ownerId, user);
                label.setText("@" + user.username());
                configurarClicAutor(label, user);
                return;
            }
        }
        
        label.setText("@Usuario");
    }

    private void configurarClicAutor(Label label, UserDto user) {
        String baseStyle = label.getStyle();
        
        if (baseStyle == null) {
            baseStyle = "";
        }
        
        label.setStyle(baseStyle + " -fx-cursor: hand; -fx-underline: true; -fx-text-fill: " + AppConstants.COLOR_ACCENT + ";");
        
        label.setOnMouseClicked(e -> {
            App.showProfileView(user);
        });
    }

    private void cargarComentariosUI(String rId, VBox container, int page) {
        if (page == 1) {
            container.getChildren().clear();
            container.setStyle("-fx-padding: 0 0 0 20; -fx-border-color: #333; -fx-border-width: 0 0 0 2;");
            
            if (SessionManager.getInstance().isAuthenticated()) {
                agregarFormularioComentario(rId, container);
            }
        } else {
            removerBotonCargarMasComentarios(container);
        }
        
        reviewService.getReviewComments(rId, page).thenAccept(res -> {
            Platform.runLater(() -> {
                procesarComentariosObtenidos(res, rId, container, page);
            });
        });
    }

    private void procesarComentariosObtenidos(CommentPaginationResponse res, String rId, VBox container, int page) {
        if (res != null) {
            if (res.comments() != null) {
                for (CommentDto c : res.comments()) {
                    container.getChildren().add(buildCommentItem(c, rId, container));
                }
                
                evaluarBotonCargarMasComentarios(rId, container, res.pagination(), page);
            }
        }
    }

    private void removerBotonCargarMasComentarios(VBox container) {
        if (!container.getChildren().isEmpty()) {
            int lastIndex = container.getChildren().size() - 1;
            
            if (container.getChildren().get(lastIndex) instanceof HBox) {
                container.getChildren().remove(lastIndex);
            }
        }
    }

    private void evaluarBotonCargarMasComentarios(String rId, VBox container, PaginationDto pag, int currentPage) {
        if (pag != null) {
            if (pag.hasNextPage() != null) {
                if (pag.hasNextPage()) {
                    Button btnMore = new Button("Cargar más comentarios");
                    btnMore.setStyle("-fx-background-color: transparent; -fx-text-fill: #aaaaaa; -fx-cursor: hand; -fx-underline: true;");
                    
                    btnMore.setOnAction(e -> {
                        cargarComentariosUI(rId, container, currentPage + 1);
                    });
                    
                    HBox centerBox = new HBox(btnMore);
                    centerBox.setAlignment(Pos.CENTER_LEFT);
                    container.getChildren().add(centerBox);
                }
            }
        }
    }

    private void agregarFormularioComentario(String rId, VBox container) {
        TextField in = new TextField(); 
        in.setPromptText("Comentar...");
        in.setStyle("-fx-control-inner-background: #2a2a2a; -fx-text-inner-color: white;");
        
        Label cFileLabel = new Label("");
        cFileLabel.setTextFill(Color.GRAY);
        cFileLabel.setStyle("-fx-font-size: 10px;");

        Button imgBtn = new Button("📷");
        imgBtn.setStyle("-fx-background-color: #333; -fx-text-fill: white; -fx-cursor: hand;");
        
        imgBtn.setOnAction(e -> {
            File f = openImageChooser();
            if (f != null) {
                selectedCommentImages.put(rId, f);
                cFileLabel.setText(f.getName());
            }
        });

        Button b = new Button("Enviar"); 
        b.setStyle("-fx-background-color: #555; -fx-text-fill: white; -fx-cursor: hand;");
        
        b.setOnAction(e -> {
            procesarEnvioComentario(in, rId, container);
        });
        
        HBox box = new HBox(10);
        box.getChildren().add(in);
        box.getChildren().add(imgBtn);
        box.getChildren().add(b);
        
        container.getChildren().add(box);
        container.getChildren().add(cFileLabel);
    }
    
    private void procesarEnvioComentario(TextField in, String rId, VBox container) {
        UserDto currentUser = SessionManager.getInstance().getCurrentUser();
        
        if (currentUser != null) {
            if (!currentUser.isVerified()) {
                mostrarAlertaPrecaucion(AppConstants.MESSAGE_ERROR_UNVERIFIED);
                return;
            }
        }
        
        if (in.getText().isBlank()) {
            return;
        }
        
        CommentRequest req = new CommentRequest(in.getText().trim());
        File img = selectedCommentImages.get(rId);
        
        reviewService.createComment(rId, req, img).thenAccept(comment -> {
            refrescarComentarios(rId, container);
        }).exceptionally(err -> {
            Platform.runLater(() -> {
                mostrarAlertaError(AppConstants.MESSAGE_ERROR_REVIEW_ACTION);
            });
            return null;
        });
    }

    private void refrescarComentarios(String rId, VBox container) {
        Platform.runLater(() -> {
            selectedCommentImages.remove(rId);
            cargarComentariosUI(rId, container, 1);
        });
    }

    private VBox buildCommentItem(CommentDto c, String rId, VBox parent) {
        String cId = c.getSafeId();
        VBox box = new VBox(5); 
        box.setStyle("-fx-background-color: #1a1a1a; -fx-padding: 8; -fx-background-radius: 5;");
        
        Label user = new Label("@Cargando..."); 
        resolverNombreAutor(c.getOwnerId(), user);
        
        HBox header = buildHeaderWithDate(user, c.created_at(), c.updated_at());
        
        String txtContent = "";
        
        if (c.content() != null) {
            txtContent = c.content();
        }
        
        Label txt = new Label(txtContent); 
        txt.setTextFill(Color.WHITE);
        
        box.getChildren().add(header);
        box.getChildren().add(txt);
        
        inyectarImagenSiExiste(c.getImageUrl(), box);
        
        HBox actions = new HBox(10);
        Button lk = new Button("Me gusta (" + c.getLikesCount() + ")");
        lk.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 4;");
        configurarBotonLikeComentario(cId, lk, c.getLikesCount(), c.getIsLikedValue(), rId, parent);
        
        actions.getChildren().add(lk);
        injectCommentActions(c, actions, rId, parent);
        
        box.getChildren().add(actions);
        
        return box;
    }

    private void configurarBotonLikeResena(String rId, Button likeBtn, int initialCount, boolean isLiked) {
        if (!SessionManager.getInstance().isAuthenticated()) {
            likeBtn.setDisable(true); 
            return;
        }
        
        if (rId.isEmpty()) {
            likeBtn.setDisable(true); 
            return;
        }
        
        likeBtn.setDisable(false);
        actualizarAparienciaLike(likeBtn, isLiked, initialCount);
        
        likeBtn.setOnAction(e -> {
            handleLikeToggleResena(rId, likeBtn);
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

    private void configurarBotonLikeComentario(String cId, Button lk, int initialCount, boolean isLiked, String rId, VBox parent) {
        if (!SessionManager.getInstance().isAuthenticated()) {
            lk.setDisable(true); 
            return;
        }
        
        if (cId.isEmpty()) {
            lk.setDisable(true); 
            return;
        }
        
        lk.setDisable(false);
        actualizarAparienciaLike(lk, isLiked, initialCount);
        
        lk.setOnAction(e -> {
            handleLikeToggleComentario(cId, lk, rId, parent);
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

    private boolean isWithinEditWindow(String createdAtStr) {
        if (createdAtStr == null) {
            return false;
        }
        
        if (createdAtStr.isEmpty()) {
            return false;
        }
        
        try {
            ZonedDateTime created = ZonedDateTime.parse(createdAtStr);
            ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
            long mins = ChronoUnit.MINUTES.between(created, now);
            
            if (mins <= 30) {
                return true;
            }
            
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private void agregarBotonEliminarResena(String rId, HBox actions) {
        Button del = new Button("Eliminar");
        del.setStyle("-fx-background-color: transparent; -fx-text-fill: #e50914; -fx-cursor: hand; -fx-underline: true;");
        
        del.setOnAction(e -> {
            if (confirmarAccion("Eliminar Reseña", "¿Estás seguro de que deseas eliminar tu reseña?")) {
                reviewService.deleteReview(rId).thenRun(() -> {
                    Platform.runLater(() -> {
                        this.currentReviewPage = 1;
                        cargarResenas(1);
                    });
                });
            }
        });
        
        actions.getChildren().add(del);
    }

    private void agregarBotonesAdminResena(ReviewDto review, HBox actions) {
        Button delAdmin = new Button("Eliminar");
        delAdmin.setStyle("-fx-background-color: transparent; -fx-text-fill: #ff9800; -fx-cursor: hand; -fx-underline: true;");

        delAdmin.setOnAction(e -> {
            if (confirmarAccion("Eliminar Reseña", "¿Deseas eliminar administrativamente esta reseña?")) {
                adminService.deleteReviewDirectly(review.getSafeId()).thenRun(() -> {
                    Platform.runLater(() -> {
                        mostrarAlertaExito("Reseña eliminada administrativamente.");
                        this.currentReviewPage = 1;
                        cargarResenas(1);
                    });
                }).exceptionally(err -> {
                    Platform.runLater(() -> mostrarAlertaError(AppConstants.MESSAGE_ERROR_API));
                    return null;
                });
            }
        });

        actions.getChildren().add(delAdmin);

        if (review.getImageUrl() != null) {
            if (!review.getImageUrl().isEmpty()) {
                Button delImgAdmin = new Button("Quitar Imagen");
                delImgAdmin.setStyle("-fx-background-color: transparent; -fx-text-fill: #ff9800; -fx-cursor: hand; -fx-underline: true;");

                delImgAdmin.setOnAction(e -> {
                    if (confirmarAccion("Quitar Imagen", "¿Deseas eliminar la imagen de esta reseña?")) {
                        adminService.removeReviewImageDirectly(review.getSafeId()).thenRun(() -> {
                            Platform.runLater(() -> {
                                mostrarAlertaExito("Imagen eliminada administrativamente.");
                                this.currentReviewPage = 1;
                                cargarResenas(1);
                            });
                        }).exceptionally(err -> {
                            Platform.runLater(() -> mostrarAlertaError(AppConstants.MESSAGE_ERROR_API));
                            return null;
                        });
                    }
                });

                actions.getChildren().add(delImgAdmin);
            }
        }
    }
    
    private void agregarBotonEliminarComentario(String cId, String rId, VBox parent, HBox actions) {
        Button del = new Button("Eliminar");
        del.setStyle("-fx-background-color: transparent; -fx-text-fill: #e50914; -fx-cursor: hand; -fx-underline: true;");
        
        del.setOnAction(e -> {
            if (confirmarAccion("Eliminar Comentario", "¿Estás seguro de que deseas eliminar tu comentario?")) {
                reviewService.deleteComment(cId).thenRun(() -> {
                    Platform.runLater(() -> {
                        cargarComentariosUI(rId, parent, 1);
                    });
                });
            }
        });
        
        actions.getChildren().add(del);
    }

    private void agregarBotonesAdminComentario(CommentDto c, String rId, VBox parent, HBox actions) {
        Button delAdmin = new Button("Eliminar");
        delAdmin.setStyle("-fx-background-color: transparent; -fx-text-fill: #ff9800; -fx-cursor: hand; -fx-underline: true;");

        delAdmin.setOnAction(e -> {
            if (confirmarAccion("Eliminar Comentario", "¿Deseas eliminar administrativamente este comentario?")) {
                adminService.deleteCommentDirectly(c.getSafeId()).thenRun(() -> {
                    Platform.runLater(() -> {
                        mostrarAlertaExito("Comentario eliminado administrativamente.");
                        cargarComentariosUI(rId, parent, 1);
                    });
                }).exceptionally(err -> {
                    Platform.runLater(() -> mostrarAlertaError(AppConstants.MESSAGE_ERROR_API));
                    return null;
                });
            }
        });

        actions.getChildren().add(delAdmin);

        if (c.getImageUrl() != null) {
            if (!c.getImageUrl().isEmpty()) {
                Button delImgAdmin = new Button("Quitar Imagen");
                delImgAdmin.setStyle("-fx-background-color: transparent; -fx-text-fill: #ff9800; -fx-cursor: hand; -fx-underline: true;");

                delImgAdmin.setOnAction(e -> {
                    if (confirmarAccion("Quitar Imagen", "¿Deseas eliminar la imagen de este comentario?")) {
                        adminService.removeCommentImageDirectly(c.getSafeId()).thenRun(() -> {
                            Platform.runLater(() -> {
                                mostrarAlertaExito("Imagen eliminada administrativamente.");
                                cargarComentariosUI(rId, parent, 1);
                            });
                        }).exceptionally(err -> {
                            Platform.runLater(() -> mostrarAlertaError(AppConstants.MESSAGE_ERROR_API));
                            return null;
                        });
                    }
                });

                actions.getChildren().add(delImgAdmin);
            }
        }
    }

    private void agregarBotonEditarResena(ReviewDto review, HBox actions, VBox card) {
        Button edit = new Button("Editar");
        edit.setStyle("-fx-background-color: transparent; -fx-text-fill: #4caf50; -fx-cursor: hand; -fx-underline: true;");
        
        edit.setOnAction(e -> {
            mostrarFormularioEdicion(review, card);
        });
        
        actions.getChildren().add(edit);
    }

    private void mostrarFormularioEdicion(ReviewDto review, VBox card) {
        card.getChildren().clear();
        
        Label errLbl = new Label();
        errLbl.setTextFill(Color.web(AppConstants.COLOR_ACCENT));
        errLbl.setVisible(false);
        errLbl.setManaged(false);
        
        TextField titleIn = new TextField(review.title() != null ? review.title() : "");
        titleIn.setStyle("-fx-control-inner-background: #2a2a2a; -fx-text-inner-color: white;");
        
        ComboBox<Integer> rateIn = new ComboBox<>();
        rateIn.getItems().add(1);
        rateIn.getItems().add(2);
        rateIn.getItems().add(3);
        rateIn.getItems().add(4);
        rateIn.getItems().add(5);
        rateIn.setValue(review.rating());
        
        TextArea contIn = new TextArea(review.content() != null ? review.content() : "");
        contIn.setPrefRowCount(3);
        contIn.setStyle("-fx-control-inner-background: #2a2a2a; -fx-text-inner-color: white;");
        
        Label fileLabel = new Label("Mantener imagen actual");
        fileLabel.setTextFill(Color.GRAY);
        
        Button attachBtn = new Button("Cambiar Imagen");
        attachBtn.setStyle("-fx-background-color: #333; -fx-text-fill: white; -fx-cursor: hand;");
        
        attachBtn.setOnAction(e -> {
            File file = openImageChooser();
            if (file != null) {
                editReviewImages.put(review.getSafeId(), file);
                fileLabel.setText(file.getName());
            }
        });
        
        HBox fileBox = new HBox(10);
        fileBox.setAlignment(Pos.CENTER_LEFT);
        fileBox.getChildren().add(attachBtn);
        fileBox.getChildren().add(fileLabel);
        
        Button saveBtn = new Button("Guardar");
        saveBtn.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; -fx-cursor: hand;");
        
        saveBtn.setOnAction(e -> {
            procesarEdicionResena(review.getSafeId(), rateIn, titleIn, contIn, errLbl);
        });
        
        Button cancelBtn = new Button("Cancelar");
        cancelBtn.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-cursor: hand;");
        
        cancelBtn.setOnAction(e -> {
            limpiarYRefrescarResenas();
        });
        
        HBox actionsBox = new HBox(10);
        actionsBox.getChildren().add(saveBtn);
        actionsBox.getChildren().add(cancelBtn);
        
        card.getChildren().add(new Label("Editar reseña:"));
        card.getChildren().add(errLbl);
        card.getChildren().add(titleIn);
        card.getChildren().add(rateIn);
        card.getChildren().add(contIn);
        card.getChildren().add(fileBox);
        card.getChildren().add(actionsBox);
    }

    private void procesarEdicionResena(String rId, ComboBox<Integer> rateIn, TextField titleIn, TextArea contIn, Label errLbl) {
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
        
        reviewService.updateReview(rId, req).thenAccept(review -> {
            File img = editReviewImages.get(rId);
            
            if (img != null) {
                if (review != null) {
                    reviewService.uploadReviewImage(review.getSafeId(), img).thenRun(() -> {
                        finalizarEdicionResena(rId);
                    });
                    return;
                }
            }
            
            finalizarEdicionResena(rId);
            
        }).exceptionally(err -> {
            Platform.runLater(() -> {
                mostrarAlertaError(AppConstants.MESSAGE_ERROR_REVIEW_ACTION);
            });
            return null;
        });
    }

    private void finalizarEdicionResena(String rId) {
        Platform.runLater(() -> {
            editReviewImages.remove(rId);
            mostrarAlertaExito(AppConstants.MESSAGE_SUCCESS_REVIEW_UPDATE);
            this.currentReviewPage = 1;
            cargarResenas(1);
        });
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

    private VBox buildPersonBox(CastDto m) {
        VBox b = new VBox(5); 
        b.setAlignment(Pos.TOP_CENTER); 
        b.setPrefWidth(120); 
        b.setMaxWidth(120);
        
        ImageView iv = new ImageView(); 
        iv.setFitHeight(150); 
        iv.setFitWidth(110);
        
        boolean tieneImagenPerson = false;
        
        if (m.person() != null) {
            if (m.person().image() != null) {
                if (m.person().image().medium() != null) {
                    if (!m.person().image().medium().isEmpty()) {
                        cargarImagenConRespaldo(m.person().image().medium(), iv);
                        tieneImagenPerson = true;
                    }
                }
            }
        }
        
        if (!tieneImagenPerson) {
            ponerImagenError(iv);
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
        
        b.getChildren().add(iv);
        b.getChildren().add(n);
        b.getChildren().add(c); 
        
        return b;
    }

    private void moverCarruselDinamico(ScrollPane sp, int dir) {
        sp.setHvalue(Math.max(0, Math.min(sp.getHvalue() + (dir * 0.2), 1)));
    }
    
    private void cargarImagenConRespaldo(String url, ImageView imageView) {
        try {
            Image img = new Image(url, true);
            
            img.errorProperty().addListener((obs, oldVal, isError) -> {
                if (isError) {
                    Platform.runLater(() -> {
                        ponerImagenError(imageView);
                    });
                }
            });
            
            imageView.setImage(img);
        } catch (Exception e) {
            ponerImagenError(imageView);
        }
    }

    private void cargarImagenErrorEnCache() {
        try {
            String errorPath = getClass().getResource("/com/src/filmtracker/images/error.png").toExternalForm();
            this.fallbackErrorImage = new Image(errorPath, true);
        } catch (Exception ex) {
        }
    }

    private void ponerImagenError(ImageView imageView) {
        if (this.fallbackErrorImage != null) {
            imageView.setImage(this.fallbackErrorImage);
        }
    }
}