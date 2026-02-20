package com.shopapp.ui;

import com.shopapp.Main;
import com.shopapp.database.UserDAO;
import com.shopapp.models.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Objects;
import java.util.Optional;

public class ManagerLoginView extends VBox {

    private final UserDAO userDAO;
    private final Stage stage;

    public ManagerLoginView(Stage stage) {
        this.stage = stage;
        this.userDAO = new UserDAO();
        
        setAlignment(Pos.CENTER);
        setSpacing(20);
        setPadding(new Insets(40));
        setStyle("-fx-background-color: #ffffff;");

        // Logo / Title
        Label lblTitle = new Label("Manager Portal");
        lblTitle.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #0284c7;");
        
        Label lblSubtitle = new Label("Admin & Manager Access");
        lblSubtitle.setStyle("-fx-text-fill: #64748b; -fx-font-size: 16px;");

        // Form
        VBox form = new VBox(15);
        form.setMaxWidth(350);
        form.setAlignment(Pos.CENTER_LEFT);

        Label lblUser = new Label("Username");
        TextField txtUser = new TextField();
        txtUser.setPromptText("Enter manager username");
        txtUser.getStyleClass().add("text-field");
        
        Label lblPass = new Label("Password");
        
        // Password field with visibility toggle
        PasswordField txtPass = new PasswordField();
        txtPass.setPromptText("Enter password");
        txtPass.getStyleClass().add("text-field");

        // Focus transition on Enter
        txtUser.setOnAction(e -> {
            if (!txtUser.getText().isEmpty()) {
                txtPass.requestFocus();
            }
        });
        
        TextField txtPassVisible = new TextField();
        txtPassVisible.setPromptText("Enter password");
        txtPassVisible.getStyleClass().add("text-field");
        txtPassVisible.setVisible(false);
        txtPassVisible.setManaged(false);
        // Bind text properties
        txtPass.textProperty().bindBidirectional(txtPassVisible.textProperty());
        
        Button btnTogglePassword = new Button("👁");
        btnTogglePassword.setStyle("-fx-font-size: 16px; -fx-padding: 8 12; -fx-cursor: hand;");
        btnTogglePassword.setOnAction(e -> {
            if (txtPass.isVisible()) {
                txtPass.setVisible(false);
                txtPass.setManaged(false);
                txtPassVisible.setVisible(true);
                txtPassVisible.setManaged(true);
                btnTogglePassword.setText("🙈");
            } else {
                txtPass.setVisible(true);
                txtPass.setManaged(true);
                txtPassVisible.setVisible(false);
                txtPassVisible.setManaged(false);
                btnTogglePassword.setText("👁");
            }
        });
        
        HBox passwordBox = new HBox(8, txtPass, txtPassVisible, btnTogglePassword);
        passwordBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(txtPass, Priority.ALWAYS);
        HBox.setHgrow(txtPassVisible, Priority.ALWAYS);

        Button btnLogin = new Button("Login as Manager");
        btnLogin.getStyleClass().add("button-primary");
        btnLogin.setMaxWidth(Double.MAX_VALUE);
        btnLogin.setStyle("-fx-font-size: 14px; -fx-padding: 10 20;");
        
        // Map enter on password fields to login
        txtPass.setOnAction(e -> btnLogin.fire());
        txtPassVisible.setOnAction(e -> btnLogin.fire());
        
        Button btnBack = new Button("← Back to Role Selection");
        btnBack.getStyleClass().add("nav-button");
        btnBack.setMaxWidth(Double.MAX_VALUE);
        btnBack.setOnAction(e -> showRoleSelection());
        
        Label lblError = new Label();
        lblError.setStyle("-fx-text-fill: #ef4444;");
        lblError.setVisible(false);

        // Login Logic
        btnLogin.setOnAction(e -> {
            String u = txtUser.getText();
            String p = txtPass.getText();
            
            if (u.isEmpty() || p.isEmpty()) {
                lblError.setText("Please enter both username and password.");
                lblError.setVisible(true);
                return;
            }

            Optional<User> user = userDAO.authenticate(u, p);
            if (user.isPresent()) {
                String role = user.get().getRole();
                // Only allow ADMIN or MANAGER roles
                if ("ADMIN".equalsIgnoreCase(role) || "MANAGER".equalsIgnoreCase(role)) {
                    launchMainApp(user.get());
                } else {
                    lblError.setText("Access denied. This is for managers only.");
                    lblError.setVisible(true);
                }
            } else {
                lblError.setText("Invalid credentials.");
                lblError.setVisible(true);
            }
        });

        form.getChildren().addAll(lblUser, txtUser, lblPass, passwordBox, lblError, btnLogin, btnBack);
        getChildren().addAll(lblTitle, lblSubtitle, form);
    }

    private void showRoleSelection() {
        RoleSelectionView roleSelection = new RoleSelectionView(stage);
        Scene scene = new Scene(roleSelection, 800, 600);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles.css")).toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Shop Management System - Role Selection");
    }

    private void launchMainApp(User user) {
        MainLayout mainRoot = new MainLayout(user);
        
        // Use current stage dimensions to avoid sudden size changes
        double width = stage.isMaximized() ? stage.getWidth() : 1280;
        double height = stage.isMaximized() ? stage.getHeight() : 800;
        Scene scene = new Scene(mainRoot, width, height);
        
        scene.getStylesheets().add(Objects.requireNonNull(Main.class.getResource("/styles.css")).toExternalForm());
        
        stage.setScene(scene);
        stage.setTitle("Shop Management System - " + user.getRole());
        
        javafx.application.Platform.runLater(() -> {
            stage.setMaximized(true);
            stage.setIconified(false);
            stage.toFront();
            stage.requestFocus();
        });
    }
}
