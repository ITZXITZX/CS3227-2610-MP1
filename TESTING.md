# Testing Policy

This project keeps test output quiet by default and uses logs only when they help diagnose failures.

## Core rules

- Prefer assertions over logs for expected behavior.
- Do not use `println`-style output in unit tests unless you are actively debugging a failure.
- Use assertion failure messages for context instead of always-on logging.
- Keep any test logging deterministic.
- Allow logging only when it helps at integration or end-to-end boundaries.
- Keep logs short, targeted, and easy to disable.
- If a test needs a lot of logging, split the test or simplify the setup.
- In CI, rely on test reports and stack traces first, not verbose runtime logs.

## Practical guidance

- Unit tests should normally be silent.
- Integration tests may log external calls or boundary interactions sparingly.
- End-to-end tests may log enough context to reconstruct a failing flow, but still only when useful.
- Prefer test names and assertion messages that explain intent clearly.

## Good pattern

```java
assertEquals(201, response.statusCode(), () -> "Request was: " + request + ", response was: " + response);
```

This keeps normal test runs clean while still giving useful context if the assertion fails.
