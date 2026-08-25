# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Compile test sources (there is no src/main — this is a test-only module)
mvn test-compile

# Run the full suite (offline, backed by a local WireMock server —
# no network access or API key required)
mvn test

# Run the smoke group only (CrudWorkflowTest + AuthTests)
mvn test -Dgroups=smoke

# Run against a different base URL (see Architecture — the committed suite
# targets the local WireMock mock server by default)
mvn test -DBASE_URL=https://your-api.example.com/api
```

`mvn test` (via `testng.xml` / Surefire) is the current automated check — a
clean run is the smoke test. `mvn clean compile` legitimately reports "No
sources to compile" since there is no `src/main`; use `test-compile`/`test`
to exercise the actual code. The Extent HTML report is written to
`target/extent-reports/` after each run.

## Architecture

`restassured-api-test-suite` is an end-to-end API test framework (Rest
Assured 5 + TestNG 7, Java 17) covering CRUD workflows, JSON/XML schema
validation, contract checks, and chained-endpoint flows. It is
**offline-first**: `BaseTest` boots a local WireMock server
(`com.apitest.utils.ReqresMockServer`) on a dynamic port and stubs every
endpoint under test, so runs are deterministic without network access.

### Flow

```
mvn test
   ↓
Surefire ── testng.xml (Smoke test block + 7 full-regression test blocks)
   ↓
BaseTest (@BeforeSuite)
   loadConfig() ← src/test/resources/config/config.properties
   ReqresMockServer.start() ── WireMock, dynamicPort()
   RequestSpecBuilder → shared RequestSpecification (baseUri = mock server)
   ↓
Test classes (com.apitest.tests.*) ──uses──▶ UserPayload, TokenStore
   ↓
@BeforeMethod/@AfterMethod ── ExtentReportManager
   ↓
@AfterSuite ── flush report + ReqresMockServer.stop()
```

### Module responsibilities

| Module | Path | Purpose |
|---|---|---|
| Base test | `src/test/java/com/apitest/base/BaseTest.java` | Shared `RequestSpecification`, config loading, Extent lifecycle hooks, starts/stops the mock server. |
| Mock server | `src/test/java/com/apitest/utils/ReqresMockServer.java` | WireMock stubs for every endpoint under test — catch-alls first, then specific stubs (WireMock "last registered wins"). |
| Token store | `src/test/java/com/apitest/utils/TokenStore.java` | Thread-local cached bearer token, fetched once via `/login`. |
| Test classes | `src/test/java/com/apitest/tests/*.java` | `CrudWorkflowTest`, `SchemaValidationTest`, `ContractValidationTest`, `ChainedEndpointTest`, `AuthTests`, `NegativeEdgeCaseTest`, `XmlSchemaValidationTest`. |
| Schemas | `src/test/resources/schemas/` | JSON Schema (draft-07) and XSD contracts. |
| Suite runner | `testng.xml` | `smoke` group + one `<test>` block per test class — new classes must be registered here to run. |
| CI | `.github/workflows/api-tests.yml` | `mvn test` on push/PR; uploads Extent Report artifact. |

### Key design constraints

- **Java 17, Maven 3, `src/test`-only** — no `src/main`; a pure
  test-automation module.
- **WireMock-backed, not live-network** — new tests extend
  `ReqresMockServer.setupStubs()` rather than calling the real reqres.in.
- **Thread-local token cache is suite-scoped, not test-scoped** — call
  `TokenStore.invalidate()` explicitly rather than assuming a fresh token
  per test.
- **`testng.xml` drives Surefire** — new test classes need a `<test>` block
  (and a slot in the `smoke` group's `<classes>` list if they belong in the
  fast pass) to actually run.

## Framework conventions

This project follows the SuperClaude Framework structure adopted across the
portfolio:

- **`PLANNING.md`** is the source-of-truth architecture doc — keep it in
  sync with the Architecture section of this file when either changes.
- **`TASK.md`** holds the priority-ordered task list; check it before
  picking up new work.
- **`plugins/`** is the reserved extension point for a real-backend CI
  profile, alternate reporters, and contract-testing integrations — see
  `plugins/README.md`.
- **`CONTRIBUTING.md`** documents the development setup (`mvn test`,
  `mvn test -Dgroups=smoke`) for contributors.
