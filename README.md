# DevSimulator clone

A practice platform for developers: browse coding challenges by difficulty, open one, and solve it
in an in-browser IDE that runs your code against real test cases. Landing page inspired by
devsimulator.dev, implemented with a Next.js frontend and a Spring Boot API.

## Stack

- **Frontend**: Next.js 14 (App Router), React 18, TypeScript, Monaco Editor (`@monaco-editor/react`)
- **Backend**: Spring Boot 3.3 / Java 21, hexagonal architecture (domain / application / adapter)
- **Code execution sandbox**: GraalVM JS (`org.graalvm.polyglot`), embedded in the JVM — no separate
  runtime or container to operate
- **Persistence**: in-memory (see [ADR 3](#adr-3-in-memory-repository-no-database-yet))

## Project structure

```
app/                          Next.js routes (App Router)
  page.tsx                    Landing page
  challenges/page.tsx         Challenge list
  challenges/[id]/page.tsx    Challenge detail + online IDE

backend/src/main/java/dev/devsimulator/
  challenge/
    domain/                   Challenge, ChallengeRepository, CodeRunner (ports), ExecutionResult
    application/               Use cases (List/Find/RunChallengeCode) — orchestration only
    adapter/in/web/            REST controller, request/response DTOs, mapper
    adapter/out/memory/        In-memory ChallengeRepository + seed data
    adapter/out/sandbox/       GraalJsCodeRunner — the sandboxed CodeRunner implementation
  common/web/                  Global exception handling, API error shape
```

## Running locally

### Backend

Requires JDK 21 and Maven.

```bash
cd backend
mvn spring-boot:run
```

Starts on `http://localhost:8080`.

### Frontend

```bash
npm install
npm run dev
```

Open `http://localhost:3000`. The frontend reads the API at `http://localhost:8080` by default —
set `NEXT_PUBLIC_API_URL` before starting Next.js to point elsewhere.

Challenge access is open: the API has no authentication or paid-plan gate.

## API

| Method | Path                        | Description                                                        |
|--------|-----------------------------|----------------------------------------------------------------------|
| GET    | `/api/challenges`           | Paginated list. Query params: `page`, `size`, `difficulty`          |
| GET    | `/api/challenges/{id}`      | Challenge detail — `404` if missing                                  |
| POST   | `/api/challenges/{id}/run`  | Runs `{ "code": "..." }` against the challenge's tests, sandboxed    |

`POST /run` responses:

```json
{
  "tests": [{ "name": "adds 8% tax to subtotal", "passed": true, "message": null }],
  "consoleOutput": "",
  "error": null,
  "timedOut": false
}
```

- `409 Conflict` if the challenge has no runnable code (design-only challenges).
- `400 Bad Request` if `code` is blank or exceeds 20,000 characters.

## Architecture Decision Records

### ADR 1: Next.js frontend + Spring Boot API, no BFF layer

**Context**: the frontend needs challenge data and a way to execute submitted code.

**Decision**: Next.js calls the Spring Boot API directly over REST; there's no GraphQL layer or
BFF in between.

**Consequences**: fewer moving parts for a project this size. If a second consumer (mobile app,
another frontend) shows up with different data-shaping needs, revisit — a translation layer
belongs between the API and multiple consumers, not baked into this API's controllers.

### ADR 2: Hexagonal architecture in the backend

**Context**: challenge listing needed to grow into challenge detail + code execution without the
web layer, business rules, and the (currently in-memory, later maybe real) data store getting
tangled together.

**Decision**: `domain` holds entities and ports (`ChallengeRepository`, `CodeRunner`) with zero
framework imports; `application` holds use cases that orchestrate ports; `adapter/in/web` and
`adapter/out/*` are the only places that know about Spring, HTTP, or GraalVM.

**Consequences**: swapping the in-memory repository for a real database, or the GraalVM runner for
a different sandbox, touches only one adapter package — the domain and use cases don't change.

### ADR 3: In-memory repository, no database yet

**Context**: 25 seeded challenges, no user accounts, no progress tracking yet.

**Decision**: `InMemoryChallengeRepository` holds a static seeded list; no database is wired up.

**Consequences**: state resets on every restart — fine today, wrong the moment progress tracking,
XP, or user-submitted challenges show up. `ChallengeRepository` is already a port, so adding a real
database later is an adapter swap, not a rewrite.

### ADR 4: Challenges as either runnable (with code) or design-only

**Context**: the seeded challenges span BEGINNER to STAFF difficulty. Staff-level challenges
("design a multi-tenant isolation strategy") aren't code katas — there's no single right answer to
execute and check.

**Decision**: `Challenge.isRunnable()` is true only when `language`, `starterCode`, and `testCode`
are all present. Today that's the 15 BEGINNER–MID challenges; SENIOR/STAFF challenges carry
`null` for all three and the frontend shows description-only, no editor.

**Consequences**: the `/run` endpoint has one clear rule for what's executable, enforced in
`RunChallengeCodeService` (`409` for non-runnable challenges) rather than the frontend guessing.

### ADR 5: Code execution sandboxed with embedded GraalVM JS, not a spawned Node process

**Context**: `POST /challenges/{id}/run` executes arbitrary user-submitted JavaScript. That's an
RCE risk if not isolated properly — this was flagged and discussed with the user before
implementation, along with the alternative of running the code in the browser instead of the
server (rejected: challenge test harnesses run server-side, and a browser sandbox can't verify a
submission it doesn't trust).

**Decision**: run submitted code inside a GraalVM `Context` (`org.graalvm.polyglot`) embedded
directly in the Spring Boot JVM, configured with `allowHostAccess(HostAccess.NONE)`, `allowIO`
disabled, `allowCreateThread(false)`. The only bridge between guest JS and the host is two
`ProxyExecutable` callbacks (`assertEqual`, `console.log`) — the polyglot API's purpose-built,
narrow channel for host callbacks that doesn't require broader host-access permissions.
Execution runs on a worker thread with a wall-clock timeout enforced by force-closing the context
(`context.close(true)`) from the caller thread — GraalVM's documented cancellation pattern, since a
running `eval` can't be interrupted any other way.

**Consequences**: no external process, container, or language runtime to install, deploy, or
patch — the sandbox is a library dependency. The trade-off is CPU: GraalVM without the native
GraalVM JDK runs JS in interpreter-only mode (visible as a startup warning), which is acceptable
for short challenge-sized scripts under the 5-second timeout but would need `js.compiler` tuning or
a proper GraalVM JDK if execution volume or script complexity grows. Only JavaScript is supported
today; adding a language means adding another `CodeRunner` port implementation.

### ADR 6: Monaco Editor for the in-browser IDE

**Context**: the challenge detail screen needed a real code editor, not a styled `<textarea>`.

**Decision**: `@monaco-editor/react`, dynamically imported (`next/dynamic`, `ssr: false`) since
Monaco depends on browser APIs unavailable during Next.js server rendering.

**Consequences**: same editor engine as VS Code (syntax highlighting, bracket matching) with
minimal integration code. Adds ~50KB+ to the challenge-detail bundle specifically — acceptable
since it's the one route that needs it and it's not part of the shared bundle.

### ADR 7: Test-name based assertions over a full unit-testing framework in the guest sandbox

**Context**: each runnable challenge needs a way to check the submitted solution and report which
specific checks passed or failed.

**Decision**: a small harness (`assertEqual(actual, expected, name)`) is prepended to every
execution, appending `{ name, passed, message }` to a results array that the runner reads back via
`JSON.stringify` — no Jest, Mocha, or similar library inside the sandbox.

**Consequences**: zero extra sandboxed dependencies to secure or update. Assertions are limited to
deep-equality checks (`JSON.stringify` comparison) — no custom matchers. Good enough for the
challenge shapes seeded so far (return-value checks); revisit if a future challenge needs to assert
on thrown exceptions or async behavior.
