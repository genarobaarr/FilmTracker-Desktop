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
            // Protección de inicialización
        }
    }

    @FXML
    private void handleFilterChange() {
        loadUsers();
        loadReviews();
        loadComments();
    }

    private void loadUsers() {
        if (usersContainer == null) { 
            return; 
        }
        
        usersContainer.getChildren().clear();
        String period = periodMap.get(periodComboBox.getValue());
        
        leaderboardService.getTopUsers(period).thenAccept(res -> {
            Platform.runLater(() -> {
                if (res != null) {
                    if (res.top() != null) {
                        res.top().forEach(this::renderUserRank);
                    }
                }
            });
        }).exceptionally(e -> null);
    }

    private void renderUserRank(UserRankDto rank) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: #1e1e1e; -fx-padding: 15; -fx-background-radius: 8; -fx-border-color: #333; -fx-cursor: hand;");

        Label rankLbl = new Label("#" + rank.rank());
        rankLbl.setTextFill(Color.web(AppConstants.COLOR_ACCENT));
        rankLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 20px;");
        rankLbl.setMinWidth(45);

        VBox contentBox = new VBox(5);
        HBox.setHgrow(contentBox, Priority.ALWAYS);
        
        Label titleLbl = new Label("Cargando usuario...");
        titleLbl.setTextFill(Color.WHITE);
        titleLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 15px;");
        
        if (rank.authId() != null) {
            userService.getUserById(rank.authId()).thenAccept(user -> {
                Platform.runLater(() -> {
                    if (user != null) {
                        if (user.username() != null) {
                            titleLbl.setText(user.name() + " (@" + user.username() + ")");
                        }
                        
                        row.setOnMouseClicked(e -> {
                            App.showProfileView(user);
                        });
                    }
                });
            }).exceptionally(e -> {
                return null;
            });
        }

        Label metaLbl = new Label("Total Likes: " + rank.totalLikes() + " (Reseñas: " + rank.reviewLikes() + ", Comentarios: " + rank.commentLikes() + ")");
        metaLbl.setTextFill(Color.GRAY);
        metaLbl.setStyle("-fx-font-size: 12px;");

        contentBox.getChildren().add(titleLbl);
        contentBox.getChildren().add(metaLbl);
        
        row.getChildren().add(rankLbl);
        row.getChildren().add(contentBox);
        
        usersContainer.getChildren().add(row);
    }

    private void loadReviews() {
        if (reviewsContainer == null) { 
            return; 
        }
        
        reviewsContainer.getChildren().clear();
        String period = periodMap.get(periodComboBox.getValue());

        leaderboardService.getTopReviews(period).thenAccept(res -> {
            Platform.runLater(() -> {
                if (res != null) {
                    if (res.top() != null) {
                        res.top().forEach(this::renderReviewRank);
                    }
                }
            });
        }).exceptionally(e -> null);
    }

    private void renderReviewRank(ReviewRankDto review) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: #1e1e1e; -fx-padding: 15; -fx-background-radius: 8; -fx-border-color: #333; -fx-cursor: hand;");

        Label rankLbl = new Label("#" + review.rank());
        rankLbl.setTextFill(Color.web(AppConstants.COLOR_ACCENT));
        rankLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 20px;");
        rankLbl.setMinWidth(45);

        VBox contentBox = new VBox(5);
        HBox.setHgrow(contentBox, Priority.ALWAYS);
        
        Label seriesLbl = new Label("Cargando serie...");
        seriesLbl.setTextFill(Color.web(AppConstants.COLOR_ACCENT));
        seriesLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

        if (review.tvmazeId() != null) {
            showService.getShowDetails(review.tvmazeId()).thenAccept(show -> {
                Platform.runLater(() -> {
                    if (show != null) {
                        if (show.name() != null) {
                            seriesLbl.setText(show.name());
                        }
                        
                        row.setOnMouseClicked(e -> {
                            App.showShowDetail(show);
                        });
                    }
                });
            }).exceptionally(e -> {
                return null;
            });
        }

        String titleStr = "Sin título";
        
        if (review.title() != null) {
            titleStr = review.title();
        }

        Label titleLbl = new Label(titleStr);
        titleLbl.setTextFill(Color.WHITE);
        titleLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 15px;");

        String contentStr = "";
        
        if (review.content() != null) {
            contentStr = review.content();
        }

        Label descLbl = new Label(contentStr);
        descLbl.setTextFill(Color.LIGHTGRAY);
        descLbl.setWrapText(true);

        Label metaLbl = new Label("@Usuario • ❤️ " + review.getSafeLikesCount() + " likes");
        metaLbl.setTextFill(Color.GRAY);
        metaLbl.setStyle("-fx-font-size: 11px;");

        if (review.authId() != null) {
            userService.getUserById(review.authId()).thenAccept(user -> {
                Platform.runLater(() -> {
                    if (user != null) {
                        if (user.username() != null) {
                            metaLbl.setText("@" + user.username() + " • ❤️ " + review.getSafeLikesCount() + " likes");
                        }
                    }
                });
            }).exceptionally(e -> {
                return null;
            });
        }

        contentBox.getChildren().add(seriesLbl);
        contentBox.getChildren().add(titleLbl);
        contentBox.getChildren().add(descLbl);
        contentBox.getChildren().add(metaLbl);
        
        row.getChildren().add(rankLbl);
        row.getChildren().add(contentBox);
        
        reviewsContainer.getChildren().add(row);
    }

    private void loadComments() {
        if (commentsContainer == null) { 
            return; 
        }
        
        commentsContainer.getChildren().clear();
        String period = periodMap.get(periodComboBox.getValue());

        leaderboardService.getTopComments(period).thenAccept(res -> {
            Platform.runLater(() -> {
                if (res != null) {
                    if (res.top() != null) {
                        res.top().forEach(this::renderCommentRank);
                    }
                }
            });
        }).exceptionally(e -> null);
    }

    private void renderCommentRank(CommentRankDto comment) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: #1e1e1e; -fx-padding: 15; -fx-background-radius: 8; -fx-border-color: #333;");

        Label rankLbl = new Label("#" + comment.rank());
        rankLbl.setTextFill(Color.web(AppConstants.COLOR_ACCENT));
        rankLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 20px;");
        rankLbl.setMinWidth(45);

        VBox contentBox = new VBox(5);
        HBox.setHgrow(contentBox, Priority.ALWAYS);
        
        Label authorLbl = new Label("Cargando usuario...");
        authorLbl.setTextFill(Color.WHITE);
        authorLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        if (comment.authId() != null) {
            userService.getUserById(comment.authId()).thenAccept(user -> {
                Platform.runLater(() -> {
                    if (user != null) {
                        if (user.username() != null) {
                            authorLbl.setText("@" + user.username());
                        }
                    }
                });
            }).exceptionally(e -> null);
        }

        String contentStr = "";
        
        if (comment.content() != null) {
            contentStr = comment.content();
        }

        Label descLbl = new Label(contentStr);
        descLbl.setTextFill(Color.LIGHTGRAY);
        descLbl.setWrapText(true);

        Label metaLbl = new Label("❤️ " + comment.getSafeLikesCount() + " likes");
        metaLbl.setTextFill(Color.web(AppConstants.COLOR_ACCENT));
        metaLbl.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");

        contentBox.getChildren().add(authorLbl);
        contentBox.getChildren().add(descLbl);
        contentBox.getChildren().add(metaLbl);
        
        row.getChildren().add(rankLbl);
        row.getChildren().add(contentBox);
        
        commentsContainer.getChildren().add(row);
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
}