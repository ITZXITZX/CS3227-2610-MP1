# HabitZone User Guide

HabitZone is a desktop habit tracker operated through typed commands. It stores habits locally, records completion on individual dates, shows each habit's completion calendar, and calculates a streak ending today.

## Command summary

| Command | Purpose |
| --- | --- |
| `add HABIT_NAME` | Add a new habit. |
| `list` | Reload and display all habits. |
| `done HABIT_INDEX_OR_NAME [YYYY-MM-DD]` | Mark a habit complete today or on the specified date. |
| `undone HABIT_INDEX_OR_NAME [YYYY-MM-DD]` | Remove today's completion or the completion on the specified date. |
| `history HABIT_INDEX_OR_NAME` | Display a habit's completion calendar. |
| `streak HABIT_INDEX_OR_NAME` | Show the consecutive completion streak ending today. |
| `delete HABIT_INDEX_OR_NAME` | Permanently delete a habit and its completion history. |
| `help` | Display the available command formats. |
| `exit` | Close HabitZone. |

## Requirements and setup

You need:

- Windows, macOS, or Linux with a graphical desktop
- JDK 25 installed and available through `JAVA_HOME` or on `PATH`
- An internet connection on the first build so Gradle can download its own distribution and the project dependencies

You do not need to install Gradle separately; the repository includes the Gradle wrapper.

Open a terminal in the repository root (the directory containing `gradlew` and `build.gradle.kts`) and verify Java:

```text
java -version
```

The reported major version should be 25. Then start HabitZone:

```powershell
# Windows PowerShell or Command Prompt
.\gradlew.bat run
```

```bash
# macOS or Linux
./gradlew run
```

The first launch can take longer while dependencies are downloaded. A window titled **HabitZone** opens when startup is complete.

## Getting around HabitZone

The window has three main areas:

- **Your habits** lists saved habits alphabetically, ignoring letter case. Each row uses aligned columns for its one-based number, `(done)` or `(undone)` status for the current local date, and habit name.
- **Selected habit history** shows a calendar for one habit. Completed dates are highlighted.
- The bottom area shows feedback, a command field, and a **Run** button. Type a command and press **Enter** or click **Run** to execute it. The field is focused when the application opens.

The current date appears at the top right, followed by a **?** help button that displays the available commands. On startup, saved habits are loaded automatically and the feedback reports either `You do not have any habits yet.` or the number found.

You can select a habit with the mouse or the list's normal Up/Down keys. Selecting it opens that habit's history. The calendar covers every month from the earliest relevant month through the latest, always including the current month, and initially scrolls to the current month.

### Keyboard controls

| Keys | Action |
| --- | --- |
| `Enter` in the command field | Run the typed command |
| `Up` / `Down` in the command field | Move through commands entered during this run; Down past the newest command clears the field |
| `Up` / `Down` in the habit list | Change the selected habit and displayed history |
| `Left` / `Right` in the habit list | Scroll horizontally when a long habit name creates a horizontal scrollbar |
| `Shift+Left` / `Shift+Right` | Focus the habits panel / history panel |
| `Shift+Down` | Focus the command field |
| `Shift+Up` | Return to the most recently focused upper panel |

Command history is kept only until the application closes. If the same command is submitted again, its older history entry is replaced by the newest one.

## Command conventions

- Command words are case-insensitive: `ADD Read` and `add Read` invoke the same command.
- Habit lookup is also case-insensitive. A habit saved as `Morning Run` can be addressed as `morning run`.
- Commands that operate on an existing habit also accept its one-based number from **Your habits**, such as `done 2`. Indices follow the current alphabetical list and can change when a habit is added or deleted, so check the visible list before using one.
- Habit names may contain spaces and do not use quotation marks. Leading and trailing spaces are removed; capitalization is otherwise preserved.
- Habit names must contain at least one alphabetic letter. Numeric-only names such as `3`, and names made only from punctuation or symbols, are rejected because positive numbers are reserved for list indices. Names that mix letters and numbers, such as `Run 3 km`, are valid.
- Two names that differ only by capitalization are duplicates.
- Dates must use the exact ISO format `YYYY-MM-DD`, for example `2026-09-01`.
- In the command formats below, `HABIT_INDEX_OR_NAME` means either a displayed number or the full habit name. Text in square brackets is optional. Do not type the brackets.

Enter `help` at any time to display the command formats available in the running application.

## Features and commands

### Add a habit

```text
add HABIT_NAME
```

Example:

```text
add Morning Run
```

The habit is saved immediately and appears in **Your habits**. New habits start with no completed dates and therefore show `(undone)`. Adding a case-insensitive duplicate reports `That habit already exists.` and makes no change. A name without any letters reports `Habit name must contain at least one letter.` and is not saved.

### List habits

```text
list
```

This reloads the alphabetically sorted habit list. It reports the number found, or `You do not have any habits yet.` when empty. It also clears the history panel.

### Mark a habit complete

```text
done HABIT_INDEX_OR_NAME [YYYY-MM-DD]
```

Examples:

```text
done Morning Run
done Morning Run 2026-08-31
done 1
```

Without a date, HabitZone uses the computer's current local date. With a date, it records that exact date, including a past or future date. Marking an already completed date again leaves one completion for that date; it does not create duplicates.

HabitZone treats a valid date at the end of this command as the optional date and all preceding text as the habit name. Consequently, avoid habit names whose final word is a date such as `Challenge 2026-09-01`, because that final word will be interpreted as the command's date.

### Unmark a completion

```text
undone HABIT_INDEX_OR_NAME [YYYY-MM-DD]
```

Examples:

```text
undone Morning Run
undone Morning Run 2026-08-31
undone 1
```

Without a date, HabitZone removes today's completion. With a date, it removes that date. If the date was not marked, the habit is left unchanged and the command still succeeds.

### View completion history

```text
history HABIT_INDEX_OR_NAME
```

Example:

```text
history Morning Run
history 1
```

This selects the matching habit and shows its calendar. Clicking a habit in **Your habits** performs the same history lookup. A habit with no completions still shows the current month's calendar with no highlighted dates.

### View the current streak

```text
streak HABIT_INDEX_OR_NAME
```

Example:

```text
streak Morning Run
streak 1
```

The result is the number of consecutive completed days ending on today. If today is not complete, the current streak is `0`, even when earlier consecutive dates exist. This command reports the streak in the feedback area; it does not add streak information to the habit row or calendar.

### Delete a habit

```text
delete HABIT_INDEX_OR_NAME
```

Example:

```text
delete Morning Run
delete 1
```

Deletion is immediate and has no confirmation prompt. It permanently removes the habit and all of its recorded completion dates from HabitZone's data file.

### Show help

```text
help
```

This prints the available command formats in the feedback area.

### Exit

```text
exit
```

This closes the HabitZone window. Closing the window with the operating system's window control has the same practical effect.

## Feedback and refresh behavior

After every successful non-exit command, HabitZone reloads the habit list from storage. A history command then displays the requested history; other successful commands clear an open history calendar. If a command fails--for example, because the habit does not exist--the existing list and calendar remain displayed and the feedback is styled as an error.

An empty command reports `Please enter a command.` An unrecognized command reports `Unknown command. Type 'help' to see available commands.` If HabitZone cannot read or write its data, it reports a storage error instead of applying the requested change.

## Saved data

HabitZone saves data in `data/habits.json`, relative to the directory from which it is launched. Changes made by `add`, `done`, `undone`, and `delete` are written immediately and are loaded on the next launch.

Do not edit `habits.json` while HabitZone is open. Malformed or unsupported content prevents the application from loading the habit list and produces a storage error. To reset HabitZone for testing, close every HabitZone window, then move or delete `data/habits.json`; the application creates a new empty file on its next read. Back up the file first if its contents matter.

## Testing the system

### Automated tests

Close HabitZone, open a terminal in the repository root, and run:

```powershell
# Windows
.\gradlew.bat test
```

```bash
# macOS or Linux
./gradlew test
```

The HTML report is generated at `build/reports/tests/test/index.html`. At the time this guide was written, all 97 tests passed in the verified Windows environment.

### Manual end-to-end test

For deterministic results, first close HabitZone and back up or remove `data/habits.json`. Start the application, then enter these commands in order:

| Step | Command | Expected result |
| --- | --- | --- |
| 1 | `list` | Feedback says `You do not have any habits yet.` and both panels are empty. |
| 2 | `add Reading` | Feedback says `Added habit 'Reading'.`; the aligned list row contains `1.`, `(undone)`, and `Reading`. |
| 3 | `done 1 2026-08-19` | Feedback says `Marked 'Reading' complete on 2026-08-19.` |
| 4 | `history 1` | The history calendar includes August 2026 and highlights day 19. |
| 5 | `streak 1` | The feedback reports a streak of 0 unless the test is run on 2026-08-19; the history panel is cleared. |
| 6 | `undone 1 2026-08-19` | Feedback confirms the date was unmarked; the list still contains the habit. |
| 7 | `delete 1` | Feedback says `Deleted habit 'Reading'.`; both panels are empty. |
| 8 | `list` | Feedback again says `You do not have any habits yet.` |

To test persistence, add a habit, close HabitZone, start it again from the same repository directory, and confirm that the habit is already present. Delete the test habit afterwards if you want to restore the prior empty state.
