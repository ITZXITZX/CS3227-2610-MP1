# HabitZone Architecture

HabitZone follows Clean Architecture with dependencies pointing inward:

- `domain`: core habit entities and value objects. No JavaFX, file storage, commands, or use case dependencies.
- `port`: interfaces owned by the application boundary, such as repositories and clocks.
- `usecase`: application actions that coordinate domain objects through ports and return structured results.
- `command`: translates user-entered command text into use case calls. It validates command syntax and formats command-level results, but contains no domain rules.
- `infrastructure`: outward-facing implementations of ports, such as JSON persistence and system-clock access. It may depend on the inner layers, never the reverse.
- `ui`: JavaFX views and controllers. It displays state and forwards user input to the command layer; it must not contain business or storage logic.
- `app`: JavaFX application entry points and dependency wiring only.

The intended dependency flow is `ui -> command -> usecase -> domain`, with
`usecase` depending on `port` interfaces and `infrastructure` implementing those
interfaces. This keeps the domain and application behavior independently testable
without JavaFX or file storage.

Completion history is exposed in descending date order through `Habit.completionHistoryDescending()` and `ViewHabitHistoryUseCase`, so the most recent completion appears first.
