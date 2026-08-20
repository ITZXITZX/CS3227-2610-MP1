# HabitZone MVP Manual Acceptance Test

This script verifies the complete MVP flow through the JavaFX application. Run it
from a clean data state so that the expected habit counts are deterministic.

## Setup

1. Close every running HabitZone window.
2. If `data/habits.json` exists, back it up or remove it. HabitZone recreates it
   as an empty store on first use.
3. Start the app with `./gradlew run` and wait for the window titled **HabitZone**.
4. Confirm that the current date is shown in the top bar and that the feedback
   area initially says `Type 'help' to see available commands.`.

Enter each command below in the command field and press Enter. Check both the
feedback area and the two panels after every command.

## Acceptance flow

| Step | Command | Expected feedback | Expected visible state |
| --- | --- | --- | --- |
| 1 | `list` | `You do not have any habits yet.` | **Your habits** is empty. **Selected habit history** is empty. |
| 2 | `add reading` | `Added habit 'reading'.` | **Your habits** contains exactly `reading`; the history panel is empty. |
| 3 | `done reading 2026-08-19` | `Marked 'reading' complete on 2026-08-19.` | **Your habits** still contains `reading`; the history panel is empty until requested. |
| 4 | `history reading` | `Showing history for 'reading'.` | **Selected habit history** contains exactly `2026-08-19`. |
| 5 | `undone reading 2026-08-19` | `Unmarked 'reading' complete on 2026-08-19.` | **Your habits** still contains `reading`; the history panel is cleared because the displayed habit snapshot was refreshed. |
| 6 | `delete reading` | `Deleted habit 'reading'.` | **Your habits** is empty and the history panel is empty. |
| 7 | `list` | `You do not have any habits yet.` | Both panels remain empty. |

## Optional persistence check

To also check the MVP persistence criterion, repeat steps 1--3, close the app,
start it again, and enter `list`. The feedback should be `Found 1 habit.` and
**Your habits** should contain `reading`. Delete it afterwards by running
`delete reading` so later acceptance runs start cleanly.

## Architecture guardrails

- The JavaFX UI only renders controller state and forwards command text.
- Commands parse text and call use cases; business rules stay out of the UI and
  command layer.
- Use cases depend on ports, while JSON storage implements those ports.
- Domain code remains independent of JavaFX, file storage, commands, and Gradle
  application classes.

The acceptance flow is manual end-to-end coverage. Keep the automated unit and
adapter tests as the focused regression suite for the same behavior.
