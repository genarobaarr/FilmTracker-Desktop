package com.src.filmtracker.controllers;

import com.src.filmtracker.App;
import com.src.filmtracker.models.*;
import com.src.filmtracker.services.ShowService;
import com.src.filmtracker.services.IShowService;
import com.src.filmtracker.utils.AppConstants;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ShowDetailController {

    @FXML private Label titleLabel, ratingLabel, statusLabel, genresLabel, summaryLabel;
    @FXML private ImageView posterDetail;
    @FXML private HBox castContainer;
    @FXML private VBox episodesContainer, similarShowsSection; 
    @FXML private ScrollPane scrollCast; 

    private IShowService apiService;
    private final Map<Integer, List<EpisodeDto>> seasonEpisodesMap = new ConcurrentHashMap<>();

    public ShowDetailController() {
        this.apiService = new ShowService();
    }

    public ShowDetailController(IShowService apiService) {
        this.apiService = apiService;
    }

    @FXML private void handleClose() { 
        Platform.exit(); System.exit(0); 
    }
    @FXML private void handleMinimize() { 
        ((Stage)titleLabel.getScene().getWindow()).setIconified(true); 
    }
    @FXML private void scrollIzqCast() { 
        moverCarruselDinamico(scrollCast, -1); 
    }
    @FXML private void scrollDerCast() { 
        moverCarruselDinamico(scrollCast, 1); 
    }
    @FXML private void handleBack() { 
        App.goBackFromDetail(); 
    }
    @FXML private void handleHome() { 
        App.setRoot(AppConstants.FXML_DASHBOARD); 
    }
    
    public void initData(Show basicShow) {
        cargarDatosBasicos(basicShow);

        apiService.getFullShowDetails(basicShow.tvmazeId()).thenAccept(fullData -> {
            Platform.runLater(() -> {
                cargarDatosBasicos(fullData.show()); 
                
                castContainer.getChildren().clear();
                scrollCast.setHvalue(0.0); 
                fullData.cast().forEach(member -> castContainer.getChildren().add(createPersonBox(member)));

                episodesContainer.getChildren().clear();
                fullData.seasons().forEach(season -> episodesContainer.getChildren().add(createSeasonAccordion(season)));

                if (fullData.show().genres() != null && !fullData.show().genres().isEmpty()) {
                    cargarSeriesSimilaresMultigenero(fullData.show().genres(), basicShow.tvmazeId());
                } else {
                    mostrarMensajeSimilaresVacio();
                }
            });
        }).exceptionally(e -> {
            System.err.println(AppConstants.MESSAGE_ERROR_API + " Detalles: " + e.getMessage());
            return null;
        });

        apiService.getShowEpisodes(basicShow.tvmazeId()).thenAccept(episodes -> {
            if (episodes != null) {
                episodes.forEach(ep -> seasonEpisodesMap.computeIfAbsent(ep.season(), k -> new ArrayList<>()).add(ep));
            }
        }).exceptionally(e -> {
            System.err.println(AppConstants.MESSAGE_ERROR_API + " Episodios: " + e.getMessage());
            return null;
        });
    }

    private void cargarSeriesSimilaresMultigenero(List<String> genres, Integer currentShowId) {
        List<CompletableFuture<List<Show>>> futures = genres.stream()
                .map(apiService::getShowsByGenre)
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .flatMap(List::stream)
                        .filter(show -> !show.tvmazeId().equals(currentShowId))
                        .filter(distinctByKey(Show::tvmazeId))
                        .collect(Collectors.toList())
                )
                .thenAccept(combinedShows -> {
                    if (combinedShows.isEmpty()) {
                        Platform.runLater(this::mostrarMensajeSimilaresVacio);
                        return;
                    }

                    Collections.shuffle(combinedShows);
                    
                    List<Show> finalShows = combinedShows.stream()
                            .limit(AppConstants.HOME_CAROUSEL_LIMIT)
                            .toList();

                    Platform.runLater(() -> renderSimilarShowsCarousel(finalShows));
                })
                .exceptionally(e -> {
                    System.err.println(AppConstants.MESSAGE_ERROR_SIMILAR + " " + e.getMessage());
                    Platform.runLater(this::mostrarMensajeSimilaresVacio);
                    return null;
                });
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    private void renderSimilarShowsCarousel(List<Show> filteredShows) {
        similarShowsSection.getChildren().clear();
        
        HBox content = new HBox(15);
        content.setPadding(new Insets(10));
        
        filteredShows.forEach(show -> agregarTarjetaShow(show, content));

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToHeight(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        Button btnIzq = new Button("<");
        btnIzq.setStyle("-fx-background-color: #1e1e1e; -fx-text-fill: white; -fx-font-size: 18px; -fx-cursor: hand;");
        btnIzq.setOnAction(e -> moverCarruselDinamico(scrollPane, -1));

        Button btnDer = new Button(">");
        btnDer.setStyle("-fx-background-color: #1e1e1e; -fx-text-fill: white; -fx-font-size: 18px; -fx-cursor: hand;");
        btnDer.setOnAction(e -> moverCarruselDinamico(scrollPane, 1));

        BorderPane carousel = new BorderPane();
        carousel.setLeft(btnIzq);
        carousel.setCenter(scrollPane);
        carousel.setRight(btnDer);
        BorderPane.setAlignment(btnIzq, Pos.CENTER);
        BorderPane.setAlignment(btnDer, Pos.CENTER);

        similarShowsSection.getChildren().add(carousel);
    }

    private void agregarTarjetaShow(Show show, HBox contenedor) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(AppConstants.FXML_SHOW_CARD));
            VBox card = loader.load();
            ShowCardController controller = loader.getController();
            controller.setData(show);
            contenedor.getChildren().add(card);
        } catch (IOException e) {
            System.err.println(AppConstants.MESSAGE_ERROR_LOAD_CARD + " " + show.name());
        }
    }

    private void mostrarMensajeSimilaresVacio() {
        Label empty = new Label(AppConstants.MESSAGE_INFO_NO_SIMILAR);
        empty.setTextFill(Color.GRAY);
        empty.setStyle("-fx-font-size: 14px; -fx-padding: 20;");
        similarShowsSection.getChildren().clear();
        similarShowsSection.getChildren().add(empty);
    }

    private void cargarDatosBasicos(Show show) {
        titleLabel.setText(show.name() != null ? show.name() : "Desconocido");
        statusLabel.setText("Status: " + (show.status() != null ? show.status() : "N/A"));
        
        if (show.genres() != null) {
            genresLabel.setText("Géneros: " + String.join(", ", show.genres()));
        }
        
        String rating = (show.rating() != null && show.rating().average() != null) 
                ? String.valueOf(show.rating().average()) : AppConstants.MESSAGE_RATING_NA;
        ratingLabel.setText("⭐ " + rating);

        if (show.summary() != null) {
            summaryLabel.setText(show.summary().replaceAll("<[^>]*>", ""));
        }

        if (show.image() != null && show.image().original() != null) {
            posterDetail.setImage(new Image(show.image().original(), true));
        }
    }

    private VBox createPersonBox(CastDto member) {
        VBox box = new VBox(5);
        box.setAlignment(Pos.TOP_CENTER); 
        box.setPrefWidth(120); 
        box.setMaxWidth(120);

        ImageView iv = new ImageView();
        iv.setFitHeight(150);
        iv.setFitWidth(110);
        if (member.person().image() != null && member.person().image().medium() != null) {
            iv.setImage(new Image(member.person().image().medium(), true));
        }
        
        Label name = new Label(member.person().name());
        name.setTextFill(Color.WHITE);
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        name.setWrapText(true); 
        name.setTextAlignment(TextAlignment.CENTER); 
        name.setAlignment(Pos.CENTER);
        
        Label character = new Label(member.character().name());
        character.setTextFill(Color.GRAY);
        character.setStyle("-fx-font-size: 12px;");
        character.setWrapText(true); 
        character.setTextAlignment(TextAlignment.CENTER); 
        character.setAlignment(Pos.CENTER);
        
        box.getChildren().addAll(iv, name, character);
        return box;
    }
    
    private VBox createSeasonAccordion(SeasonDto season) {
        VBox container = new VBox();
        container.setStyle("-fx-background-color: transparent;");

        HBox header = new HBox(15);
        header.setStyle("-fx-background-color: #1e1e1e; -fx-padding: 10 20; -fx-background-radius: 5; -fx-cursor: hand;");
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label number = new Label("Temp " + season.number());
        number.setTextFill(Color.web(AppConstants.COLOR_ACCENT));
        number.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        number.setMinWidth(70);
        
        Label info = new Label(season.episodeOrder() + " Episodios");
        info.setTextFill(Color.WHITE);
        info.setStyle("-fx-font-size: 14px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label arrowIndicator = new Label("▼");
        arrowIndicator.setTextFill(Color.GRAY);
        arrowIndicator.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        header.getChildren().addAll(number, info, spacer, arrowIndicator);

        VBox episodesList = new VBox();
        episodesList.setStyle("-fx-padding: 15; -fx-background-color: #151515; -fx-background-radius: 0 0 5 5;");
        episodesList.setVisible(false);
        episodesList.setManaged(false); 

        header.setOnMouseClicked(e -> {
            boolean isExpanded = episodesList.isVisible();
            
            episodesList.setVisible(!isExpanded);
            episodesList.setManaged(!isExpanded);
            arrowIndicator.setText(isExpanded ? "▼" : "▲");
            
            header.setStyle(isExpanded 
                ? "-fx-background-color: #1e1e1e; -fx-padding: 10 20; -fx-background-radius: 5; -fx-cursor: hand;" 
                : "-fx-background-color: #2a2a2a; -fx-padding: 10 20; -fx-background-radius: 5 5 0 0; -fx-cursor: hand;");

            if (!isExpanded) {
                poblarListaDeEpisodios(season.number(), episodesList);
            }
        });

        container.getChildren().addAll(header, episodesList);
        return container;
    }

    private void poblarListaDeEpisodios(Integer seasonNumber, VBox container) {
        container.getChildren().clear(); 
        
        List<EpisodeDto> episodes = seasonEpisodesMap.get(seasonNumber);
        
        if (episodes == null || episodes.isEmpty()) {
            Label empty = new Label(episodes == null ? "Cargando episodios..." : "No hay episodios disponibles.");
            empty.setTextFill(Color.GRAY);
            empty.setStyle("-fx-font-size: 14px; -fx-padding: 20;");
            container.getChildren().add(empty);
            return;
        }

        HBox carouselContent = new HBox(15);
        carouselContent.setPadding(new Insets(10));
        
        for (EpisodeDto ep : episodes) {
            carouselContent.getChildren().add(createEpisodeCard(ep));
        }

        ScrollPane scrollPane = new ScrollPane(carouselContent);
        scrollPane.setFitToHeight(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        Button btnIzq = new Button("<");
        btnIzq.setStyle("-fx-background-color: #1e1e1e; -fx-text-fill: white; -fx-font-size: 18px; -fx-cursor: hand;");
        btnIzq.setOnAction(e -> moverCarruselDinamico(scrollPane, -1));

        Button btnDer = new Button(">");
        btnDer.setStyle("-fx-background-color: #1e1e1e; -fx-text-fill: white; -fx-font-size: 18px; -fx-cursor: hand;");
        btnDer.setOnAction(e -> moverCarruselDinamico(scrollPane, 1));

        BorderPane interactiveCarousel = new BorderPane();
        interactiveCarousel.setLeft(btnIzq);
        interactiveCarousel.setCenter(scrollPane);
        interactiveCarousel.setRight(btnDer);
        BorderPane.setAlignment(btnIzq, Pos.CENTER);
        BorderPane.setAlignment(btnDer, Pos.CENTER);

        container.getChildren().add(interactiveCarousel);
    }

    private VBox createEpisodeCard(EpisodeDto ep) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: #222222; -fx-background-radius: 8; -fx-padding: 12;");
        card.setPrefWidth(260); 
        card.setMaxWidth(260);

        ImageView iv = new ImageView();
        iv.setFitWidth(236);
        iv.setFitHeight(133);
        if (ep.image() != null && ep.image().medium() != null) {
            iv.setImage(new Image(ep.image().medium(), true));
        }

        Label title = new Label(String.format("S%dE%d - %s", ep.season(), ep.number(), ep.name() != null ? ep.name() : "Desconocido"));
        title.setTextFill(Color.WHITE);
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        title.setWrapText(true);

        String infoText = (ep.airdate() != null ? ep.airdate() : "N/A");
        if (ep.runtime() != null) infoText += " | " + ep.runtime() + " min";
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

    private void moverCarruselDinamico(ScrollPane scrollPane, int direccion) {
        double viewportWidth = scrollPane.getViewportBounds().getWidth();
        double contentWidth = scrollPane.getContent().getBoundsInLocal().getWidth();

        if (contentWidth <= viewportWidth || viewportWidth == 0) return;

        double scrollableWidth = contentWidth - viewportWidth;
        double avanceReal = viewportWidth - 275; 
        if (avanceReal <= 0) avanceReal = viewportWidth / 2; 

        double porcentajeSalto = avanceReal / scrollableWidth;
        double newValue = scrollPane.getHvalue() + (porcentajeSalto * direccion);

        scrollPane.setHvalue(Math.max(0.0, Math.min(newValue, 1.0)));
    }
}