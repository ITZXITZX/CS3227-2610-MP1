# Reflections on AI-Assisted Software Engineering

## Introduction

I used an LLM throughout HabitZone's development for architecture exploration, implementation planning, coding, testing, debugging, documentation, and UI refinement. The experience showed me that an LLM is highly useful as an engineering collaborator. It could inspect many files quickly, suggest alternatives, and turn requirements into working code, but the quality of its output depended heavily on the context and constraints in my prompt. I still had to decide what the product should do, recognise when an answer was only plausible rather than verified, test the application as a user, and correct decisions that no longer matched the project.

My prompting also changed over time. Early prompts were broad and exploratory. Later prompts referred to `PLAN.md` and `architecture.md`, requested focused tests and a code trace, and specified how relevant files should be staged. This made the work more reproducible and reduced architectural drift. However, longer prompts were not automatically better: precise acceptance criteria and relevant context mattered more than asking for a very detailed explanation.

## Example 1: Evolving the architecture prompt

My first architecture prompt described HabitZone as a single-user habit tracker, listed the MVP operations, and stated that it had to be a Java desktop application controlled using CLI commands. I then asked the LLM to propose several architectures, draw each one, and recommend the most suitable option.

I formulated the prompt this way to use the LLM for design-space exploration rather than prematurely asking it to implement the first design it produced. Requiring multiple proposals and a recommendation encouraged comparison of trade-offs. The diagrams also made it easier to assess dependency direction rather than relying only on architectural terminology.

The LLM proposed a layered CLI architecture, a command-pattern architecture, and an MVC-inspired CLI architecture. It recommended the simple layered design because it inferred that the MVP had a small command set, one user, and simple persistence. This was a reasonable answer to the prompt, but it exposed missing information in my requirements. I had not said clearly that “CLI” meant a command interface embedded in a modern JavaFX UI. I also had not described planned features such as expiry, reminders, streaks, categories, and priority. Consequently, the first recommendation optimised for the current MVP and did not adequately account for the existing JavaFX scaffold or future extensibility.

I refined the prompt by explicitly adding JavaFX, the future features, and the requirement that extensions should not be tightly coupled. The second response proposed layered JavaFX, MVC with the Command pattern, and Clean/Hexagonal Architecture. It recommended a lightweight Clean Architecture with a command adapter. This recommendation became the basis of the project's `ui`, `command`, `usecase`, `domain`, `port`, and `infrastructure` boundaries.

This exchange taught me that an LLM's assumptions usually fill gaps in the prompt with the simplest locally reasonable interpretation. The initial answer was not exactly “wrong”; it solved an underspecified problem. I verified and challenged it by comparing it with the actual JavaFX project scaffold and my product roadmap. The engineering judgement was deciding that some additional structure was justified by known future changes, while avoiding unnecessary enterprise-level complexity for a small application.

Next time, I would state both current constraints and likely directions of change in the first design prompt. I would also request explicit assumptions and evaluation criteria—for example, UI independence, testability, persistence replaceability, and the cost of adding a new command. That would make hidden assumptions visible earlier and allow the proposals to be compared on criteria that matter to the project.

## Example 2: Turning a design into incremental issue prompts

After choosing the architecture, I asked:

> Using your latest recommendation, create a plan for another AI agent to develop incrementally and test incrementally while remaining aligned with the overall architecture. Break it into phases and issue-sized tasks, and state what success looks like for each task.

The purpose of this prompt was not only to obtain a to-do list. I wanted persistent project context that future AI sessions could use even when they did not contain the original architecture discussion. Asking for “issue-sized” tasks constrained the scope, while “what success looks like” turned design intentions into acceptance criteria. The resulting `PLAN.md` included architecture rules, task descriptions, success conditions, and tests for each phase.

This worked well as a bridge from exploration to production, but it also revealed a weakness in relying on familiar labels. The LLM interpreted “similar to a GitHub issue” reasonably, yet some generated titles—such as “Establish package boundaries” and “Implement MVP use cases”—were too broad when separated from their full descriptions. The tasks were understandable only because the body supplied the missing scope. I verified this by presenting parts of the plan independently to another chatbot and asking whether the issues were clear. That comparison highlighted that a plan can be structurally complete while its issue titles remain ambiguous.

I then developed a reusable implementation prompt:

> Implement Phase _ Issue __ in PLAN.md. Trace the code line by line and explain all relevant file changes with code examples in a structured manner. Follow the project logging guidelines. Guide me on how to run and test it in Git Bash. Stage only the changed files relevant to this issue.

This prompt evolved from simply asking for implementation. Each added clause addressed a practical problem: referencing the plan supplied scope and architecture; requesting tests made verification part of completion; asking for a trace helped me learn and review cross-layer changes; and limiting staging reduced the risk of committing unrelated files from a dirty working tree.

One useful result occurred in Phase 6 Issue 11. While wiring the JavaFX UI to the command layer, the LLM found that the result contract could not distinguish “no habit-list payload” from “an updated but empty habit list.” Therefore, deleting the final habit could leave stale UI content. It introduced an explicit marker for whether a result included a habit snapshot and added an end-to-end adapter test for the add, complete, history, uncomplete, and delete flow. This was more valuable than mechanically following the issue because it identified an interface ambiguity across layers.

However, “trace line by line” often produced more explanation than I needed and did not itself improve the code. For small local changes, manually reading a diff was faster. Next time, I would ask for a data-flow trace and explanations of non-obvious decisions rather than every line. I would also make issue titles action-oriented and self-contained, list files or layers expected to change, and include explicit non-goals so an agent can work safely with less context.

## Example 3: Using manual smoke testing to refine error handling

After Issue 11 passed focused and full automated tests, I manually entered `done exercise`. The application replied, “Please provide a habit name,” even though the name was present and the missing argument was the date. I prompted the LLM with the exact command, the observed message, the expected distinction, and the requirement that both `done` and `undone` behave consistently.

This prompt was effective because it contained a minimal reproduction and a user-visible expected outcome. It gave the LLM much less room to guess than a vague request such as “fix command errors.” The LLM traced both commands to their shared dated-command parser, changed a single non-date token to mean “habit name supplied, date missing or invalid,” retained the date-only case as “habit name missing,” and added regression tests for both commands.

The important lesson was that a passing test suite did not prove that the interaction was understandable. Existing tests validated technical behaviour but had encoded or overlooked the wrong message. Manual smoke testing supplied product knowledge that the LLM and tests did not have: error messages should respond to what the user actually entered and teach the correct syntax.

This example also led to another refinement. When similar usage messages were later added, I noticed that the LLM had hard-coded strings instead of using the existing `CommandMessages` class. I asked it to refactor the messages through that shared abstraction and apply the pattern to other commands. The LLM initially optimised for the local fix; I had to enforce consistency with the codebase's conventions. Engineering judgement was needed to decide that centralised messages were worth preserving for consistency and maintenance, rather than accepting duplicated but functional literals.

Next time, I would include an error-message table in the acceptance criteria before implementation. It would cover empty arguments, a name without a date, a date without a name, an invalid date, and an unknown habit. I would also require tests to assert both behaviour and helpful command-specific usage text. This would shift usability verification earlier instead of discovering it only during a smoke test.

## Example 4: Recognising where prompting and code changes were insufficient

A more difficult interaction concerned initial keyboard focus. The command field displayed a caret when HabitZone launched, but typing did nothing until I clicked the window. The first prompt asked the LLM to fix the issue and explain the change. It inferred that JavaFX focus traversal was overriding an early `requestFocus()` call and deferred the request using `Platform.runLater(...)`. The change was plausible, but my manual test showed that it did not solve the problem.

I responded with direct evidence: the window had appeared, but I still needed to click it. The LLM then distinguished focus inside the JavaFX scene from operating-system focus on the application window and added stage-level focus requests. When I explained that launching through Gradle still failed, it identified a further environmental constraint: Windows may prevent a child application from stealing foreground focus from the terminal or Gradle process. It recommended testing a packaged application launched from File Explorer and acknowledged that JavaFX cannot guarantee foreground activation.

This was a case where repeated prompting could improve the diagnosis but could not override the operating system. The first response also demonstrated the danger of equating a theoretically correct API call or `git diff --check` with verified user behaviour. I verified the feature through direct observation in the actual launch context, not merely compilation. The appropriate engineering decision was to define a realistic test environment and accept the platform boundary rather than continuing to add increasingly aggressive focus code.

Next time, I would include the operating system, exact launch command, foreground application, and expected test procedure in the first bug report. I would also ask the LLM to separate guarantees from best-effort behaviour and to state what cannot be automated reliably. For environment-sensitive UI defects, manual work and platform knowledge can be more effective than further prompt elaboration.

## Verification, mistakes, and engineering responsibility

There was one instance where I asked for Windows and macOS/Linux test commands in the Developer Guide, the LLM reported that the file had been updated. There was some mismatch between the opened tab of that file and the file that was saved. I saved and overwrote the older version on accident, then could not find it. When I asked it to check whether the change was saved, it discovered that the edit was absent and that older test-status text had returned. It then restored and re-read the file. This showed that even a confident completion statement is not evidence. For documentation as well as code, verification should inspect the saved file and working-tree diff, not rely on the model's account of what it intended to do.

My verification approach therefore combined several levels:

1. I inspected diffs and staged-file lists to ensure only issue-related changes were included.
2. I ran focused tests for fast feedback and the complete Gradle suite for integration confidence.
3. I manually exercised user workflows because automated tests could miss misleading text, focus behaviour, layout, and scrolling quality.
4. I checked architecture boundaries when a feature crossed layers.
5. I re-opened generated documentation and compared it with the actual product.

The LLM was strongest when the task had an observable contract: a failing test, an exact command and output, a named architecture rule, or a clear UI state transition. It was less effective when requirements were implicit, when visual quality was subjective, or when behaviour depended on Windows window-management policy. In those cases, manual experimentation and human judgement remained essential.

## Overall reflection

AI assistance accelerated HabitZone substantially. It was especially useful for exploring alternatives, tracing a feature across layers, generating focused regression tests, and maintaining consistency across code and documentation. Prompting also acted as a form of requirements engineering: every time the result was wrong, I had to determine whether the cause was an implementation defect, a missing constraint, an unsuitable abstraction, or an invalid assumption about the environment.

The main lesson is that effective AI-assisted software engineering is an iterative control loop:

> provide context and an observable goal → inspect assumptions and changes → verify with tests and real use → refine the requirement or implementation

The engineer remains responsible for every part of that loop. The LLM can propose and execute quickly, but it does not own the product intent, cannot reliably judge all user experience details, and may report completion before the saved state is verified. My future prompts will be shorter but more precise: they will reference authoritative project documents, specify acceptance examples and non-goals, ask for assumptions and limitations, and require verification appropriate to the risk. That approach treats the LLM neither as an oracle nor merely as autocomplete, but as a fast collaborator whose work must still be reviewed with normal software-engineering discipline.
