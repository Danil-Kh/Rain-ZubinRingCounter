# Project Overview

RainZubinRingCounter is an **offline Windows desktop application** that scans Ukrainian-language
`.docx` transcript/subtitle files, counts how many lines ("rings") each character speaks, and writes
report `.docx` files. It is a **hybrid Spring Boot + JavaFX** app: Spring Boot owns the bean container
and DI; JavaFX owns the UI (drag-and-drop of `.docx` files onto a circle). Apache POI (XWPF) does all
Word document reading and writing. Ships to end users as a single `.exe` produced by wrapping the fat
JAR with Launch4j. No network, no server, no web surface — treat it as a fully local desktop tool.

Domain vocabulary is Ukrainian and load-bearing: a valid transcript table header is exactly
`ТАЙМ-КОД` / `ПЕРСОНАЖ` / `ТЕКСТ`, and names are sorted with a `uk-UA` `Collator`. Do not "normalize"
these strings to English.

- App version shown in UI: `app.version` in `application.properties` (currently `4.0.1`) — this is the
  real product version and is intentionally **decoupled** from the Maven `0.0.1-SNAPSHOT`.

# Core Commands

Windows is the primary dev/target OS; use the wrapper. From the repo root:

- **Build (fat JAR):** `.\mvnw.cmd -B clean package`
  Produces `target\RainZubinRingCounter-0.0.1-SNAPSHOT-jar-with-dependencies.jar` via
  `maven-assembly-plugin` (Main-Class `org.example.rainzubinringcounter.RainZubinRingCounterApplication`).
- **Run tests:** `.\mvnw.cmd test`
- **Single test class:** `.\mvnw.cmd test -Dtest=RingReaderTest`
- **Single test method:** `.\mvnw.cmd test -Dtest=RingReaderTest#one_error_parsing_episod_27`
- **CI-equivalent full check:** `.\mvnw.cmd -B clean verify` (this is exactly what GitHub Actions runs).
- **Run from source (dev):** run the `main` in `RainZubinRingCounterApplication`, or
  `java -jar target\...-jar-with-dependencies.jar`.

**Package EXE (manual, not wired into Maven):**
1. Build the fat JAR (above).
2. Wrap it with **Launch4j** using `RainZubinRingCounter-0.0.1-SNAPSHOT.xml` as the config.
   ⚠️ That config has **hard-coded absolute paths** (`C:\Java program\...\target\...jar` →
   `...\out\artifacts\...\test.exe`). Update `<jar>` and `<outfile>` to the current machine before
   generating the EXE. There is no CI step for the EXE — it is built by hand.
   ⚠️ **Bundle a JRE.** The Launch4j `<jre>` block points at `%JAVA_HOME%;%PATH%` with
   `minVersion 1.6.0_22`, so on a customer machine without a matching Java+JavaFX the EXE dies at launch
   with **"A JNI error has occurred"** / **"A Java Exception has occurred"** (a JVM-launcher dialog, before
   `main` runs — so the in-app Java-17 check never fires). Real deployments ship a **bundled JDK 21**
   (verified in prod: **Eclipse Adoptium 21**, shown in the app footer). Do not assume the target has Java.

# Architecture & Key Entry Points

- **Entry point:** `org.example.rainzubinringcounter.RainZubinRingCounterApplication#main`
  → checks the running JRE (`REQUIRED_JAVA_VERSION = 17`, shows a Swing error dialog and `System.exit(1)`
  if too old) → `Application.launch(RingCounter.class, ...)`.
- **JavaFX/Spring bridge:** `configuration/RingCounter` (a JavaFX `Application`) — `init()` boots the
  Spring context via `SpringApplicationBuilder`, installs `GlobalExceptionHandler` as the default
  uncaught-exception handler, then `start()` fires a `StageReadyEvent`.
- **UI bootstrap:** `configuration/StageInitializer` listens for `StageReadyEvent`, loads
  `resources/ringcounter.fxml` (Scene 1300×600) and shows the stage.
- **UI controller:** `RingCounterController` (`@Controller`) — owns the drag/drop circle, the two mutually
  exclusive checkboxes (UI labels **"Summarize files"** = `sumFile` = aggregate all dropped files into one
  report; **"Split files"** = `splitFile` = one report per file, named `ring_<original>.docx`;
  default/neither = all files combined into one `ring_All …` doc), text areas, and file chooser. The window
  title is **"Ring Counter"**; the footer shows `App Version` (from `application.properties`) and the running
  `Java` version+vendor. Also builds output `.docx`.
- **Document engine (parsing):** `RingReader` (`@Service`) — reads a `.docx` with POI XWPF, branches on
  tables vs. paragraphs, validates time-codes (`\d{2}[:;,.]\d{2}`) and character names, and returns a
  `ReaderResult`.
- **Pipelines:**
  - *Scan/extract:* `RingReader.reader(path, calculateTheSumOfRings)` → `dealWithRingsInTable` /
    `dealWithRingsInParagraph` → `validateRing`. **The `calculateTheSumOfRings` boolean is the "Summarize
    files" mode**, and it changes two things on purpose (a customer requirement, not a quirk): `true` keeps
    `sortedHashMap` **accumulating across all dropped files** (the running total) and **skips the per-character
    timecode list** (`nameToTimes`), because the summary doc wants totals only; `false` (default/Split modes)
    **resets per file** and **records each character's timecodes**. Don't "fix" this asymmetry.
  - *Export:* `RingCounterController.createDocument(...)` copies source paragraph/table formatting and
    writes a new `.docx` to `~/Documents/GeneratedDocs` (created on demand). File names are
    `ring_*` / `ring_All *` / `sum_All *` plus a timestamp.
- **Models / errors:** `Ring`, `ReaderResult` (Lombok `@Getter`); domain exceptions in `exception/`
  (`IncorrectFileFormatException`, `TheHeaderTableException`, messages in `ExceptionMessage`).

# Development Guardrails

## Document memory management (POI / streams / file locks)
- POI holds the **entire** `XWPFDocument` in memory; the "sum"/multi-file paths open many docs in a loop.
  Be deliberate about how many files are processed at once and free docs promptly.
- **Known leaks — do not copy the pattern:** `RingCounterController.isValidFile(...)` does
  `new XWPFDocument(new FileInputStream(file))` and never closes it (leaks a file handle every call, and
  the same file is validated repeatedly). `createDocument(...)` closes its streams manually at the end,
  so an exception mid-method leaks. When you touch this code, wrap POI/stream use in
  **try-with-resources** (as `RingReader.reader` already does) rather than manual `close()`.
- On Windows an un-closed `FileInputStream`/`XWPFDocument` keeps a **lock** on the source `.docx`, which
  breaks re-processing and user file operations — always release input streams before writing output.

## Code style & exception conventions
- Logging is **SLF4J via Lombok `@Slf4j`**. Prefer `log.*` over the `System.out.println` /
  `e.printStackTrace()` calls still scattered through the controller and `RingReader` — do not add new
  ones.
- User-facing errors go through `GlobalExceptionHandler.handleException(...)`, which shows a JavaFX
  `Alert` and **must** run on the FX thread (it already wraps in `Platform.runLater`). Never pop UI
  directly from a worker/POI thread.
- Lombok is a **provided/annotation-processor** dependency (excluded from the Spring Boot repackage and
  from the assembly manifest scope). Keep using `@Getter/@Setter/@Builder/@Slf4j`; don't hand-write
  boilerplate it would generate.

## Settled scope decisions (locked at kickoff — do not re-introduce)
- **Windows-only desktop EXE, by design.** Web was rejected to avoid server costs; Mac is unsupported (at
  best a raw `.jar` under Wine). Do not propose a web port, a server, or a Mac build.
- **No authentication / login / user accounts.** Deliberately dropped — it is a single local tool. Don't add
  password gates, licensing, or "admin vs user" roles.
- **Batch size ~30 files** is the customer's practical ceiling (POI loads each fully); larger batches are
  slow, not forbidden.

## Desktop application safety
- Runtime is **fully offline**. Do not add network calls, telemetry, update checks, or web dependencies.
- Output directory is hard-coded to `System.getProperty("user.home")/Documents/GeneratedDocs`. Treat all
  paths as local user paths; if you extend I/O, keep everything under the user's home and validate file
  extensions (only `.docx` is supported; non-Word input surfaces `IncorrectFileFormatException`).
- Tests are **fixture-dependent**: `RingReaderTest` reads real `.docx` files from the repo `Documents/`
  folder by relative path and asserts exact counts. Keep those fixtures in place; adjust assertions only
  with intent.

## Recurring tech debt (be aware, fix opportunistically — do not mass-refactor)
- `RingCounterController` does `new RingReader()` instead of injecting the `@Service`, bypassing Spring DI
  (so `RingReader` shared state is a plain field, not a managed singleton).
- `javafx-maven-plugin` in `pom.xml` has a **wrong `mainClass`** (`org.example.RainZubinRingCounter...`,
  bad package casing) — `mvn javafx:run` will not work as-configured; run the real main class instead.
- Env pin mismatch worth knowing: `pom.xml` targets **Java 21**, CI uses **Temurin 21**, but the runtime
  self-check only requires **Java 17**.

# Input Format Contract (what the parser demands of a `.docx`)

This app is used by an external dubbing studio; almost every support issue is an **input file that violates
an implicit contract**, not a code crash. Before changing the parser, know the rules it enforces (a shared
"rules doc" was given to the customer; these are the code-enforced parts):

- **`.docx` only.** `.doc` is silently unsupported (surfaces as `IncorrectFileFormatException`).
- **Two file shapes:** table-based files require the exact header row `ТАЙМ-КОД` / `ПЕРСОНАЖ` / `ТЕКСТ`
  (`ТАЙМ-КОД` **with the hyphen**); paragraph-based files have no header. Wrong/missing header on a table
  file → `TheHeaderTableException`.
- **The first ring is the anchor.** In the paragraph path, `dealWithRingsInParagraph` ignores everything
  until it sees the first line that is a valid `timecode + name`, then validates from there (`foundFirstRing`).
  If the first ring is malformed, detection is thrown off for the whole file — the team's written rules tell
  the studio the **first ring must be formatted perfectly**. Keep this invariant when editing the parser.
- **Multi-word character names** (e.g. `МАЛИЙ НАРУТО`) are kept whole only because a **TAB** separates the
  name from the reply; a parenthetical like `(тло)` is stripped (`name.split("(")[0]`). Missing TAB collapses
  or mis-splits the name — historically the "Малий …" merge complaints. The TAB rule below is load-bearing.
- **Time-code format is `MM:SS` only** — regex `\d{2}[:;,.]\d{2}` (separator `: ; , .`). **`HH:MM:SS`
  (e.g. `00:00:35`) is NOT accepted** and yields `The sentence does not have a time code`. This is the
  root cause of non-Naruto films (feature-length, `HH:MM:SS`) failing — a known, still-open limitation.
- **Character names must be ALL-CAPS** — regex `^[\p{Lu}\p{N}\-_/.;:,]+$`. Lowercase → `does not have a
  proper Name`. **Trailing digits ARE allowed** (`ЧОЛОВІК1`, `УЧЕНЬ2`) — that was an early bug, since fixed;
  do not re-forbid them.
- **Name and reply must be separated by a TAB**, not a newline. A reply starting on a new line →
  `does not have a TAB`.
- **One ring per table row**; two rings in one cell/row is unsupported. No empty cells / empty rows.
- **No page breaks** inside the source — a `розрив сторінки` corrupts pagination of the generated report
  (the "87 vs 89 series" two-column mess). Strikethrough/track-changes formatting also causes problems.

The customer has asked (open feature request, Jan–Feb 2026) to **relax** the fixed header words and the
all-caps name rule. Treat that as a deliberate design change — it touches core `RingReader` validation
(`isValidTimeCode`, `isValidName`, `dealWithRingsInTable`) — not a quick tweak.

# Known Production Issues (open as of this writing — customer-reported)

- **Word "recover unreadable content" prompt on every generated report.** Word shows
  *"виявлено непридатний для читання вміст … Відновити …?"* each time a `ring_*` / `sum_*` `.docx` is opened;
  the user must click **Так/Yes**. The POI output in `RingCounterController.createDocument` is not fully
  Word-clean (partial CTP/CTR copies, table `CTTbl` cloning). High-visibility annoyance for the studio's
  director — customer wants it to "just open".
- **HH:MM:SS time codes rejected** — see Input Format Contract. Blocks feature-length films.
- **`Index 0 out of bounds for length 0`** Alert on some files — unguarded `words[0]`/`words[1]` access in
  the paragraph path when a line splits to fewer tokens than expected.
- **EXE launch failures on fresh machines** (`JNI error` / `A Java Exception has occurred`) — see the
  Package-EXE guardrail above; mitigated by shipping a bundled JDK 21, not by app code.

# Scope note
Read-only inspection generated this file. Do not modify application source, the Launch4j config, or the
`Documents/` test fixtures unless a task explicitly asks.
