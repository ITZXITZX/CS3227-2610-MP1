---
name: log-conversation
description: Save or update a clean Markdown transcript of the current conversation in `logs/transcript.md`. Use when the user asks to save, record, log, journal, or preserve this conversation.
---

# Logging Conversation

## Goal

Maintain exactly one transcript entry per conversation thread in `logs/transcript.md`. Keep entries in ascending chronological order: the earliest date/time is at the top and the latest is at the bottom.

A conversation entry represents the entire active thread: every accessible, visible user and assistant message, from the first conversational message through the current request. Each assistant message records the model and reasoning setting used for that message. Re-running this skill in the same thread must update that entry rather than create a duplicate.

## Thread identity

Use the timestamp of the first visible user message as the thread identity. Format it as `YYYY-MM-DD HH:MM:SS +08:00` and use this exact header:

```md
## Conversation - <thread-start timestamp>
```

Before asking the user, actively inspect every thread-metadata source available in the client context: the conversation header, the timestamp attached to the first visible user message, and any supplied thread-start field. Use the first valid timestamp found, converted to the required format.

- If the timestamp is not directly exposed, search existing transcript entries for the first visible user message to recover its matching thread header.
- Do not infer a first-message timestamp from message wording or use the current time as a substitute.
- If no timestamp can be found after these checks, use a stable client-supplied thread identifier. Ask the user only if neither a timestamp nor a stable identifier is available.
- Do not claim a transcript is complete when earlier turns are unavailable because of context limits.

## Exclusions

Include every visible user and assistant message, including assistant progress updates and requests for information.

Do not log:

- System, developer, tool, or hidden agent messages
- Tool calls, command output, raw command links, client metadata, UI indicators, or status updates
- Skill invocation syntax such as `[$log-conversation](...)`
- URLs in embedded Markdown links. Preserve the visible link label only; for example, log `[architecture.md](C:\\project\\docs\\architecture.md)` as `architecture.md`.

## Assistant model details

For each assistant message, obtain the model name and reasoning setting from the message or active-thread metadata. Preserve changes within a thread: if different assistant messages used different settings, label each message with its own settings.

- Use the exact user-facing names supplied by metadata, such as `5.6 Terra` and `Light reasoning`.
- Do not guess, generalize, or derive the setting from hidden system instructions.
- If the model or reasoning setting for any assistant message is unavailable or ambiguous, ask the user to provide it before writing or updating the transcript entry.

## Procedure

1. Ensure `logs/transcript.md` exists. If it does not, create it with only `# Transcript`.
2. Determine the exact thread header using the thread identity discovery procedure above.
3. Search `logs/transcript.md` for that exact header.
4. If it exists, replace the complete block after the header through (but not including) the next `## Conversation -` header, or through end of file.
5. If it does not exist, compare its full header timestamp (date first, then time) with the existing entry timestamps and insert it immediately before the first later entry. Append it only when it is later than every existing entry.
6. Preserve all existing entries unchanged and keep the complete transcript in ascending chronological order.
7. Write the transcript explicitly as UTF-8 without a BOM. Re-read the written entry as UTF-8 before reporting success.

## Text encoding and portability

- Never let UTF-8 content pass through a legacy Windows code page. Strings such as `â€™`, `â€œ`, `â€`, or `â€”` are mojibake and must not be written to the transcript.
- After writing or updating an entry, scan that entry for the mojibake prefix `â` and the Unicode replacement character `U+FFFD`. If either is found, repair the entry before continuing.
- When the available write path cannot preserve typographic Unicode characters reliably, use equivalent ASCII punctuation (`'`, `"`, and `-`) in the transcript. Preserve wording and meaning; do not leave corrupted bytes behind.

## Required entry format

## Conversation - <thread-start timestamp>

### Conversation history

```markdown
**User:** <message>

**Assistant (<model>, <reasoning setting>):** <message>
```

### Summary of Actions Taken

- <completed action, or `No actions taken.`>

Keep all conversation history between the `### Conversation history` and `### Summary of Actions Taken` headings inside one fenced `markdown` block. Keep turns in chronological order, separated by blank lines, and use the bold `**User:**` and `**Assistant (<model>, <reasoning setting>):**` labels exactly as shown.

## Accuracy rules
- Preserve the full meaning of every logged message; do not summarize conversation history.
- Summarize only completed actions in the summary section.
- Exclude the logging request itself unless the user explicitly asks for it to be included.
- If only part of the thread is accessible, add this item under the summary:

  `- Note: Earlier turns were unavailable in the current context.`

<!-- Legacy rationale retained below as a comment.
The most important change is replacing “identify/extract the first message and timestamp” with a defined metadata source and fallback. That makes the skill reliable rather than dependent on information the agent may not have.
