package com.shopapp.ui;

import com.shopapp.database.UserDAO;
import com.shopapp.models.User;
import com.shopapp.Main;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Objects;
import java.util.Optional;

public class LoginView extends VBox {

    private final UserDAO userDAO;
    private final Stage stage;

    public LoginView(Stage stage) {
        this.stage = stage;
        this.userDAO = new UserDAO();
        
        setAlignment(Pos.CENTER);
        setSpacing(20);
        setPadding(new Insets(40));
        setStyle("-fx-background-color: #ffffff;"); // Light theme background

        // Logo / Title
        Label lblTitle = new Label("Shop Manager");
        lblTitle.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #38bdf8;");
        
        Label lblSubtitle = new Label("Sign in to continue");
        lblSubtitle.setStyle("-fx-text-fill: #64748b; -fx-font-size: 16px;");

        // Form
        VBox form = new VBox(15);
        form.setMaxWidth(350);
        form.setAlignment(Pos.CENTER_LEFT);

        Label lblUser = new Label("Username");
        lblUser.setStyle("-fx-text-fill: #1e293b; -fx-font-size: 14px;");
        TextField txtUser = new TextField();
        txtUser.setPromptText("Enter username (e.g., admin)");
        txtUser.getStyleClass().add("text-field");

        Label lblPass = new Label("Password");
        lblPass.setStyle("-fx-text-fill: #1e293b; -fx-font-size: 14px;");
        PasswordField txtPass = new PasswordField();
        txtPass.setPromptText("Enter password");
        txtPass.getStyleClass().add("text-field");

        Button btnLogin = new Button("Login");
        btnLogin.getStyleClass().add("button-primary");
        btnLogin.setMaxWidth(Double.MAX_VALUE);
        btnLogin.setStyle("-fx-font-size: 14px; -fx-padding: 10 20;");
        
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
                launchMainApp(user.get());
            } else {
                lblError.setText("Invalid credentials.");
                lblError.setVisible(true);
            }
        });

        form.getChildren().addAll(lblUser, txtUser, lblPass, txtPass, lblError, btnLogin);
        getChildren().addAll(lblTitle, lblSubtitle, form);
    }

    private void launchMainApp(User user) {
        // Transition to Main App
        MainLayout mainRoot = new MainLayout(user); // Pass user to layout
        Scene scene = new Scene(mainRoot, 1280, 800);
        scene.getStylesheets().add(Objects.requireNonNull(Main.class.getResource("/styles.css")).toExternalForm());
        
        stage.setScene(scene);
        stage.centerOnScreen();
    }
}
