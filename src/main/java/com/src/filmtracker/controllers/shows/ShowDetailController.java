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
import com.src.filmtracker.utils.CustomAlertHelper;
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
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class ShowDetailController {

    private static final String STYLE_BTN_TRANSPARENT_RED = "-fx-background-color: transparent; -fx-text-fill: #e50914; -fx-cursor: hand; -fx-underline: true;";
    private static final String STYLE_BTN_TRANSPARENT_ORANGE = "-fx-background-color: transparent; -fx-text-fill: #ff9800; -fx-cursor: hand; -fx-underline: true;";
    private static final String STYLE_BTN_TRANSPARENT_GREEN = "-fx-background-color: transparent; -fx-text-fill: #4caf50; -fx-cursor: hand; -fx-underline: true;";
    private static final String STYLE_BTN_TRANSPARENT_GREY = "-fx-background-color: transparent; -fx-text-fill: #aaaaaa; -fx-cursor: hand; -fx-underline: true;";
    private static final String STYLE_BTN_DARK_GREY = "-fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-cursor: hand;";
    private static final String STYLE_BTN_DARK_GREY_RADIUS = "-fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 4;";
    private static final String STYLE_BTN_RED_RADIUS = "-fx-background-color: #e50914; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 4;";
    private static final String STYLE_INPUT_BG = "-fx-control-inner-background: #2a2a2a; -fx-text-inner-color: white;";
    private static final String STYLE_CARD_BG = "-fx-background-color: #151515; -fx-padding: 15; -fx-background-radius: 8; -fx-border-color: #333;";
    private static final String STYLE_COMMENT_BG = "-fx-background-color: #1a1a1a; -fx-padding: 8; -fx-background-radius: 5;";
    private static final String STYLE_DISABLED_STAR = "-fx-font-size: 28px; -fx-text-fill: #555555; -fx-cursor: hand;";
    private static final String STYLE_ACTIVE_STAR = "-fx-font-size: 28px; -fx-text-fill: #ff9800; -fx-cursor: hand;";
    
    private static final String TEXT_ELIMINAR = "Eliminar";
    private static final String TEXT_QUITAR_IMAGEN = "Quitar Imagen";
    private static final String TEXT_USUARIO_DEF = "@Usuario";
    private static final String TEXT_DESCONOCIDO = "Desconocido";

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
        // Constructor por defecto
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
        if (basicShow == null || basicShow.tvmazeId() == null) {
            return;
        }
        
        this.currentTvmazeId = basicShow.tvmazeId();
        cargarDatosBasicosUI(basicShow);
        inyectarBotonesLibreria();

        apiService.getFullShowDetails(currentTvmazeId).thenAccept(fullData -> 
            Platform.runLater(() -> procesarDatosDetallados(fullData))
        ).exceptionally(e -> {
            App.procesarErrorCritico(e);
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
        }).exceptionally(e -> {
            App.procesarErrorCritico(e);
            return null;
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
        
        titleLabel.setText(show.name() != null ? show.name() : TEXT_DESCONOCIDO);
        statusLabel.setText("Status: " + (show.status() != null ? show.status() : "N/A"));
        
        if (show.genres() != null) {
            genresLabel.setText("Géneros: " + String.join(", ", show.genres()));
        }
        
        String rating = "N/A";
        if (show.rating() != null && show.rating().average() != null) {
            rating = String.valueOf(show.rating().average());
        }
        ratingLabel.setText("⭐ " + rating);
        
        if (show.summary() != null) {
            summaryLabel.setText(show.summary().replaceAll("<.*?>", ""));
        }
        
        if (show.image() != null && show.image().original() != null) {
            cargarImagenConRespaldo(show.image().original(), posterDetail);
        }
    }

    private void inyectarBotonesLibreria() {
        if (!SessionManager.getInstance().isAuthenticated()) {
            return;
        }
        
        HBox libBox = new HBox(15);
        libBox.setAlignment(Pos.CENTER_LEFT);
        
        Button favBtn = new Button("Cargando...");
        favBtn.setStyle(STYLE_BTN_DARK_GREY_RADIUS);
        
        Button watchBtn = new Button("Cargando...");
        watchBtn.setStyle(STYLE_BTN_DARK_GREY_RADIUS);
        
        libBox.getChildren().addAll(favBtn, watchBtn);
        
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
            if (list == null || list.isEmpty()) {
                finalizarConfiguracionFav(btn, false);
                return;
            }
            if (evaluarListaParaId(list, currentTvmazeId)) {
                finalizarConfiguracionFav(btn, true);
                return;
            }
            if (list.size() == 10) {
                buscarEnFavoritosRecursivo(page + 1, btn);
                return;
            }
            finalizarConfiguracionFav(btn, false);
        }).exceptionally(err -> {
            App.procesarErrorCritico(err);
            finalizarConfiguracionFav(btn, false);
            return null;
        });
    }

    private void finalizarConfiguracionFav(Button btn, boolean isFav) {
        Platform.runLater(() -> {
            actualizarAparienciaFav(btn, isFav);
            btn.setOnAction(e -> handleToggleFav(btn));
        });
    }

    private void handleToggleFav(Button btn) {
        boolean currFav = btn.getText().contains("Quitar");
        actualizarAparienciaFav(btn, !currFav);
        
        if (currFav) {
            libraryService.removeFavorite(currentTvmazeId).exceptionally(err -> {
                Platform.runLater(() -> {
                    if (!App.procesarErrorCritico(err)) {
                        actualizarAparienciaFav(btn, true);
                        mostrarAlertaError(AppConstants.MESSAGE_ERROR_API);
                    }
                });
                return null;
            });
        } else {
            libraryService.addFavorite(currentTvmazeId).exceptionally(err -> {
                Platform.runLater(() -> {
                    if (!App.procesarErrorCritico(err)) {
                        actualizarAparienciaFav(btn, false);
                        mostrarAlertaError(err.toString().contains("409") ? AppConstants.MESSAGE_ERROR_DUPLICATE_LIB : AppConstants.MESSAGE_ERROR_API);
                    }
                });
                return null;
            });
        }
    }

    private void actualizarAparienciaFav(Button btn, boolean isFav) {
        if (isFav) {
            btn.setText("Quitar de Favoritos (💔)");
            btn.setStyle(STYLE_BTN_RED_RADIUS);
        } else {
            btn.setText("Añadir a Favoritos (❤️)");
            btn.setStyle(STYLE_BTN_DARK_GREY_RADIUS);
        }
    }

    private void configurarBtnWatchlist(Button btn) {
        buscarEnWatchlistRecursivo(1, btn);
    }

    private void buscarEnWatchlistRecursivo(int page, Button btn) {
        libraryService.getWatchlistPaged(page).thenAccept(list -> {
            if (list == null || list.isEmpty()) {
                finalizarConfiguracionWatch(btn, false);
                return;
            }
            if (evaluarListaParaId(list, currentTvmazeId)) {
                finalizarConfiguracionWatch(btn, true);
                return;
            }
            if (list.size() == 10) {
                buscarEnWatchlistRecursivo(page + 1, btn);
                return;
            }
            finalizarConfiguracionWatch(btn, false);
        }).exceptionally(err -> {
            App.procesarErrorCritico(err);
            finalizarConfiguracionWatch(btn, false);
            return null;
        });
    }

    private void finalizarConfiguracionWatch(Button btn, boolean isWatch) {
        Platform.runLater(() -> {
            actualizarAparienciaWatch(btn, isWatch);
            btn.setOnAction(e -> handleToggleWatch(btn));
        });
    }
    
    private boolean evaluarListaParaId(List<LibraryItemDto> list, Integer targetId) {
        for (LibraryItemDto item : list) {
            if (item.tvmazeId() != null && item.tvmazeId().equals(targetId)) {
                return true;
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
                    if (!App.procesarErrorCritico(err)) {
                        actualizarAparienciaWatch(btn, true);
                        mostrarAlertaError(AppConstants.MESSAGE_ERROR_API);
                    }
                });
                return null;
            });
        } else {
            libraryService.addWatchlist(currentTvmazeId).exceptionally(err -> {
                Platform.runLater(() -> {
                    if (!App.procesarErrorCritico(err)) {
                        actualizarAparienciaWatch(btn, false);
                        mostrarAlertaError(err.toString().contains("409") ? AppConstants.MESSAGE_ERROR_DUPLICATE_LIB : AppConstants.MESSAGE_ERROR_API);
                    }
                });
                return null;
            });
        }
    }

    private void actualizarAparienciaWatch(Button btn, boolean isWatch) {
        if (isWatch) {
            btn.setText("Quitar de Watchlist (➖)");
            btn.setStyle(STYLE_BTN_RED_RADIUS);
        } else {
            btn.setText("Añadir a Watchlist (➕)");
            btn.setStyle(STYLE_BTN_DARK_GREY_RADIUS);
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
        if (genres == null || genres.isEmpty()) {
            return;
        }
        
        List<CompletableFuture<List<Show>>> futures = new ArrayList<>();
        for (String g : genres) {
            futures.add(apiService.getShowsByGenre(g));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> mapearShowsSimilares(futures))
            .thenAccept(combined -> {
                Collections.shuffle(combined);
                List<Show> limited = combined.stream().limit(AppConstants.HOME_CAROUSEL_LIMIT).toList();
                Platform.runLater(() -> renderSimilarCarousel(limited));
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
                // Ignorado intencionalmente 
            }
        }
        return result;
    }

    private void procesarShowSimilar(Show s, Set<Integer> seen, List<Show> result) {
        if (s.tvmazeId() != null && !s.tvmazeId().equals(currentTvmazeId)) {
            if (seen.add(s.tvmazeId())) {
                result.add(s);
            }
        }
    }

    private void renderSimilarCarousel(List<Show> shows) {
        similarShowsSection.getChildren().clear();
        
        if (shows == null || shows.isEmpty()) {
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

        Button bI = new Button("<"); 
        bI.setStyle(STYLE_BTN_DARK_GREY);
        
        Button bD = new Button(">"); 
        bD.setStyle(STYLE_BTN_DARK_GREY);
        
        bI.setOnAction(e -> moverCarruselDinamico(sp, -1)); 
        bD.setOnAction(e -> moverCarruselDinamico(sp, 1));
        
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
        HBox h = new HBox();
        h.setAlignment(Pos.CENTER_LEFT);
        h.setStyle("-fx-background-color: #1e1e1e; -fx-padding: 10; -fx-cursor: hand;");
        
        Label lblTitle = new Label("Temporada " + s.number());
        lblTitle.setTextFill(Color.WHITE);
        lblTitle.setStyle("-fx-font-weight: bold;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label lblIndicator = new Label("Episodios ▼");
        lblIndicator.setTextFill(Color.LIGHTGRAY);
        
        h.getChildren().addAll(lblTitle, spacer, lblIndicator);
        
        VBox eps = new VBox(); 
        eps.setVisible(false); 
        eps.setManaged(false);
        eps.setStyle("-fx-padding: 15; -fx-background-color: #151515; -fx-background-radius: 0 0 5 5;");
        
        configurarEventoAcordeon(h, eps, lblIndicator, s.number());
        
        c.getChildren().addAll(h, eps);
        return c;
    }

    private void configurarEventoAcordeon(HBox header, VBox eps, Label lblIndicator, int seasonNumber) {
        header.setOnMouseClicked(e -> {
            boolean mostrar = !eps.isVisible();
            eps.setVisible(mostrar);
            eps.setManaged(mostrar);
            
            if (mostrar) {
                lblIndicator.setText("Episodios ▲");
                if (eps.getChildren().isEmpty()) {
                    renderEpisodes(seasonNumber, eps);
                }
            } else {
                lblIndicator.setText("Episodios ▼");
            }
        });
    }

    private void renderEpisodes(int num, VBox container) {
        container.getChildren().clear();
        List<EpisodeDto> list = seasonEpisodesMap.get(num);
        
        if (list == null || list.isEmpty()) {
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

        Button bI = new Button("<"); 
        bI.setStyle(STYLE_BTN_DARK_GREY);
        
        Button bD = new Button(">"); 
        bD.setStyle(STYLE_BTN_DARK_GREY);
        
        bI.setOnAction(e -> moverCarruselDinamico(sp, -1));
        bD.setOnAction(e -> moverCarruselDinamico(sp, 1));

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
        if (ep.image() != null && ep.image().medium() != null && !ep.image().medium().isEmpty()) {
            cargarImagenConRespaldo(ep.image().medium(), iv);
            tieneImagen = true;
        }
        
        if (!tieneImagen) {
            ponerImagenError(iv);
        }

        String name = ep.name() != null ? ep.name() : TEXT_DESCONOCIDO;
        Label title = new Label(String.format("S%dE%d - %s", ep.season(), ep.number(), name));
        title.setTextFill(Color.WHITE);
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        title.setWrapText(true);

        String infoText = ep.airdate() != null ? ep.airdate() : "N/A";
        if (ep.runtime() != null) {
            infoText += " | " + ep.runtime() + " min";
        }
        
        Label info = new Label(infoText);
        info.setTextFill(Color.web("#aaaaaa")); 
        info.setStyle("-fx-font-size: 12px;");

        Label summary = new Label(ep.summary() != null ? ep.summary().replaceAll("<.*?>", "") : "Sin descripción disponible.");
        summary.setTextFill(Color.web("#cccccc"));
        summary.setWrapText(true);
        summary.setPrefHeight(60); 
        summary.setTextAlignment(TextAlignment.JUSTIFY);

        card.getChildren().addAll(iv, title, info, summary);
        return card;
    }

    private void cargarResenas(int page) {
        reviewService.getShowReviews(currentTvmazeId, page).thenAccept(res -> 
            Platform.runLater(() -> dibujarSeccionResenas(res, page, false))
        ).exceptionally(e -> {
            Platform.runLater(() -> {
                if (!App.procesarErrorCritico(e)) {
                    dibujarSeccionResenas(null, page, true);
                }
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
        
        if (res == null || res.reviews() == null || res.reviews().isEmpty()) {
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
        if (pag != null && Boolean.TRUE.equals(pag.hasNextPage())) {
            Button btnMore = new Button("Cargar más reseñas");
            btnMore.setStyle(STYLE_BTN_DARK_GREY_RADIUS + " -fx-padding: 8 15;");
            
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

    private File openImageChooser() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Seleccionar Imagen");
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg");
        fc.getExtensionFilters().add(filter);
        return fc.showOpenDialog(titleLabel.getScene().getWindow());
    }

    private void inyectarImagenSiExiste(String url, VBox card) {
        if (url == null || url.isEmpty()) {
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
        titleIn.setStyle(STYLE_INPUT_BG);
        
        int[] ratingState = new int[]{0};
        HBox starBox = buildStarRatingComponent(ratingState, 0);
        
        TextArea contIn = new TextArea(); 
        contIn.setPromptText("Tu reseña..."); 
        contIn.setPrefRowCount(3);
        contIn.setStyle(STYLE_INPUT_BG);
        
        Label fileLabel = new Label("Sin imagen");
        fileLabel.setTextFill(Color.GRAY);
        
        Button attachBtn = new Button("Adjuntar Imagen");
        attachBtn.setStyle(STYLE_BTN_DARK_GREY);
        
        attachBtn.setOnAction(e -> {
            File file = openImageChooser();
            if (file != null) {
                selectedReviewImage = file;
                fileLabel.setText(file.getName());
            }
        });

        HBox fileBox = new HBox(10, attachBtn, fileLabel);
        fileBox.setAlignment(Pos.CENTER_LEFT);

        Button btn = new Button("Publicar"); 
        btn.setStyle(STYLE_BTN_RED_RADIUS);
        btn.setOnAction(e -> enviarResena(ratingState, titleIn, contIn, errLbl));
        
        form.getChildren().addAll(new Label("Escribe una reseña:"), errLbl, titleIn, starBox, contIn, fileBox, btn);
        return form;
    }

    private void enviarResena(int[] ratingState, TextField titleIn, TextArea contIn, Label errLbl) {
        UserDto currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null && !currentUser.isVerified()) {
            mostrarAlertaPrecaucion(AppConstants.MESSAGE_ERROR_UNVERIFIED);
            return;
        }
        
        if (ratingState == null || ratingState.length == 0 || ratingState[0] == 0 || titleIn.getText().isBlank() || contIn.getText().isBlank()) {
            return;
        }
        errLbl.setVisible(false); 
        errLbl.setManaged(false);
        
        ReviewRequest req = new ReviewRequest(currentTvmazeId, ratingState[0], titleIn.getText().trim(), contIn.getText().trim());
        
        reviewService.createReview(req).thenAccept(review -> {
            if (selectedReviewImage != null && review != null) {
                reviewService.uploadReviewImage(review.getSafeId(), selectedReviewImage).thenRun(this::limpiarYRefrescarResenas);
                return;
            }
            limpiarYRefrescarResenas();
        }).exceptionally(err -> {
            Platform.runLater(() -> {
                if (!App.procesarErrorCritico(err)) {
                    mostrarAlertaError(err.toString().contains("409") ? AppConstants.MESSAGE_ERROR_DUPLICATE_REVIEW : AppConstants.MESSAGE_ERROR_REVIEW_ACTION);
                }
            });
            return null;
        });
    }
    
    private void injectReviewActions(ReviewDto review, HBox actions, VBox card) {
        if (!SessionManager.getInstance().isAuthenticated() || review.getOwnerId() == null || review.getOwnerId().isEmpty()) {
            return;
        }
        
        UserDto currUser = SessionManager.getInstance().getCurrentUser();
        String currAuth = currUser.getSafeAuthId();
        boolean isAdmin = "ADMIN".equals(currUser.role());
        
        if (review.getOwnerId().equals(currAuth)) {
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
        if (!SessionManager.getInstance().isAuthenticated() || c.getOwnerId() == null || c.getOwnerId().isEmpty()) {
            return;
        }
        
        UserDto currUser = SessionManager.getInstance().getCurrentUser();
        String currAuth = currUser.getSafeAuthId();
        boolean isAdmin = "ADMIN".equals(currUser.role());
        String cId = c.getSafeId();
        
        if (c.getOwnerId().equals(currAuth)) {
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
        rep.setStyle(STYLE_BTN_TRANSPARENT_RED);
        rep.setOnAction(e -> com.src.filmtracker.utils.ReportModalHelper.openReportModal(targetType, targetId));
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
        Window owner = titleLabel.getScene().getWindow();
        return CustomAlertHelper.mostrarConfirmacion(titulo, contenido, owner);
    }

    private void mostrarAlertaError(String mensaje) {
        CustomAlertHelper.mostrarError(mensaje);
    }
    
    private void mostrarAlertaExito(String mensaje) {
        CustomAlertHelper.mostrarExito(mensaje);
    }
    
    private void mostrarAlertaPrecaucion(String mensaje) {
        Window owner = titleLabel.getScene().getWindow();
        if (com.src.filmtracker.utils.CustomAlertHelper.mostrarPrecaucion("Atención", mensaje, "Verificar ahora", "Cancelar", owner)) {
            App.setRoot(AppConstants.FXML_VERIFY_EMAIL);
        }
    }

    private String parsearIsoFormato(String isoString) {
        if (isoString == null || isoString.isEmpty()) {
            return "";
        }
        try {
            return ZonedDateTime.parse(isoString).format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"));
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
        if (!created.equals(updated) && !updated.isEmpty()) {
            return created + " (editado " + updated + ")";
        }
        return created;
    }

    private HBox buildHeaderWithDate(Label authorLabel, String createdAt, String updatedAt) {
        Label dateLabel = new Label(formatearFechaVisual(createdAt, updatedAt));
        dateLabel.setTextFill(Color.web("#888888"));
        dateLabel.setStyle("-fx-font-size: 11px;");
        
        HBox header = new HBox(10, authorLabel, dateLabel);
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    private VBox buildReviewCard(ReviewDto review) {
        String rId = review.getSafeId();
        VBox card = new VBox(8);
        card.setStyle(STYLE_CARD_BG);

        Label author = new Label(TEXT_USUARIO_DEF); 
        resolverNombreAutor(review.getOwnerId(), author);
        HBox header = buildHeaderWithDate(author, review.created_at(), review.updated_at());
        
        Label title = new Label(review.title() != null ? review.title() : "Sin título");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;"); 
        title.setTextFill(Color.WHITE);
        
        Label content = new Label(review.content() != null ? review.content() : "");
        content.setTextFill(Color.LIGHTGRAY); 
        content.setWrapText(true);

        card.getChildren().addAll(header, title, content);
        inyectarImagenSiExiste(review.getImageUrl(), card);

        HBox actions = new HBox(15); 
        actions.setAlignment(Pos.CENTER_LEFT);
        HBox likeContainer = buildLikeButtonsResena(rId, review.getLikesCount(), review.getIsLikedValue());

        VBox commContainer = new VBox(10); 
        commContainer.setManaged(false); 
        commContainer.setVisible(false);
        
        Button commBtn = new Button("Comentarios (" + review.getCommentsCount() + ")");
        commBtn.setStyle(STYLE_BTN_TRANSPARENT_GREY);
        
        commBtn.setOnAction(e -> {
            boolean show = !commContainer.isVisible();
            commContainer.setVisible(show); 
            commContainer.setManaged(show);
            if (show && !rId.isEmpty()) {
                cargarComentariosUI(rId, commContainer, 1);
            }
        });

        actions.getChildren().addAll(likeContainer, commBtn);
        injectReviewActions(review, actions, card);
        card.getChildren().addAll(actions, commContainer);
        
        return card;
    }

    private void resolverNombreAutor(String ownerId, Label label) {
        if (ownerId == null || ownerId.isEmpty()) {
            label.setText(TEXT_USUARIO_DEF);
            return;
        }
        if (userCache.containsKey(ownerId)) {
            UserDto cachedUser = userCache.get(ownerId);
            label.setText("@" + cachedUser.username());
            configurarClicAutor(label, cachedUser);
            return;
        }
        userService.getUserById(ownerId).thenAccept(user -> 
            Platform.runLater(() -> procesarUsuarioObtenido(user, ownerId, label))
        );
    }

    private void procesarUsuarioObtenido(UserDto user, String ownerId, Label label) {
        if (user != null && user.username() != null) {
            userCache.put(ownerId, user);
            label.setText("@" + user.username());
            configurarClicAutor(label, user);
            return;
        }
        label.setText(TEXT_USUARIO_DEF);
    }

    private void configurarClicAutor(Label label, UserDto user) {
        String baseStyle = label.getStyle() != null ? label.getStyle() : "";
        label.setStyle(baseStyle + " -fx-cursor: hand; -fx-underline: true; -fx-text-fill: " + AppConstants.COLOR_ACCENT + ";");
        label.setOnMouseClicked(e -> App.showProfileView(user));
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
        
        reviewService.getReviewComments(rId, page).thenAccept(res -> 
            Platform.runLater(() -> procesarComentariosObtenidos(res, rId, container, page))
        );
    }

    private void procesarComentariosObtenidos(CommentPaginationResponse res, String rId, VBox container, int page) {
        if (res != null && res.comments() != null) {
            for (CommentDto c : res.comments()) {
                container.getChildren().add(buildCommentItem(c, rId, container));
            }
            evaluarBotonCargarMasComentarios(rId, container, res.pagination(), page);
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
        if (pag != null && Boolean.TRUE.equals(pag.hasNextPage())) {
            Button btnMore = new Button("Cargar más comentarios");
            btnMore.setStyle(STYLE_BTN_TRANSPARENT_GREY);
            btnMore.setOnAction(e -> cargarComentariosUI(rId, container, currentPage + 1));
            HBox centerBox = new HBox(btnMore);
            centerBox.setAlignment(Pos.CENTER_LEFT);
            container.getChildren().add(centerBox);
        }
    }

    private void agregarFormularioComentario(String rId, VBox container) {
        TextField in = new TextField(); 
        in.setPromptText("Comentar...");
        in.setStyle(STYLE_INPUT_BG);
        
        Label cFileLabel = new Label("");
        cFileLabel.setTextFill(Color.GRAY);
        cFileLabel.setStyle("-fx-font-size: 10px;");

        Button imgBtn = new Button("📷");
        imgBtn.setStyle(STYLE_BTN_DARK_GREY);
        imgBtn.setOnAction(e -> {
            File f = openImageChooser();
            if (f != null) {
                selectedCommentImages.put(rId, f);
                cFileLabel.setText(f.getName());
            }
        });

        Button b = new Button("Enviar"); 
        b.setStyle("-fx-background-color: #555; -fx-text-fill: white; -fx-cursor: hand;");
        b.setOnAction(e -> procesarEnvioComentario(in, rId, container));
        
        HBox box = new HBox(10, in, imgBtn, b);
        container.getChildren().addAll(box, cFileLabel);
    }
    
    private void procesarEnvioComentario(TextField in, String rId, VBox container) {
        UserDto currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null && !currentUser.isVerified()) {
            mostrarAlertaPrecaucion(AppConstants.MESSAGE_ERROR_UNVERIFIED);
            return;
        }
        
        if (in.getText().isBlank()) {
            return;
        }
        
        CommentRequest req = new CommentRequest(in.getText().trim());
        File img = selectedCommentImages.get(rId);
        
        reviewService.createComment(rId, req, img).thenAccept(comment -> 
            refrescarComentarios(rId, container)
        ).exceptionally(err -> {
            Platform.runLater(() -> {
                if (!App.procesarErrorCritico(err)) {
                    mostrarAlertaError(AppConstants.MESSAGE_ERROR_REVIEW_ACTION);
                }
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
        box.setStyle(STYLE_COMMENT_BG);
        
        Label user = new Label("@Cargando..."); 
        resolverNombreAutor(c.getOwnerId(), user);
        HBox header = buildHeaderWithDate(user, c.created_at(), c.updated_at());
        
        Label txt = new Label(c.content() != null ? c.content() : ""); 
        txt.setTextFill(Color.WHITE);
        
        box.getChildren().addAll(header, txt);
        inyectarImagenSiExiste(c.getImageUrl(), box);
        
        HBox actions = new HBox(10);
        HBox likeContainer = buildLikeButtonsComentario(cId, c.getLikesCount(), c.getIsLikedValue());
        
        actions.getChildren().add(likeContainer);
        injectCommentActions(c, actions, rId, parent);
        box.getChildren().add(actions);
        
        return box;
    }
    
    private HBox buildLikeButtonsResena(String rId, int initialCount, boolean isLiked) {
        HBox container = new HBox(5);
        container.setAlignment(Pos.CENTER_LEFT);
        
        Button btnLike = new Button("👍 " + initialCount);
        Button btnUnlike = new Button("👎");
        
        actualizarEstadoBotonesLike(btnLike, btnUnlike, isLiked);
        
        if (!SessionManager.getInstance().isAuthenticated() || rId.isEmpty()) {
            deshabilitarBotonesCompletamente(btnLike, btnUnlike);
            container.getChildren().addAll(btnLike, btnUnlike);
            return container;
        }
        
        int[] countState = new int[]{initialCount};
        btnLike.setOnAction(e -> procesarLikeResena(rId, btnLike, btnUnlike, countState));
        btnUnlike.setOnAction(e -> procesarUnlikeResena(rId, btnLike, btnUnlike, countState));
        
        container.getChildren().addAll(btnLike, btnUnlike);
        return container;
    }

    private void procesarLikeResena(String rId, Button btnLike, Button btnUnlike, int[] countState) {
        btnLike.setDisable(true);
        reviewService.toggleReviewLike(rId, false).thenRun(() -> 
            Platform.runLater(() -> {
                countState[0]++;
                btnLike.setText("👍 " + countState[0]);
                actualizarEstadoBotonesLike(btnLike, btnUnlike, true);
            })
        ).exceptionally(err -> {
            Platform.runLater(() -> {
                if (!App.procesarErrorCritico(err)) btnLike.setDisable(false);
            });
            return null;
        });
    }

    private void procesarUnlikeResena(String rId, Button btnLike, Button btnUnlike, int[] countState) {
        btnUnlike.setDisable(true);
        reviewService.toggleReviewLike(rId, true).thenRun(() -> 
            Platform.runLater(() -> {
                countState[0] = Math.max(0, countState[0] - 1);
                btnLike.setText("👍 " + countState[0]);
                actualizarEstadoBotonesLike(btnLike, btnUnlike, false);
            })
        ).exceptionally(err -> {
            Platform.runLater(() -> {
                if (!App.procesarErrorCritico(err)) btnUnlike.setDisable(false);
            });
            return null;
        });
    }

    private HBox buildLikeButtonsComentario(String cId, int initialCount, boolean isLiked) {
        HBox container = new HBox(5);
        container.setAlignment(Pos.CENTER_LEFT);
        
        Button btnLike = new Button("👍 " + initialCount);
        Button btnUnlike = new Button("👎");
        
        actualizarEstadoBotonesLike(btnLike, btnUnlike, isLiked);
        
        if (!SessionManager.getInstance().isAuthenticated() || cId.isEmpty()) {
            deshabilitarBotonesCompletamente(btnLike, btnUnlike);
            container.getChildren().addAll(btnLike, btnUnlike);
            return container;
        }
        
        int[] countState = new int[]{initialCount};
        btnLike.setOnAction(e -> procesarLikeComentario(cId, btnLike, btnUnlike, countState));
        btnUnlike.setOnAction(e -> procesarUnlikeComentario(cId, btnLike, btnUnlike, countState));
        
        container.getChildren().addAll(btnLike, btnUnlike);
        return container;
    }

    private void procesarLikeComentario(String cId, Button btnLike, Button btnUnlike, int[] countState) {
        btnLike.setDisable(true);
        reviewService.toggleCommentLike(cId, false).thenRun(() -> 
            Platform.runLater(() -> {
                countState[0]++;
                btnLike.setText("👍 " + countState[0]);
                actualizarEstadoBotonesLike(btnLike, btnUnlike, true);
            })
        ).exceptionally(err -> {
            Platform.runLater(() -> {
                if (!App.procesarErrorCritico(err)) btnLike.setDisable(false);
            });
            return null;
        });
    }

    private void procesarUnlikeComentario(String cId, Button btnLike, Button btnUnlike, int[] countState) {
        btnUnlike.setDisable(true);
        reviewService.toggleCommentLike(cId, true).thenRun(() -> 
            Platform.runLater(() -> {
                countState[0] = Math.max(0, countState[0] - 1);
                btnLike.setText("👍 " + countState[0]);
                actualizarEstadoBotonesLike(btnLike, btnUnlike, false);
            })
        ).exceptionally(err -> {
            Platform.runLater(() -> {
                if (!App.procesarErrorCritico(err)) btnUnlike.setDisable(false);
            });
            return null;
        });
    }

    private void actualizarEstadoBotonesLike(Button btnLike, Button btnUnlike, boolean isLiked) {
        if (isLiked) {
            btnLike.setDisable(true);
            btnLike.setStyle("-fx-background-color: #e50914; -fx-text-fill: white; -fx-background-radius: 4; -fx-opacity: 1; -fx-font-weight: bold;");
            btnUnlike.setDisable(false);
            btnUnlike.setStyle(STYLE_BTN_DARK_GREY_RADIUS);
        } else {
            btnLike.setDisable(false);
            btnLike.setStyle(STYLE_BTN_DARK_GREY_RADIUS);
            btnUnlike.setDisable(true);
            btnUnlike.setStyle("-fx-background-color: #1a1a1a; -fx-text-fill: #555555; -fx-background-radius: 4; -fx-opacity: 1;");
        }
    }

    private void deshabilitarBotonesCompletamente(Button btnLike, Button btnUnlike) {
        btnLike.setDisable(true);
        btnUnlike.setDisable(true);
        btnLike.setStyle("-fx-background-color: #1a1a1a; -fx-text-fill: #555555; -fx-background-radius: 4; -fx-opacity: 1;");
        btnUnlike.setStyle("-fx-background-color: #1a1a1a; -fx-text-fill: #555555; -fx-background-radius: 4; -fx-opacity: 1;");
    }

    private boolean isWithinEditWindow(String createdAtStr) {
        if (createdAtStr == null || createdAtStr.isEmpty()) {
            return false;
        }
        try {
            ZonedDateTime created = ZonedDateTime.parse(createdAtStr);
            ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
            return ChronoUnit.MINUTES.between(created, now) <= 30;
        } catch (Exception e) {
            return false;
        }
    }

    private void agregarBotonEliminarResena(String rId, HBox actions) {
        Button del = new Button(TEXT_ELIMINAR);
        del.setStyle(STYLE_BTN_TRANSPARENT_RED);
        del.setOnAction(e -> {
            if (confirmarAccion("Eliminar Reseña", "¿Estás seguro de que deseas eliminar tu reseña?")) {
                reviewService.deleteReview(rId).thenRun(() -> 
                    Platform.runLater(() -> {
                        this.currentReviewPage = 1;
                        cargarResenas(1);
                    })
                ).exceptionally(err -> {
                    App.procesarErrorCritico(err);
                    return null;
                });
            }
        });
        actions.getChildren().add(del);
    }

    private void agregarBotonesAdminResena(ReviewDto review, HBox actions) {
        Button delAdmin = new Button(TEXT_ELIMINAR);
        delAdmin.setStyle(STYLE_BTN_TRANSPARENT_ORANGE);
        delAdmin.setOnAction(e -> {
            if (confirmarAccion("Eliminar Reseña", "¿Deseas eliminar administrativamente esta reseña?")) {
                adminService.deleteReviewDirectly(review.getSafeId()).thenRun(() -> 
                    Platform.runLater(() -> {
                        mostrarAlertaExito("Reseña eliminada administrativamente.");
                        this.currentReviewPage = 1;
                        cargarResenas(1);
                    })
                ).exceptionally(err -> {
                    Platform.runLater(() -> {
                        if (!App.procesarErrorCritico(err)) mostrarAlertaError(AppConstants.MESSAGE_ERROR_API);
                    });
                    return null;
                });
            }
        });
        actions.getChildren().add(delAdmin);

        if (review.getImageUrl() != null && !review.getImageUrl().isEmpty()) {
            Button delImgAdmin = new Button(TEXT_QUITAR_IMAGEN);
            delImgAdmin.setStyle(STYLE_BTN_TRANSPARENT_ORANGE);
            delImgAdmin.setOnAction(e -> {
                if (confirmarAccion(TEXT_QUITAR_IMAGEN, "¿Deseas eliminar la imagen de esta reseña?")) {
                    adminService.removeReviewImageDirectly(review.getSafeId()).thenRun(() -> 
                        Platform.runLater(() -> {
                            mostrarAlertaExito("Imagen eliminada administrativamente.");
                            this.currentReviewPage = 1;
                            cargarResenas(1);
                        })
                    ).exceptionally(err -> {
                        Platform.runLater(() -> {
                            if (!App.procesarErrorCritico(err)) mostrarAlertaError(AppConstants.MESSAGE_ERROR_API);
                        });
                        return null;
                    });
                }
            });
            actions.getChildren().add(delImgAdmin);
        }
    }
    
    private void agregarBotonEliminarComentario(String cId, String rId, VBox parent, HBox actions) {
        Button del = new Button(TEXT_ELIMINAR);
        del.setStyle(STYLE_BTN_TRANSPARENT_RED);
        del.setOnAction(e -> {
            if (confirmarAccion("Eliminar Comentario", "¿Estás seguro de que deseas eliminar tu comentario?")) {
                reviewService.deleteComment(cId).thenRun(() -> 
                    Platform.runLater(() -> cargarComentariosUI(rId, parent, 1))
                ).exceptionally(err -> {
                    App.procesarErrorCritico(err);
                    return null;
                });
            }
        });
        actions.getChildren().add(del);
    }

    private void agregarBotonesAdminComentario(CommentDto c, String rId, VBox parent, HBox actions) {
        Button delAdmin = new Button(TEXT_ELIMINAR);
        delAdmin.setStyle(STYLE_BTN_TRANSPARENT_ORANGE);
        delAdmin.setOnAction(e -> {
            if (confirmarAccion("Eliminar Comentario", "¿Deseas eliminar administrativamente este comentario?")) {
                adminService.deleteCommentDirectly(c.getSafeId()).thenRun(() -> 
                    Platform.runLater(() -> {
                        mostrarAlertaExito("Comentario eliminado administrativamente.");
                        cargarComentariosUI(rId, parent, 1);
                    })
                ).exceptionally(err -> {
                    Platform.runLater(() -> {
                        if (!App.procesarErrorCritico(err)) mostrarAlertaError(AppConstants.MESSAGE_ERROR_API);
                    });
                    return null;
                });
            }
        });
        actions.getChildren().add(delAdmin);

        if (c.getImageUrl() != null && !c.getImageUrl().isEmpty()) {
            Button delImgAdmin = new Button(TEXT_QUITAR_IMAGEN);
            delImgAdmin.setStyle(STYLE_BTN_TRANSPARENT_ORANGE);
            delImgAdmin.setOnAction(e -> {
                if (confirmarAccion(TEXT_QUITAR_IMAGEN, "¿Deseas eliminar la imagen de este comentario?")) {
                    adminService.removeCommentImageDirectly(c.getSafeId()).thenRun(() -> 
                        Platform.runLater(() -> {
                            mostrarAlertaExito("Imagen eliminada administrativamente.");
                            cargarComentariosUI(rId, parent, 1);
                        })
                    ).exceptionally(err -> {
                        Platform.runLater(() -> {
                            if (!App.procesarErrorCritico(err)) mostrarAlertaError(AppConstants.MESSAGE_ERROR_API);
                        });
                        return null;
                    });
                }
            });
            actions.getChildren().add(delImgAdmin);
        }
    }

    private void agregarBotonEditarResena(ReviewDto review, HBox actions, VBox card) {
        Button edit = new Button("Editar");
        edit.setStyle(STYLE_BTN_TRANSPARENT_GREEN);
        edit.setOnAction(e -> mostrarFormularioEdicion(review, card));
        actions.getChildren().add(edit);
    }

    private void mostrarFormularioEdicion(ReviewDto review, VBox card) {
        card.getChildren().clear();
        
        Label errLbl = new Label();
        errLbl.setTextFill(Color.web(AppConstants.COLOR_ACCENT));
        errLbl.setVisible(false);
        errLbl.setManaged(false);
        
        TextField titleIn = new TextField(review.title() != null ? review.title() : "");
        titleIn.setStyle(STYLE_INPUT_BG);
        
        int initialRating = review.rating() != null ? review.rating() : 0;
        int[] ratingState = new int[]{initialRating};
        HBox starBox = buildStarRatingComponent(ratingState, initialRating);
        
        TextArea contIn = new TextArea(review.content() != null ? review.content() : "");
        contIn.setPrefRowCount(3);
        contIn.setStyle(STYLE_INPUT_BG);
        
        Label fileLabel = new Label("Mantener imagen actual");
        fileLabel.setTextFill(Color.GRAY);
        
        Button attachBtn = new Button("Cambiar Imagen");
        attachBtn.setStyle(STYLE_BTN_DARK_GREY);
        attachBtn.setOnAction(e -> {
            File file = openImageChooser();
            if (file != null) {
                editReviewImages.put(review.getSafeId(), file);
                fileLabel.setText(file.getName());
            }
        });
        
        HBox fileBox = new HBox(10, attachBtn, fileLabel);
        fileBox.setAlignment(Pos.CENTER_LEFT);
        
        Button saveBtn = new Button("Guardar");
        saveBtn.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; -fx-cursor: hand;");
        saveBtn.setOnAction(e -> procesarEdicionResena(review.getSafeId(), ratingState, titleIn, contIn, errLbl));
        
        Button cancelBtn = new Button("Cancelar");
        cancelBtn.setStyle(STYLE_BTN_DARK_GREY);
        cancelBtn.setOnAction(e -> limpiarYRefrescarResenas());
        
        HBox actionsBox = new HBox(10, saveBtn, cancelBtn);
        
        card.getChildren().addAll(new Label("Editar reseña:"), errLbl, titleIn, starBox, contIn, fileBox, actionsBox);
    }

    private void procesarEdicionResena(String rId, int[] ratingState, TextField titleIn, TextArea contIn, Label errLbl) {
        if (ratingState == null || ratingState.length == 0 || ratingState[0] == 0 || titleIn.getText().isBlank() || contIn.getText().isBlank()) {
            return;
        }
        
        errLbl.setVisible(false);
        errLbl.setManaged(false);
        
        ReviewRequest req = new ReviewRequest(currentTvmazeId, ratingState[0], titleIn.getText().trim(), contIn.getText().trim());
        
        reviewService.updateReview(rId, req).thenAccept(review -> {
            File img = editReviewImages.get(rId);
            if (img != null && review != null) {
                reviewService.uploadReviewImage(review.getSafeId(), img).thenRun(() -> finalizarEdicionResena(rId));
                return;
            }
            finalizarEdicionResena(rId);
        }).exceptionally(err -> {
            Platform.runLater(() -> {
                if (!App.procesarErrorCritico(err)) mostrarAlertaError(AppConstants.MESSAGE_ERROR_REVIEW_ACTION);
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
            // Ignorado intencionalmente al renderizar elementos gráficos
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
        if (m.person() != null && m.person().image() != null && m.person().image().medium() != null && !m.person().image().medium().isEmpty()) {
            cargarImagenConRespaldo(m.person().image().medium(), iv);
            tieneImagenPerson = true;
        }
        
        if (!tieneImagenPerson) ponerImagenError(iv);
        
        String actorName = m.person() != null && m.person().name() != null ? m.person().name() : TEXT_DESCONOCIDO;
        Label n = new Label(actorName); 
        n.setTextFill(Color.WHITE); 
        n.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        n.setWrapText(true); 
        n.setTextAlignment(TextAlignment.CENTER);
        
        String charName = m.character() != null && m.character().name() != null ? m.character().name() : "";
        Label c = new Label(charName);
        c.setTextFill(Color.GRAY); 
        c.setStyle("-fx-font-size: 12px;");
        c.setWrapText(true); 
        c.setTextAlignment(TextAlignment.CENTER);
        
        b.getChildren().addAll(iv, n, c); 
        return b;
    }

    private void moverCarruselDinamico(ScrollPane sp, int dir) {
        sp.setHvalue(Math.clamp(sp.getHvalue() + (dir * 0.2), 0.0, 1.0));
    }
    
    private void cargarImagenConRespaldo(String url, ImageView imageView) {
        try {
            Image img = new Image(url, true);
            img.errorProperty().addListener((obs, oldVal, isError) -> {
                if (isError) Platform.runLater(() -> ponerImagenError(imageView));
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
            // Falla silenciosa permitida para imágenes estáticas
        }
    }

    private void ponerImagenError(ImageView imageView) {
        if (this.fallbackErrorImage != null) imageView.setImage(this.fallbackErrorImage);
    }
    
    private HBox buildStarRatingComponent(int[] ratingState, int initialValue) {
        HBox starContainer = new HBox(5);
        starContainer.setAlignment(Pos.CENTER_LEFT);
        List<Label> stars = new ArrayList<>();
        
        for (int i = 1; i <= 5; i++) {
            Label star = new Label("★");
            star.setStyle(STYLE_DISABLED_STAR);
            stars.add(star);
            starContainer.getChildren().add(star);
        }
        configurarInteraccionesEstrellas(stars, ratingState, initialValue);
        return starContainer;
    }

    private void configurarInteraccionesEstrellas(List<Label> stars, int[] ratingState, int initialValue) {
        if (initialValue > 0) {
            ratingState[0] = initialValue;
            actualizarColorEstrellas(stars, initialValue);
        }
        
        int index = 1;
        for (Label star : stars) {
            final int starValue = index;
            star.setOnMouseEntered(e -> actualizarColorEstrellas(stars, starValue));
            star.setOnMouseExited(e -> actualizarColorEstrellas(stars, ratingState[0]));
            star.setOnMouseClicked(e -> {
                ratingState[0] = starValue;
                actualizarColorEstrellas(stars, starValue);
            });
            index++;
        }
    }

    private void actualizarColorEstrellas(List<Label> stars, int limit) {
        int index = 1;
        for (Label star : stars) {
            star.setStyle(index <= limit ? STYLE_ACTIVE_STAR : STYLE_DISABLED_STAR);
            index++;
        }
    }
}