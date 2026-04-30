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
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ShowDetailController {

    @FXML private Label titleLabel, ratingLabel, statusLabel, genresLabel, summaryLabel;
    @FXML private ImageView posterDetail;
    @FXML private HBox castContainer;
    @FXML private VBox episodesContainer, similarShowsSection, reviewsSection; 
    @FXML private ScrollPane scrollCast; 

    private final IShowService apiService = new ShowService();
    private final IReviewService reviewService = new ReviewService();
    private final Map<Integer, List<EpisodeDto>> seasonEpisodesMap = new ConcurrentHashMap<>();
    private Integer currentTvmazeId;

    @FXML private void handleClose() { 
        Platform.exit(); System.exit(0); 
    }
    @FXML private void handleMinimize() { 
        ((Stage)titleLabel.getScene().getWindow()).setIconified(true); 
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
        if (basicShow == null || basicShow.tvmazeId() == null) return;
        this.currentTvmazeId = basicShow.tvmazeId();
        cargarDatosBasicosUI(basicShow);

        apiService.getFullShowDetails(currentTvmazeId).thenAccept(fullData -> {
            Platform.runLater(() -> {
                if (fullData == null) return;
                cargarDatosBasicosUI(fullData.show()); 
                actualizarCastUI(fullData.cast());
                actualizarTemporadasUI(fullData.seasons());
                if (fullData.show() != null && fullData.show().genres() != null) {
                    cargarSeriesSimilaresMultigenero(fullData.show().genres());
                }
            });
        }).exceptionally(e -> { System.err.println(e.getMessage()); return null; });

        apiService.getShowEpisodes(currentTvmazeId).thenAccept(episodes -> {
            if (episodes != null) episodes.forEach(ep -> seasonEpisodesMap.computeIfAbsent(ep.season(), k -> new ArrayList<>()).add(ep));
        });

        cargarResenas();
    }

    private void cargarDatosBasicosUI(Show show) {
        if (show == null) return; 
        titleLabel.setText(show.name() != null ? show.name() : "Desconocido");
        statusLabel.setText("Status: " + (show.status() != null ? show.status() : "N/A"));
        if (show.genres() != null) genresLabel.setText("Géneros: " + String.join(", ", show.genres()));
        String rating = (show.rating() != null && show.rating().average() != null) ? String.valueOf(show.rating().average()) : "N/A";
        ratingLabel.setText("⭐ " + rating);
        if (show.summary() != null) summaryLabel.setText(show.summary().replaceAll("<[^>]*>", ""));
        if (show.image() != null && show.image().original() != null) posterDetail.setImage(new Image(show.image().original(), true));
    }

    private void actualizarCastUI(List<CastDto> cast) {
        castContainer.getChildren().clear();
        scrollCast.setHvalue(0.0);
        if (cast != null) cast.forEach(m -> castContainer.getChildren().add(buildPersonBox(m)));
    }

    private void actualizarTemporadasUI(List<SeasonDto> seasons) {
        episodesContainer.getChildren().clear();
        if (seasons != null) seasons.forEach(s -> episodesContainer.getChildren().add(createSeasonAccordion(s)));
    }

    private void cargarSeriesSimilaresMultigenero(List<String> genres) {
        if (genres == null || genres.isEmpty()) return;
        List<CompletableFuture<List<Show>>> futures = genres.stream().map(apiService::getShowsByGenre).toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> futures.stream().map(CompletableFuture::join).flatMap(List::stream)
                .filter(s -> s.tvmazeId() != null && !s.tvmazeId().equals(currentTvmazeId))
                .filter(distinctByKey(Show::tvmazeId)).collect(Collectors.toList()))
            .thenAccept(combined -> {
                Collections.shuffle(combined);
                Platform.runLater(() -> renderSimilarCarousel(combined.stream().limit(20).toList()));
            });
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    private void renderSimilarCarousel(List<Show> shows) {
        similarShowsSection.getChildren().clear();
        if (shows == null || shows.isEmpty()) {
            similarShowsSection.getChildren().add(new Label("No hay series similares disponibles."));
            return;
        }
        HBox content = new HBox(15);
        content.setPadding(new Insets(10));
        shows.forEach(s -> injectShowCard(s, content));
        
        ScrollPane sp = new ScrollPane(content);
        sp.setFitToHeight(true);
        sp.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        Button bI = new Button("<"); Button bD = new Button(">");
        bI.setOnAction(e -> moverCarruselDinamico(sp, -1));
        bD.setOnAction(e -> moverCarruselDinamico(sp, 1));
        
        BorderPane bp = new BorderPane(sp);
        bp.setLeft(bI); bp.setRight(bD);
        similarShowsSection.getChildren().add(bp);
    }

    private void cargarResenas() {
        reviewService.getShowReviews(currentTvmazeId).thenAccept(reviews -> {
            Platform.runLater(() -> {
                reviewsSection.getChildren().clear();
                if (SessionManager.getInstance().isAuthenticated()) {
                    reviewsSection.getChildren().add(buildReviewForm());
                }
                if (reviews == null || reviews.isEmpty()) {
                    Label lbl = new Label("No hay reseñas aún. ¡Sé el primero!");
                    lbl.setTextFill(Color.GRAY);
                    reviewsSection.getChildren().add(lbl);
                    return;
                }
                reviews.forEach(r -> reviewsSection.getChildren().add(buildReviewCard(r)));
            });
        });
    }

    private VBox buildReviewForm() {
        VBox form = new VBox(10);
        form.setStyle("-fx-background-color: #1e1e1e; -fx-padding: 15; -fx-background-radius: 8;");
        
        TextField titleIn = new TextField(); 
        titleIn.setPromptText("Título de la reseña");
        titleIn.setStyle("-fx-control-inner-background: #2a2a2a; -fx-text-fill: white; -fx-prompt-text-fill: #888888;");
        
        ComboBox<Integer> rateIn = new ComboBox<>(); 
        rateIn.getItems().addAll(1, 2, 3, 4, 5);
        rateIn.setPromptText("Calificación");
        
        TextArea contIn = new TextArea(); 
        contIn.setPromptText("Tu reseña..."); 
        contIn.setPrefRowCount(3);
        contIn.setStyle("-fx-control-inner-background: #2a2a2a; -fx-text-fill: white; -fx-prompt-text-fill: #888888;");
        
        Button btn = new Button("Publicar");
        btn.setStyle("-fx-background-color: #e50914; -fx-text-fill: white; -fx-cursor: hand;");
        
        btn.setOnAction(e -> {
            if (rateIn.getValue() == null || titleIn.getText().isBlank() || contIn.getText().isBlank()) return;
            ReviewRequest req = new ReviewRequest(currentTvmazeId, rateIn.getValue(), titleIn.getText().trim(), contIn.getText().trim());
            reviewService.createReview(req).thenRun(() -> Platform.runLater(this::cargarResenas));
        });
        form.getChildren().addAll(new Label("Escribe una reseña:"), titleIn, rateIn, contIn, btn);
        return form;
    }

    private VBox buildReviewCard(ReviewDto review) {
        String rId = review.getSafeId();
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: #151515; -fx-padding: 15; -fx-background-radius: 8; -fx-border-color: #333;");

        Label author = new Label("@" + obtenerNombreAutor(review.getOwnerId()));
        author.setTextFill(Color.web(AppConstants.COLOR_ACCENT));
        
        Label title = new Label(review.title() != null ? review.title() : "Sin título");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        title.setTextFill(Color.WHITE);

        Label content = new Label(review.content() != null ? review.content() : "");
        content.setTextFill(Color.LIGHTGRAY);
        content.setWrapText(true);

        HBox actions = new HBox(15);
        actions.setAlignment(Pos.CENTER_LEFT);
        
        Button likeBtn = new Button("Me gusta (" + review.getLikesCount() + ")");
        likeBtn.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 4;");
        likeBtn.setOnAction(e -> {
            if (!rId.isEmpty()) reviewService.toggleReviewLike(rId).thenRun(() -> Platform.runLater(this::cargarResenas));
        });

        VBox commContainer = new VBox(10); 
        commContainer.setManaged(false); commContainer.setVisible(false);
        
        Button commBtn = new Button("Comentarios (" + review.getCommentsCount() + ")");
        commBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #aaaaaa; -fx-cursor: hand; -fx-underline: true;");
        commBtn.setOnAction(e -> {
            boolean show = !commContainer.isVisible();
            commContainer.setVisible(show);
            commContainer.setManaged(show);
            if (show && !rId.isEmpty()) cargarComentariosUI(rId, commContainer);
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
            in.setStyle("-fx-control-inner-background: #2a2a2a; -fx-text-fill: white; -fx-prompt-text-fill: #888888;");
            
            Button b = new Button("Enviar");
            b.setStyle("-fx-background-color: #555; -fx-text-fill: white; -fx-cursor: hand;");
            
            b.setOnAction(e -> {
                if (in.getText().isBlank()) return;
                reviewService.createComment(rId, new CommentRequest(in.getText().trim()))
                        .thenRun(() -> Platform.runLater(() -> cargarComentariosUI(rId, container)));
            });
            container.getChildren().add(new HBox(10, in, b));
        }
        
        reviewService.getReviewComments(rId).thenAccept(list -> Platform.runLater(() -> {
            if (list != null) list.forEach(c -> container.getChildren().add(buildCommentItem(c, rId, container)));
        }));
    }

    private VBox buildCommentItem(CommentDto c, String rId, VBox parent) {
        String cId = c.getSafeId();
        VBox box = new VBox(5); 
        box.setStyle("-fx-background-color: #1a1a1a; -fx-padding: 8; -fx-background-radius: 5;");
        
        Label user = new Label("@" + obtenerNombreAutor(c.getOwnerId()));
        user.setTextFill(Color.GRAY);
        
        Label txt = new Label(c.content() != null ? c.content() : ""); 
        txt.setTextFill(Color.WHITE);
        
        HBox actions = new HBox(10);
        Button lk = new Button("Me gusta (" + c.getLikesCount() + ")");
        lk.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 4;");
        lk.setOnAction(e -> {
            if (!cId.isEmpty()) reviewService.toggleCommentLike(cId).thenRun(() -> Platform.runLater(() -> cargarComentariosUI(rId, parent)));
        });
        
        actions.getChildren().add(lk);
        injectCommentDelete(cId, c.getOwnerId(), actions, rId, parent);
        
        box.getChildren().addAll(user, txt, actions);
        return box;
    }

    private String obtenerNombreAutor(String ownerId) {
        if (!SessionManager.getInstance().isAuthenticated() || ownerId == null || ownerId.isEmpty()) return "Usuario";
        String currId = SessionManager.getInstance().getCurrentUser().id();
        String currAuth = SessionManager.getInstance().getCurrentUser().authId();
        if (ownerId.equals(currId) || ownerId.equals(currAuth)) {
            return SessionManager.getInstance().getCurrentUser().username();
        }
        return "Usuario";
    }

    private void injectOwnerActions(String rId, String ownerId, HBox actions) {
        if (!SessionManager.getInstance().isAuthenticated() || ownerId == null || ownerId.isEmpty()) return;
        String currId = SessionManager.getInstance().getCurrentUser().id();
        String currAuth = SessionManager.getInstance().getCurrentUser().authId();
        
        if (ownerId.equals(currId) || ownerId.equals(currAuth)) {
            Button del = new Button("Eliminar");
            del.setStyle("-fx-background-color: transparent; -fx-text-fill: #e50914; -fx-cursor: hand; -fx-underline: true;");
            del.setOnAction(e -> reviewService.deleteReview(rId).thenRun(() -> Platform.runLater(this::cargarResenas)));
            actions.getChildren().add(del);
        }
    }

    private void injectCommentDelete(String cId, String ownerId, HBox actions, String rId, VBox parent) {
        if (!SessionManager.getInstance().isAuthenticated() || ownerId == null || ownerId.isEmpty()) return;
        String currId = SessionManager.getInstance().getCurrentUser().id();
        String currAuth = SessionManager.getInstance().getCurrentUser().authId();
        
        if (ownerId.equals(currId) || ownerId.equals(currAuth)) {
            Button del = new Button("Eliminar");
            del.setStyle("-fx-background-color: transparent; -fx-text-fill: #e50914; -fx-cursor: hand; -fx-underline: true;");
            del.setOnAction(e -> reviewService.deleteComment(cId).thenRun(() -> Platform.runLater(() -> cargarComentariosUI(rId, parent))));
            actions.getChildren().add(del);
        }
    }

    private void injectShowCard(Show s, HBox container) {
        try {
            FXMLLoader l = new FXMLLoader(getClass().getResource(AppConstants.FXML_SHOW_CARD));
            VBox card = l.load();
            ((ShowCardController)l.getController()).setData(s);
            container.getChildren().add(card);
        } catch (IOException e) { System.err.println("Error card: " + s.name()); }
    }

    private VBox buildPersonBox(CastDto m) {
        VBox b = new VBox(5); b.setAlignment(Pos.TOP_CENTER); b.setPrefWidth(120);
        ImageView iv = new ImageView(); iv.setFitHeight(150); iv.setFitWidth(110);
        if (m.person().image() != null) iv.setImage(new Image(m.person().image().medium(), true));
        Label n = new Label(m.person().name()); n.setTextFill(Color.WHITE); n.setWrapText(true);
        b.getChildren().addAll(iv, n);
        return b;
    }

    private VBox createSeasonAccordion(SeasonDto s) {
        VBox c = new VBox();
        HBox h = new HBox(new Label("Temp " + s.number()));
        h.setStyle("-fx-background-color: #1e1e1e; -fx-padding: 10; -fx-cursor: hand; -fx-text-fill: white;");
        VBox eps = new VBox(); eps.setVisible(false); eps.setManaged(false);
        h.setOnMouseClicked(e -> {
            eps.setVisible(!eps.isVisible()); eps.setManaged(eps.isVisible());
            if (eps.isVisible()) renderEpisodes(s.number(), eps);
        });
        c.getChildren().addAll(h, eps);
        return c;
    }

    private void renderEpisodes(int num, VBox container) {
        container.getChildren().clear();
        List<EpisodeDto> list = seasonEpisodesMap.get(num);
        if (list != null) {
            list.forEach(ep -> {
                Label l = new Label("E" + ep.number() + " - " + ep.name());
                l.setTextFill(Color.LIGHTGRAY);
                container.getChildren().add(l);
            });
        }
    }

    private void moverCarruselDinamico(ScrollPane sp, int dir) {
        double val = sp.getHvalue() + (dir * 0.2);
        sp.setHvalue(Math.max(0, Math.min(val, 1)));
    }
}