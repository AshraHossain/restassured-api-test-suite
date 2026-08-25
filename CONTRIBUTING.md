# Contributing to restassured-api-test-suite

`restassured-api-test-suite` follows the SuperClaude Framework project
structure used across this portfolio. Before making changes, read
[`PLANNING.md`](PLANNING.md) for the architecture (offline-first WireMock
design, module responsibilities, and key design constraints) and
[`TASK.md`](TASK.md) for the current priority list.

## Development Setup

**Prerequisites:** Java 17+, Maven 3.8+

```bash
# Compile test sources
mvn test-compile

# Run the full suite (offline, backed by a local WireMock server —
# no network access or API key required)
mvn test

# Run the smoke group only (CrudWorkflowTest + AuthTests)
mvn test -Dgroups=smoke

# Run against a different base URL (see PLANNING.md — the committed suite
# targets the local WireMock mock server by default)
mvn test -DBASE_URL=https://your-api.example.com/api
```

`mvn test` (via `testng.xml` / Surefire) is the current automated check —
treat a clean run as the minimum bar before opening a PR. The Extent HTML
report is written to `target/extent-reports/` after each run.

## Project Structure

| Path | Purpose |
|---|---|
| `src/test/java/com/apitest/base/BaseTest.java` | Shared `RequestSpecification`, config loading, Extent lifecycle hooks |
| `src/test/java/com/apitest/utils/ReqresMockServer.java` | WireMock stub server — extend this to add new mocked endpoints |
| `src/test/java/com/apitest/utils/TokenStore.java` | Thread-local bearer token cache |
| `src/test/java/com/apitest/tests/*.java` | Test classes (CRUD, schema, contract, chained, auth, negative/edge, XML) |
| `src/test/resources/schemas/` | JSON Schema (draft-07) and XSD contracts |
| `testng.xml` | Suite runner — new test classes must be added here to run |
| `plugins/` | Reserved extension point — see [`plugins/README.md`](plugins/README.md) |

See [`PLANNING.md`](PLANNING.md) for the authoritative architecture
reference — keep it (and `CLAUDE.md`) in sync with any structural change.

## Conventions

- **Offline-first** — new tests must go through `ReqresMockServer`'s
  WireMock stubs, not a live network call. Add a new `stubFor(...)` block
  in `ReqresMockServer.setupStubs()` rather than hitting `reqres.in`
  directly.
- **Register new test classes in `testng.xml`** — Surefire is wired to
  `testng.xml`, not default class discovery; a new test class needs its own
  `<test>` block (and a place in the `smoke` group's `<classes>` list if it
  belongs in the fast pass).
- **Distinct stub bodies per test** — key new create/update stubs on a
  request-body substring unique to the test class (e.g.
  `withRequestBody(containing("..."))`) so tests don't collide on shared
  mock state.
- Commit messages follow Conventional Commits, scoped to the project:
  `type(restassured-api-test-suite): description` (e.g.
  `feat(restassured-api-test-suite): add pagination boundary tests`).

## License

Proprietary — see [LICENSE](LICENSE). Contributions are accepted under the
same terms as the rest of the repository.
