package com.shopapp;

import java.util.Objects;

import com.shopapp.database.DatabaseManager;
import com.shopapp.ui.RoleSelectionView;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Initialize Database
        DatabaseManager.init();

        // Start with Role Selection Screen
        RoleSelectionView roleSelectionView = new RoleSelectionView(primaryStage);
        
        Scene scene = new Scene(roleSelectionView, 800, 600);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles.css")).toExternalForm());

        // Configure Stage
        primaryStage.setTitle("Shop Management System - Role Selection");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.show();
    }

    @Override
    public void stop() {
        System.out.println("Application shutting down...");
        DatabaseManager.shutdown();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
