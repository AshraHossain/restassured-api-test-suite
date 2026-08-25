# PLANNING.md — restassured-api-test-suite

## What this is

`restassured-api-test-suite` is an end-to-end API test framework built with
Rest Assured 5 and TestNG 7. It exercises CRUD workflows, JSON/XML schema
validation, consumer-driven contract checks, and multi-step chained-endpoint
flows against a reqres.in-style user API, wired into GitHub Actions for
regression on every push and pull request.

The suite is **offline-first**: rather than hitting the real reqres.in
service, `BaseTest` boots a local WireMock server
(`com.apitest.utils.ReqresMockServer`) on a dynamic port and stubs every
endpoint the tests exercise, so the suite runs deterministically without
network access or rate limits.

## Architecture

```
mvn test
   │
   ▼
Surefire ── testng.xml (suite runner: Smoke + 7 full-regression <test> blocks)
   │
   ▼
BaseTest (@BeforeSuite)
   │  loadConfig() ← src/test/resources/config/config.properties
   │  ReqresMockServer.start() ── WireMock, dynamicPort()
   │  RequestSpecBuilder → shared RequestSpecification (baseUri = mock server)
   ▼
Test classes (com.apitest.tests.*) ──uses──▶ UserPayload (request bodies)
   │                                    TokenStore (thread-local bearer token)
   ▼
@BeforeMethod/@AfterMethod ── ExtentReportManager (per-test Extent node)
   │
   ▼
@AfterSuite ── ExtentReportManager.flush() + ReqresMockServer.stop()
   │
   ▼
target/extent-reports/ (HTML report, uploaded as CI artifact)
```

### Module responsibilities

| Module | Path | Purpose |
|---|---|---|
| Base test | `src/test/java/com/apitest/base/BaseTest.java` | Shared `RequestSpecification`, config loading, Extent test lifecycle hooks (`@BeforeSuite`/`@BeforeMethod`/`@AfterMethod`/`@AfterSuite`), starts/stops the mock server. |
| Mock server | `src/test/java/com/apitest/utils/ReqresMockServer.java` | WireMock server on a dynamic port; stubs every endpoint under test (`/api/users`, `/api/login`, `/api/register`, `/api/xml/users`, etc.), including negative/edge-case and boundary-value responses. |
| Token store | `src/test/java/com/apitest/utils/TokenStore.java` | Thread-local cached bearer token; fetches once via `/login` and reuses across chained/auth tests. |
| Report manager | `src/test/java/com/apitest/utils/ExtentReportManager.java` | Singleton Extent Reports instance. |
| Payload builder | `src/test/java/com/apitest/payloads/UserPayload.java` | Request body construction for user create/update calls. |
| Test classes | `src/test/java/com/apitest/tests/*.java` | `CrudWorkflowTest`, `SchemaValidationTest`, `ContractValidationTest`, `ChainedEndpointTest`, `AuthTests`, `NegativeEdgeCaseTest`, `XmlSchemaValidationTest`. |
| Schemas | `src/test/resources/schemas/json/*.json`, `src/test/resources/schemas/xml/*.xsd` | JSON Schema (draft-07) and XSD contracts validated via `json-schema-validator` / `hasXPath()`. |
| Suite runner | `testng.xml` | Defines the `smoke` group (fast PR sanity pass: `CrudWorkflowTest` + `AuthTests`) plus one `<test>` block per test class for full regression. |
| CI | `.github/workflows/api-tests.yml` | `mvn test` on push/PR; uploads the Extent Report as a build artifact. |

### Deterministic, offline test data

- `ReqresMockServer` registers WireMock stubs in two tiers: broad
  catch-alls first (so a request that matches nothing specific still gets a
  sane default), then specific stubs keyed on request body content
  (`withRequestBody(containing(...))`) or query params — WireMock's
  "last registered wins" rule means the specific stubs, registered second,
  take priority.
- Each test class that needs a distinct created-user response registers its
  own `containing(...)` stub (e.g. `"Chain Test User"` for
  `ChainedEndpointTest`, `"Schema Tester"` for `SchemaValidationTest`) so
  tests don't collide on shared mock state.
- `config.properties` documents that `BASE_URL`/`API_KEY` are meant to be
  overridden via CI env vars/secrets for a real-backend run, but the suite
  as committed always talks to the local WireMock instance via
  `ReqresMockServer.getBaseUrl()`.

## Key design constraints

- **Java 17, Maven 3, `src/test`-only** — there is no `src/main`; this is a
  pure test-automation module, not a library. `mvn compile` legitimately
  reports "No sources to compile" — use `mvn test-compile` or `mvn test` to
  exercise the actual code.
- **WireMock-backed, not live-network** — do not add tests that call the
  real `reqres.in` directly; extend `ReqresMockServer.setupStubs()` instead
  so runs stay deterministic and offline.
- **Thread-local token cache is suite-scoped, not test-scoped** —
  `TokenStore` fetches once per thread and reuses; call `invalidate()`
  explicitly after a 401 or at teardown rather than assuming a fresh token
  per test.
- **`testng.xml` drives Surefire** — `pom.xml`'s `maven-surefire-plugin` is
  wired to `testng.xml`, not Surefire's default class discovery; new test
  classes must be added to a `<test>` block (and to the `smoke` group's
  `<classes>` list if they belong in the fast pass) to actually run.

## Next steps (not yet done)

- Add a real-backend CI job/profile that points at an actual API (using the
  `BASE_URL`/`API_KEY` env vars `config.properties` already documents)
  alongside the offline WireMock-backed default.
- Parallelize the TestNG suite (`testng.xml` currently sets
  `parallel="none"`).
- `.github/modernize/java-upgrade/` contains Java-upgrade tooling scaffolding
  not yet wired into a workflow — evaluate whether to adopt or remove it.
