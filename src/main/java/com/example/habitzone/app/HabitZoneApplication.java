package com.example.habitzone.app;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class HabitZoneApplication extends Application {
    @Override
    public void start(Stage stage) {
        Label title = new Label("HabitZone");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label placeholder = new Label("Command-driven habit tracking UI coming soon.");
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(24));
        root.setTop(title);
        root.setCenter(placeholder);

        stage.setTitle("HabitZone");
        stage.setScene(new Scene(root, 480, 320));
        stage.show();
    }
}
