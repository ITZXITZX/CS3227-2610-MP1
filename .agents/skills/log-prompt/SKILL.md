---
name: log-prompt
description: Log the full conversation history for the current Codex thread as a single Markdown entry. Use when the user asks to record, audit, journal, or preserve a conversation.
---

# Prompt Reply Log

Create exactly one Markdown log entry for the current conversation after completing the requested work.

1. Preserve the conversation history verbatim, including the user's messages, assistant replies, and any tool or file actions that materially affected the work.
2. Track material actions concisely; never include secrets, credentials, hidden reasoning, or raw tool telemetry.
3. Draft the exact user-facing final reply before logging.
4. Run `scripts/append-log.ps1`, passing the conversation history, exact drafted reply, and action summaries. Unless the user requests another path, use the script's repository-local default, `logs/log-prompt.md`.
5. Verify that one new `## Conversation` section was appended and that its reply matches the final reply exactly. Then send that reply without changing it.

If logging fails, do not claim success. Explain the failure in the final reply. Do not log commentary messages as replies; only log the single final reply.
