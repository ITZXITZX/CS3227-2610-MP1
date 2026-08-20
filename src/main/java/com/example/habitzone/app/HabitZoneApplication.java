package com.example.habitzone.app;

import com.example.habitzone.command.CommandRegistry;
import com.example.habitzone.command.CommandResult;
import com.example.habitzone.infrastructure.JsonHabitRepository;
import com.example.habitzone.port.ClockProvider;
import com.example.habitzone.ui.HabitZoneView;
import com.example.habitzone.ui.MainWindowController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.time.LocalDate;

public class HabitZoneApplication extends Application {
    @Override
    public void start(Stage stage) {
        CommandRegistry commandRegistry = CommandRegistry.withRepository(
                new JsonHabitRepository(),
                (ClockProvider) LocalDate::now
        );
        MainWindowController controller = new MainWindowController(input -> executeAndRefresh(commandRegistry, input), stage::close);
        HabitZoneView root = new HabitZoneView(controller, LocalDate::now);

        Scene scene = new Scene(root, 980, 680);
        scene.getStylesheets().add(HabitZoneApplication.class.getResource("/com/example/habitzone/ui/habit-zone.css")
                .toExternalForm());
        stage.setTitle("HabitZone");
        stage.setMinWidth(780);
        stage.setMinHeight(560);
        stage.setScene(scene);
        stage.show();
    }

    private CommandResult executeAndRefresh(CommandRegistry commandRegistry, String input) {
        CommandResult result = commandRegistry.execute(input);
        if (result.error() || result.exit()) {
            return result;
        }
        CommandResult habits = commandRegistry.execute("list");
        return new CommandResult(result.message(), false, false, habits.habits(), result.history());
    }
}
