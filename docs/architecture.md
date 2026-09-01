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

Habit indices are a use-case/presentation concept rather than persisted domain state. The
use-case layer defines one case-insensitive alphabetical display order and resolves a positive
integer selector against that one-based order. `ViewHabitsUseCase` uses the same order, and the
JavaFX view presents each snapshot in aligned index, current-status, and name columns. This keeps command resolution consistent
with the visible list without coupling the domain model or JSON format to mutable positions.
To keep positive integer selectors unambiguous, the domain supplies a Unicode-aware new-name
validation rule requiring at least one alphabetic letter. The add use case exposes violations as
an `INVALID_HABIT_NAME` result, and the command layer translates that result into user feedback.
Rehydration remains compatible with numeric names saved before this rule, allowing users to load
and delete those legacy entries by their displayed index.

Completion history is exposed in descending date order through `Habit.completionHistoryDescending()` and `ViewHabitHistoryUseCase`, so the most recent completion appears first.
The JavaFX UI presents those completion values in a scrollable month calendar. Calendar layout,
the visible month range, and completion-day styling remain presentation concerns in `ui`; no
JavaFX calendar types or navigation state cross into the use-case or domain layers.
