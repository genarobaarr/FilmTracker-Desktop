package com.src.filmtracker.controllers.leaderboards;

import com.src.filmtracker.App;
import com.src.filmtracker.models.leaderboards.UserRankDto;
import com.src.filmtracker.models.leaderboards.ReviewRankDto;
import com.src.filmtracker.models.leaderboards.CommentRankDto;
import com.src.filmtracker.services.leaderboards.ILeaderboardService;
import com.src.filmtracker.services.leaderboards.LeaderboardService;
import com.src.filmtracker.services.shows.IShowService;
import com.src.filmtracker.services.shows.ShowService;
import com.src.filmtracker.services.users.IUserService;
import com.src.filmtracker.services.users.UserService;
import com.src.filmtracker.utils.AppConstants;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.LinkedHashMap;
import java.util.Map;

public class LeaderboardsController {

    private static final String STYLE_ROW_INTERACTIVE = "-fx-background-color: #1e1e1e; -fx-padding: 15; -fx-background-radius: 8; -fx-border-color: #333; -fx-cursor: hand;";
    private static final String STYLE_ROW_STATIC = "-fx-background-color: #1e1e1e; -fx-padding: 15; -fx-background-radius: 8; -fx-border-color: #333;";
    private static final String STYLE_RANK_NUMBER = "-fx-font-weight: bold; -fx-font-size: 20px;";
    private static final String STYLE_TITLE_15 = "-fx-font-weight: bold; -fx-font-size: 15px;";
    private static final String STYLE_SERIES_TITLE = "-fx-font-weight: bold; -fx-font-size: 13px;";
    private static final String STYLE_AUTHOR_LINK = "-fx-font-weight: bold; -fx-font-size: 14px; -fx-cursor: hand; -fx-underline: true;";
    private static final String STYLE_META_12 = "-fx-font-size: 12px;";
    private static final String STYLE_META_11 = "-fx-font-size: 11px;";
    
    private static final String TEXT_LOADING_USER = "Cargando usuario...";
    private static final String TEXT_LOADING_SERIES = "Cargando serie...";
    private static final String TEXT_NO_TITLE = "Sin título";

    @FXML private ComboBox<String> periodComboBox;
    @FXML private VBox usersContainer;
    @FXML private VBox reviewsContainer;
    @FXML private VBox commentsContainer;
    @FXML private Tab tabUsers;
    @FXML private Tab tabReviews;
    @FXML private Tab tabComments;

    private final ILeaderboardService leaderboardService = new LeaderboardService();
    private final IUserService userService = new UserService();
    private final IShowService showService = new ShowService();
    private final Map<String, String> periodMap = new LinkedHashMap<>();

    @FXML
    public void initialize() {
        try {
            periodMap.put("Esta Semana", "week");
            periodMap.put("Hoy", "day");
            periodMap.put("Este Mes", "month");
            periodMap.put("Histórico", "all");

            periodComboBox.getItems().addAll(periodMap.keySet());
            periodComboBox.setValue("Esta Semana");

            tabUsers.setOnSelectionChanged(e -> {
                if (tabUsers.isSelected()) {
                    loadUsers();
                }
            });
            
            tabReviews.setOnSelectionChanged(e -> {
                if (tabReviews.isSelected()) {
                    loadReviews();
                }
            });
            
            tabComments.setOnSelectionChanged(e -> {
                if (tabComments.isSelected()) {
                    loadComments();
                }
            });

            loadUsers();
        } catch (Exception e) {
            // Ignorado intencionalmente: Falla segura al inicializar el combobox sin afectar la UI principal
        }
    }

    @FXML
    private void handleFilterChange() {
        loadUsers();
        loadReviews();
        loadComments();
    }

    @FXML 
    private void handleBack() { 
        App.setRoot(AppConstants.FXML_DASHBOARD); 
    }
    
    @FXML 
    private void handleMinimize() { 
        ((Stage) usersContainer.getScene().getWindow()).setIconified(true); 
    }
    
    @FXML 
    private void handleClose() { 
        Platform.exit(); 
        System.exit(0); 
    }

    private void loadUsers() {
        if (usersContainer == null) {
            return;
        } 
        
        usersContainer.getChildren().clear();
        String period = periodMap.get(periodComboBox.getValue());
        
        leaderboardService.getTopUsers(period).thenAccept(res -> {
            if (res != null && res.top() != null) {
                Platform.runLater(() -> res.top().forEach(this::renderUserRank));
            }
        }).exceptionally(e -> {
            App.procesarErrorCritico(e);
            return null;
        });
    }

    private void renderUserRank(UserRankDto rank) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle(STYLE_ROW_INTERACTIVE);

        Label rankLbl = new Label("#" + rank.rank());
        rankLbl.setTextFill(Color.web(AppConstants.COLOR_ACCENT));
        rankLbl.setStyle(STYLE_RANK_NUMBER);
        rankLbl.setMinWidth(45);

        VBox contentBox = new VBox(5);
        HBox.setHgrow(contentBox, Priority.ALWAYS);
        
        Label titleLbl = new Label(TEXT_LOADING_USER);
        titleLbl.setTextFill(Color.WHITE);
        titleLbl.setStyle(STYLE_TITLE_15);
        
        if (rank.authId() != null) {
            userService.getUserById(rank.authId()).thenAccept(user -> {
                if (user != null && user.username() != null) {
                    Platform.runLater(() -> {
                        titleLbl.setText(user.name() + " (@" + user.username() + ")");
                        row.setOnMouseClicked(e -> App.showProfileView(user));
                    });
                }
            }).exceptionally(e -> {
                App.procesarErrorCritico(e);
                return null;
            });
        }

        Label metaLbl = new Label("Total Likes: " + rank.totalLikes() + " (Reseñas: " + rank.reviewLikes() + ", Comentarios: " + rank.commentLikes() + ")");
        metaLbl.setTextFill(Color.GRAY);
        metaLbl.setStyle(STYLE_META_12);

        contentBox.getChildren().addAll(titleLbl, metaLbl);
        row.getChildren().addAll(rankLbl, contentBox);
        usersContainer.getChildren().add(row);
    }

    private void loadReviews() {
        if (reviewsContainer == null) {
            return;
        }
        
        reviewsContainer.getChildren().clear();
        String period = periodMap.get(periodComboBox.getValue());
        
        leaderboardService.getTopReviews(period).thenAccept(res -> {
            if (res != null && res.top() != null) {
                Platform.runLater(() -> res.top().forEach(this::renderReviewRank));
            }
        }).exceptionally(e -> {
            App.procesarErrorCritico(e);
            return null;
        });
    }

    private void renderReviewRank(ReviewRankDto review) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle(STYLE_ROW_INTERACTIVE);

        Label rankLbl = new Label("#" + review.rank());
        rankLbl.setTextFill(Color.web(AppConstants.COLOR_ACCENT));
        rankLbl.setStyle(STYLE_RANK_NUMBER);
        rankLbl.setMinWidth(45);

        VBox contentBox = new VBox(5);
        HBox.setHgrow(contentBox, Priority.ALWAYS);
        
        Label seriesLbl = new Label(TEXT_LOADING_SERIES);
        seriesLbl.setTextFill(Color.web(AppConstants.COLOR_ACCENT));
        seriesLbl.setStyle(STYLE_SERIES_TITLE);

        if (review.tvmazeId() != null) {
            showService.getShowDetails(review.tvmazeId()).thenAccept(show -> {
                if (show != null && show.name() != null) {
                    Platform.runLater(() -> {
                        seriesLbl.setText(show.name());
                        row.setOnMouseClicked(e -> App.showShowDetail(show));
                    });
                }
            }).exceptionally(e -> {
                App.procesarErrorCritico(e);
                return null;
            });
        }

        String titleStr = review.title() != null ? review.title() : TEXT_NO_TITLE;
        Label titleLbl = new Label(titleStr);
        titleLbl.setTextFill(Color.WHITE);
        titleLbl.setStyle(STYLE_TITLE_15);

        String contentStr = review.content() != null ? review.content() : "";
        Label descLbl = new Label(contentStr);
        descLbl.setTextFill(Color.LIGHTGRAY);
        descLbl.setWrapText(true);

        Label metaLbl = new Label("@Usuario • ❤️ " + review.getSafeLikesCount() + " likes");
        metaLbl.setTextFill(Color.GRAY);
        metaLbl.setStyle(STYLE_META_11);

        if (review.authId() != null) {
            userService.getUserById(review.authId()).thenAccept(user -> {
                if (user != null && user.username() != null) {
                    Platform.runLater(() -> metaLbl.setText("@" + user.username() + " • ❤️ " + review.getSafeLikesCount() + " likes"));
                }
            }).exceptionally(e -> {
                App.procesarErrorCritico(e);
                return null;
            });
        }

        contentBox.getChildren().addAll(seriesLbl, titleLbl, descLbl, metaLbl);
        row.getChildren().addAll(rankLbl, contentBox);
        reviewsContainer.getChildren().add(row);
    }

    private void loadComments() {
        if (commentsContainer == null) {
            return;
        }
        
        commentsContainer.getChildren().clear();
        String period = periodMap.get(periodComboBox.getValue());
        
        leaderboardService.getTopComments(period).thenAccept(res -> {
            if (res != null && res.top() != null) {
                Platform.runLater(() -> res.top().forEach(this::renderCommentRank));
            }
        }).exceptionally(e -> {
            App.procesarErrorCritico(e);
            return null;
        });
    }

    private void renderCommentRank(CommentRankDto comment) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle(STYLE_ROW_STATIC);

        Label rankLbl = new Label("#" + comment.rank());
        rankLbl.setTextFill(Color.web(AppConstants.COLOR_ACCENT));
        rankLbl.setStyle(STYLE_RANK_NUMBER);
        rankLbl.setMinWidth(45);

        VBox contentBox = new VBox(5);
        HBox.setHgrow(contentBox, Priority.ALWAYS);
        
        Label authorLbl = new Label(TEXT_LOADING_USER);
        authorLbl.setTextFill(Color.WHITE);
        authorLbl.setStyle(STYLE_AUTHOR_LINK);

        if (comment.authId() != null) {
            userService.getUserById(comment.authId()).thenAccept(user -> {
                if (user != null && user.username() != null) {
                    Platform.runLater(() -> {
                        authorLbl.setText("@" + user.username());
                        authorLbl.setTextFill(Color.web(AppConstants.COLOR_ACCENT));
                        authorLbl.setOnMouseClicked(e -> App.showProfileView(user));
                    });
                }
            }).exceptionally(e -> {
                App.procesarErrorCritico(e);
                return null;
            });
        }

        String contentStr = comment.content() != null ? comment.content() : "";
        Label descLbl = new Label(contentStr);
        descLbl.setTextFill(Color.LIGHTGRAY);
        descLbl.setWrapText(true);

        Label metaLbl = new Label("❤️ " + comment.getSafeLikesCount() + " likes");
        metaLbl.setTextFill(Color.web(AppConstants.COLOR_ACCENT));
        metaLbl.setStyle(STYLE_META_11 + " -fx-font-weight: bold;");

        contentBox.getChildren().addAll(authorLbl, descLbl, metaLbl);
        row.getChildren().addAll(rankLbl, contentBox);
        commentsContainer.getChildren().add(row);
    }
}