package com.example.habitzone.ui;

import com.example.habitzone.usecase.HabitSnapshot;
import javafx.geometry.Pos;
import javafx.css.PseudoClass;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Main JavaFX layout; it renders controller state and forwards input unchanged. */
public final class HabitZoneView extends BorderPane {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("EEEE, d MMMM uuuu");
    private static final String COMPLETED_TODAY_LABEL = " (done)";
    private static final String NOT_COMPLETED_TODAY_LABEL = " (undone)";
    private static final PseudoClass SELECTED_PANEL = PseudoClass.getPseudoClass("selected");
    private final MainWindowController controller;
    private final ListView<HabitSnapshot> habitList = new ListView<>();
    private final HabitHistoryCalendar historyCalendar;
    private final Label feedback = new Label();
    private final TextField commandInput = new TextField();
    private VBox habitPanel;
    private VBox historyPanel;
    private VBox commandPanel;
    private final List<String> commandHistory = new ArrayList<>();
    private int commandHistoryIndex;
    private UpperPanel lastFocusedUpperPanel = UpperPanel.LEFT;

    private enum UpperPanel { LEFT, RIGHT }

    public HabitZoneView(MainWindowController controller) {
        this(controller, () -> LocalDate.now(Clock.systemDefaultZone()));
    }

    public HabitZoneView(MainWindowController controller, Supplier<LocalDate> dateSupplier) {
        this.controller = controller;
        this.historyCalendar = new HabitHistoryCalendar(dateSupplier.get());
        getStyleClass().add("app-root");
        setTop(createTopBar(historyCalendar.today()));
        setCenter(createMainArea());
        setBottom(createCommandArea());
        installPanelFocusNavigation();
        habitList.getSelectionModel().selectedItemProperty().addListener(
                (observable, previousHabit, selectedHabit) -> showHabitHistory(selectedHabit));
        habitList.setCellFactory(list -> new javafx.scene.control.ListCell<>() {
            @Override protected void updateItem(HabitSnapshot habit, boolean empty) {
                super.updateItem(habit, empty);
                setText(empty || habit == null ? null : habit.name()
                        + (habit.completedToday() ? COMPLETED_TODAY_LABEL : NOT_COMPLETED_TODAY_LABEL));
            }
        });
        refresh();
    }

    private void installPanelFocusNavigation() {
        addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (!event.isShiftDown()) {
                return;
            }

            boolean focusChanged = switch (event.getCode()) {
                case LEFT -> focusUpperPanel(UpperPanel.LEFT);
                case RIGHT -> focusUpperPanel(UpperPanel.RIGHT);
                case DOWN -> focusCommandPanel();
                case UP -> focusUpperPanel(lastFocusedUpperPanel);
                default -> false;
            };
            if (focusChanged) {
                event.consume();
            }
        });
        habitList.focusedProperty().addListener((observable, wasFocused, isFocused) -> {
            if (isFocused) {
                lastFocusedUpperPanel = UpperPanel.LEFT;
                selectPanel(habitPanel);
            }
        });
        historyCalendar.focusedProperty().addListener((observable, wasFocused, isFocused) -> {
            if (isFocused) {
                lastFocusedUpperPanel = UpperPanel.RIGHT;
                selectPanel(historyPanel);
            }
        });
        commandInput.focusedProperty().addListener((observable, wasFocused, isFocused) -> {
            if (isFocused) {
                selectPanel(commandPanel);
            }
        });
    }

    private void selectPanel(VBox selectedPanel) {
        habitPanel.pseudoClassStateChanged(SELECTED_PANEL, selectedPanel == habitPanel);
        historyPanel.pseudoClassStateChanged(SELECTED_PANEL, selectedPanel == historyPanel);
        commandPanel.pseudoClassStateChanged(SELECTED_PANEL, selectedPanel == commandPanel);
    }

    private boolean focusUpperPanel(UpperPanel panel) {
        lastFocusedUpperPanel = panel;
        if (panel == UpperPanel.LEFT) {
            habitList.requestFocus();
        } else {
            historyCalendar.requestFocus();
        }
        return true;
    }

    private boolean focusCommandPanel() {
        commandInput.requestFocus();
        return true;
    }

    private HBox createTopBar(LocalDate today) {
        Label title = new Label("HabitZone");
        title.getStyleClass().add("app-title");
        Label date = new Label(today.format(DATE_FORMAT));
        date.getStyleClass().add("current-date");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topBar = new HBox(title, spacer, date);
        topBar.getStyleClass().add("top-bar");
        topBar.setAlignment(Pos.CENTER_LEFT);
        date.setAlignment(Pos.CENTER);
        return topBar;
    }

    private HBox createMainArea() {
        habitPanel = panel("Your habits", habitList);
        historyPanel = panel("Selected habit history", historyCalendar);
        HBox mainArea = new HBox(habitPanel, historyPanel);
        mainArea.getStyleClass().add("main-area");
        HBox.setHgrow(habitPanel, Priority.ALWAYS);
        HBox.setHgrow(historyPanel, Priority.ALWAYS);
        return mainArea;
    }

    private VBox panel(String heading, Region content) {
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
        commandInput.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.UP) {
                showPreviousCommand();
                event.consume();
            } else if (event.getCode() == KeyCode.DOWN) {
                showNextCommand();
                event.consume();
            }
        });
        commandPanel = new VBox(10, feedback, commandInput);
        commandPanel.getStyleClass().add("command-area");
        return commandPanel;
    }

    /** Gives the user immediate access to command entry when the window opens. */
    public void focusCommandInput() {
        commandInput.requestFocus();
    }

    private void submitCommand() {
        String input = commandInput.getText();
        rememberCommand(input);
        controller.submit(input);
        commandInput.clear();
        refresh();
    }

    private void rememberCommand(String input) {
        if (!input.isBlank()) {
            commandHistory.remove(input);
            commandHistory.add(input);
        }
        commandHistoryIndex = commandHistory.size();
    }

    private void showPreviousCommand() {
        if (commandHistoryIndex == 0) {
            return;
        }

        commandHistoryIndex--;
        String previousCommand = commandHistory.get(commandHistoryIndex);
        commandInput.setText(previousCommand);
        commandInput.positionCaret(previousCommand.length());
    }

    private void showNextCommand() {
        if (commandHistoryIndex >= commandHistory.size()) {
            return;
        }

        commandHistoryIndex++;
        if (commandHistoryIndex == commandHistory.size()) {
            commandInput.clear();
            return;
        }

        String nextCommand = commandHistory.get(commandHistoryIndex);
        commandInput.setText(nextCommand);
        commandInput.positionCaret(nextCommand.length());
    }

    private void refresh() {
        if (!habitList.getItems().equals(controller.habits())) {
            habitList.getItems().setAll(controller.habits());
        }
        controller.history().ifPresentOrElse(historyCalendar::show, historyCalendar::clear);
        highlightAndRevealDisplayedHabit();
        feedback.setText(controller.feedback());
        feedback.pseudoClassStateChanged(javafx.css.PseudoClass.getPseudoClass("error"), controller.feedbackIsError());
    }

    private void highlightAndRevealDisplayedHabit() {
        controller.displayedHistoryHabitId().ifPresent(displayedHabitId -> {
            for (int index = 0; index < habitList.getItems().size(); index++) {
                if (habitList.getItems().get(index).id().equals(displayedHabitId)) {
                    habitList.getSelectionModel().select(index);
                    if (!isHabitVisible(index)) {
                        habitList.scrollTo(index);
                    }
                    return;
                }
            }
        });
    }

    private boolean isHabitVisible(int index) {
        return habitList.lookupAll(".list-cell").stream()
                .filter(javafx.scene.control.ListCell.class::isInstance)
                .map(javafx.scene.control.ListCell.class::cast)
                .anyMatch(cell -> !cell.isEmpty() && cell.getIndex() == index
                        && cell.getParent().getLayoutBounds().intersects(cell.getBoundsInParent()));
    }

    ListView<HabitSnapshot> habitListForTesting() {
        return habitList;
    }

    HabitHistoryCalendar historyCalendarForTesting() {
        return historyCalendar;
    }

    TextField commandInputForTesting() {
        return commandInput;
    }

    private void showHabitHistory(HabitSnapshot selectedHabit) {
        if (selectedHabit == null
                || controller.displayedHistoryHabitId()
                        .filter(selectedHabit.id()::equals)
                        .isPresent()) {
            return;
        }

        controller.showHabitHistory(selectedHabit);
        refresh();
    }

}
