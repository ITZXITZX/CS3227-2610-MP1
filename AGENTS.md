# HabitZone agent guidance

- Do not create project-local Gradle user-home/cache directories (for example, `.gradle-user-home`) when running builds or tests. Use the existing Gradle configuration, and remove any accidental project-local cache immediately.
