package com.example.habitzone.usecase;

import java.util.Objects;
import java.util.Optional;

public record UseCaseResult<T>(boolean success, T data, UseCaseError error) {
    public UseCaseResult {
        if (success) {
            Objects.requireNonNull(data, "data");
            if (error != null) {
                throw new IllegalArgumentException("Successful result cannot include an error");
            }
        } else {
            Objects.requireNonNull(error, "error");
        }
    }

    public static <T> UseCaseResult<T> success(T data) {
        return new UseCaseResult<>(true, data, null);
    }

    public static <T> UseCaseResult<T> failure(UseCaseError error) {
        return new UseCaseResult<>(false, null, error);
    }

    public Optional<T> dataOrEmpty() {
        return Optional.ofNullable(data);
    }
}
