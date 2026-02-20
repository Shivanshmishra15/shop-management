package com.shopapp.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Objects;

public class RoleSelectionView extends VBox {

    private final Stage stage;

    public RoleSelectionView(Stage stage) {
        this.stage = stage;
        
        setAlignment(Pos.CENTER);
        setSpacing(30);
        setPadding(new Insets(60));
        setStyle("-fx-background-color: #ffffff;");

        // Title
        Label lblTitle = new Label("Shop Management System");
        lblTitle.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: #0284c7;");
        
        Label lblSubtitle = new Label("Select Your Role");
        lblSubtitle.setStyle("-fx-text-fill: #64748b; -fx-font-size: 18px; -fx-padding: 0 0 20 0;");

        // Manager Login Button
        Button btnManager = new Button("Manager Login");
        btnManager.getStyleClass().add("button-primary");
        btnManager.setPrefWidth(300);
        btnManager.setPrefHeight(60);
        btnManager.setStyle("-fx-font-size: 18px;");
        btnManager.setOnAction(e -> showManagerLogin());

        // Employee Login Button
        Button btnEmployee = new Button("Employee Login");
        btnEmployee.getStyleClass().add("button-primary");
        btnEmployee.setPrefWidth(300);
        btnEmployee.setPrefHeight(60);
        btnEmployee.setStyle("-fx-font-size: 18px;");
        btnEmployee.setOnAction(e -> showEmployeeLogin());

        // Info labels
        Label lblManagerInfo = new Label("For Admin & Manager roles");
        lblManagerInfo.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13px;");
        
        Label lblEmployeeInfo = new Label("For Cashier role");
        lblEmployeeInfo.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13px;");

        VBox managerBox = new VBox(8, btnManager, lblManagerInfo);
        managerBox.setAlignment(Pos.CENTER);
        
        VBox employeeBox = new VBox(8, btnEmployee, lblEmployeeInfo);
        employeeBox.setAlignment(Pos.CENTER);

        getChildren().addAll(lblTitle, lblSubtitle, managerBox, employeeBox);
    }

    private void showManagerLogin() {
        ManagerLoginView managerLogin = new ManagerLoginView(stage);
        Scene scene = new Scene(managerLogin, 800, 600);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles.css")).toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Shop Management System - Manager Login");
        javafx.application.Platform.runLater(() -> {
            stage.setMaximized(true);
            stage.setIconified(false);
        });
    }

    private void showEmployeeLogin() {
        EmployeeLoginView employeeLogin = new EmployeeLoginView(stage);
        Scene scene = new Scene(employeeLogin, 800, 600);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles.css")).toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Shop Management System - Employee Login");
        javafx.application.Platform.runLater(() -> {
            stage.setMaximized(true);
            stage.setIconified(false);
        });
    }
}
