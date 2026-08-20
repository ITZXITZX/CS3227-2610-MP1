package com.example.habitzone.ui;

import com.example.habitzone.usecase.HabitHistory;
import com.example.habitzone.usecase.HabitSnapshot;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.function.Supplier;

/** Main JavaFX layout; it renders controller state and forwards input unchanged. */
public final class HabitZoneView extends BorderPane {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("EEEE, d MMMM uuuu");
    private final MainWindowController controller;
    private final ListView<HabitSnapshot> habitList = new ListView<>();
    private final ListView<String> historyList = new ListView<>();
    private final Label feedback = new Label();
    private final TextField commandInput = new TextField();

    public HabitZoneView(MainWindowController controller) {
        this(controller, () -> LocalDate.now(Clock.systemDefaultZone()));
    }

    public HabitZoneView(MainWindowController controller, Supplier<LocalDate> dateSupplier) {
        this.controller = controller;
        getStyleClass().add("app-root");
        setTop(createTopBar(dateSupplier.get()));
        setCenter(createMainArea());
        setBottom(createCommandArea());
        refresh();
    }

    private HBox createTopBar(LocalDate today) {
        Label title = new Label("HabitZone");
        title.getStyleClass().add("app-title");
        Label date = new Label(today.format(DATE_FORMAT));
        date.getStyleClass().add("current-date");
        HBox topBar = new HBox(title, date);
        topBar.getStyleClass().add("top-bar");
        topBar.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(date, Priority.ALWAYS);
        date.setAlignment(Pos.CENTER_RIGHT);
        return topBar;
    }

    private HBox createMainArea() {
        VBox habits = panel("Your habits", habitList);
        VBox history = panel("Selected habit history", historyList);
        HBox mainArea = new HBox(habits, history);
        mainArea.getStyleClass().add("main-area");
        HBox.setHgrow(habits, Priority.ALWAYS);
        HBox.setHgrow(history, Priority.ALWAYS);
        return mainArea;
    }

    private VBox panel(String heading, ListView<?> content) {
        Label label = new Label(heading);
        label.getStyleClass().add("panel-heading");
        VBox panel = new VBox(12, label, content);
        panel.getStyleClass().add("panel");
        VBox.setVgrow(content, Priority.ALWAYS);
        return panel;
    }

    private VBox createCommandArea() {
        feedback.getStyleClass().add("feedback");
        commandInput.setPromptText("Enter a command, e.g. help or list");
        commandInput.getStyleClass().add("command-input");
        commandInput.setOnAction(event -> submitCommand());
        VBox bottom = new VBox(10, feedback, commandInput);
        bottom.getStyleClass().add("command-area");
        return bottom;
    }

    private void submitCommand() {
        controller.submit(commandInput.getText());
        commandInput.clear();
        refresh();
    }

    private void refresh() {
        habitList.getItems().setAll(controller.habits());
        habitList.setCellFactory(list -> new javafx.scene.control.ListCell<>() {
            @Override protected void updateItem(HabitSnapshot habit, boolean empty) {
                super.updateItem(habit, empty);
                setText(empty || habit == null ? null : habit.name());
            }
        });
        historyList.getItems().setAll(controller.history().map(this::historyLines).orElseGet(java.util.List::of));
        feedback.setText(controller.feedback());
        feedback.pseudoClassStateChanged(javafx.css.PseudoClass.getPseudoClass("error"), controller.feedbackIsError());
    }

    private java.util.List<String> historyLines(HabitHistory history) {
        return history.completions().stream().map(completion -> completion.date().toString()).toList();
    }
}
