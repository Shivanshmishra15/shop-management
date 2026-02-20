package com.shopapp.ui;

import com.shopapp.database.ProductDAO;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import java.util.function.Consumer;

public class DashboardView extends VBox {

    private final ProductDAO productDAO;
    private final Consumer<String> navigationCallback;

    public DashboardView(Consumer<String> navigationCallback) {
        this.navigationCallback = navigationCallback;
        this.productDAO = new ProductDAO();
        setSpacing(25);
        setPadding(new Insets(10));

        // Header
        Label title = new Label("Dashboard");
        title.getStyleClass().add("page-title");

        // Stats Grid
        HBox statsContainer = new HBox(20);
        statsContainer.getChildren().addAll(
                createStatCard("Total Products", "124", "blue", "INVENTORY"),
                createStatCard("Low Stock Alerts", "5", "red", "INVENTORY_LOW"),
                createStatCard("Inventory Value", "$12,450", "green", "REPORTS")
        );

        // Recent Activity Section
        VBox activitySection = new VBox(15);
        activitySection.getStyleClass().add("card");
        Label activityTitle = new Label("Recent Activity");
        activityTitle.getStyleClass().add("card-title");
        
        Label activityPlaceholder = new Label("• New Product 'Wireless Mouse' added\n• Invoice #1024 created\n• Stock updated for 'HDMI Cable'");
        activityPlaceholder.getStyleClass().add("activity-text");
        
        VBox.setVgrow(activitySection, Priority.ALWAYS);
        activitySection.getChildren().addAll(activityTitle, activityPlaceholder);

        getChildren().addAll(title, statsContainer, activitySection);
    }

    private VBox createStatCard(String title, String value, String colorTheme, String targetView) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        HBox.setHgrow(card, Priority.ALWAYS);
        card.setMinWidth(200);
        
        // Interaction
        card.setCursor(Cursor.HAND);
        card.setOnMouseClicked(e -> navigationCallback.accept(targetView));
        card.getStyleClass().add("stat-card");

        Label lblTitle = new Label(title);
        lblTitle.getStyleClass().add("stat-label");

        Label lblValue = new Label(value);
        lblValue.getStyleClass().add("stat-value");

        Region indicator = new Region();
        indicator.setMaxHeight(4);
        indicator.setMinHeight(4);
        String colorCode = switch (colorTheme) {
            case "red" -> "#ef4444";
            case "green" -> "#10b981";
            default -> "#0ea5e9";
        };
        indicator.setStyle("-fx-background-color: " + colorCode + "; -fx-background-radius: 2;");

        card.getChildren().addAll(lblTitle, lblValue, indicator);
        return card;
    }
}
