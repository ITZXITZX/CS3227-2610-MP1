package com.example.habitzone.command;

import com.example.habitzone.infrastructure.StorageException;
import com.example.habitzone.usecase.UseCaseResult;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

final class CommandSupport {
    private CommandSupport() {
    }

    static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    static Optional<LocalDate> parseIsoDate(String value) {
        try {
            return Optional.of(LocalDate.parse(value));
        } catch (DateTimeParseException exception) {
            return Optional.empty();
        }
    }

    static <T> CommandResult executeUseCase(
            Supplier<UseCaseResult<T>> action,
            Function<T, CommandResult> onSuccess
    ) {
        try {
            UseCaseResult<T> result = action.get();
            if (!result.success()) {
                return CommandResult.failure(CommandMessages.fromUseCaseError(result.error()));
            }
            return onSuccess.apply(result.data());
        } catch (StorageException exception) {
            return CommandResult.failure(CommandMessages.STORAGE_FAILURE);
        }
    }
}
