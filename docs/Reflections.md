0. Set Up: Medium Control

1. Exploration mode, Planning mode, Production mode 
- Exploration mode:
    - 1st prompt
        - described features (functional requirements) of the MVP
        - stated assumptions for the MVP
        - stated my constraint: Java Desktop App (using CLI)
        - Then, I asked the Agent to propose a few architectures, with simple diagrams using mermaid or plantUML for each proposal, and recommend the most suitable one
    - 1st prompt results
        - Proposed:
            - Simple Layered CLI Architecture
            - Command Pattern Architecture
            - MVC-Inspired CLI Architecture
        - Recommended: 
            - Simple Layered CLI Architecture
            - My requirements suggest that the app has a "small, well-defined command set, a single user, and simple persistence needs"
    - 1st prompt evaluation:
        - LLM understood the problem (requirements, context)
        - LLM explained each proposal with helpful diagrams and explanation of the shape, pros, cons.
        - For the recommendation, it additionally suggested the API for each module
        - However, since my prompt was not comprehensive about the possibility of future versions of the app, thus the proposed architecture was not designed with consideration for that
        - Even though my initial skeleton was built using IntelliJ to include javaFX, the proposed architectures did not include javaFX in the design. Thus my requirements should be more explicit


    - 2nd prompt
        - added requirement for javaFX
        - informed agent of additional future features, added without tight coupling
    - 2nd prompt results:
        - Proposed:
            - Layered JavaFX + CLI Architecture
            - MVC + Command Pattern
            - Clean Architecture / Hexagonal Architecture (UI layer, App layer, Domain layer, Infra layer)
        - Recommended:
            - Clean Architecture With Command Pattern Adapter
            - "gives you the extensibility you want without tying the app to JavaFX or to the current MVP feature set."
            - Gave other key design decisions reasons
            - Gave suggested MVP UI, suggested commands, Test Plan, Assumptions
    - 2nd prompt evaluation
        - added requirements were now considered together with previous prompt (since its within the same context window)
        - suggested commands were not the shortest phrasing, but it works for the MVP as its clear (eg `delete`, `history`)

- Planning Mode
    - my prompt:
        - want to create a prompt for another agent to understand and develop incrementally, testing incrementally, always aligning to the overall architecture
        - break down recommendation into phases, and for each phase a specific task (similar to each github issue), how it looks like if it succeeds
        - include any other important details
    - my prompt design considerations:
        - wanted to create an architecture that can be referenced as context for each feature built
        - "similar to github issue" helps scope the size to what the model knows to be a suitable github issue --> however its an assumption that the models' idea of a github issue is the same as mine
    - my prompt results:
        - provided architecture rules for every issue
        - provided architectural diagram
        - For each phase and issue:
            - Task
            - What success looks like
            - Tests
    - my prompt evaluation
        - passed 1 issue to gemini chatbot to verify that the issues were clear.
        - However, when I passed just the issue titles to gemini, and told it that I felt it was unclear, it then agreed that the issues are unclear. 
            - Problem 1: Implementation without scope. For example, issue 6 is `Implement MVP use cases` but does not specify which use cases
            - Problem 2: Broad Architectural Jargon. For example, issue 2 is `Establish package boundaries` that could be a refactoring task or a design rule.
        - Thus, while the entire MVP development is broken down into phases/issues with a clear structure of task, goal, test, it does not follow the best practice in naming the github issue to be descriptive.
- Production
    - Asked agent to create issues on github first based on the issues generated during the plan stage
        

2. Prompting Strategies
- Tree of Thoughts (ToT) Prompting
    - Architecture Exploration stage
- Zero-shot Chain of Thought
    - Planning stage


3. 3 Examples of interesting prompts