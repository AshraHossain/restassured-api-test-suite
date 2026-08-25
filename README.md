# REST Assured API Automation Suite

![Build Status](https://github.com/AshraHossain/restassured-api-test-suite/actions/workflows/api-tests.yml/badge.svg)
![Java](https://img.shields.io/badge/Java-17-007396?logo=java)
![Rest Assured](https://img.shields.io/badge/Rest%20Assured-5.4-49A55E)
![TestNG](https://img.shields.io/badge/TestNG-7.9-FF6C37)
![License](https://img.shields.io/badge/License-Proprietary-red)

End-to-end API test framework built with **Rest Assured 5** and **TestNG 7**. Covers CRUD workflows, JSON/XML schema assertions, consumer-driven contract validation, and multi-step chained endpoint flows — wired into **GitHub Actions** for continuous regression on every push and pull request.

---

## Features

| Capability | Detail |
|---|---|
| CRUD workflow tests | Full create → read → update → delete lifecycle with chained IDs |
| JSON Schema validation | `matchesJsonSchemaInClasspath()` against draft-07 schemas |
| XML / XPath assertions | `hasXPath()` for XML endpoint contracts |
| Contract validation | Required fields, types, forbidden keys (e.g. `password`) |
| Chained endpoint tests | Auth → profile → order multi-step flows with token propagation |
| Auth & headers | Bearer, Basic Auth, custom headers; 401/400 negative scenarios |
| Negative / edge cases | `@DataProvider`-driven 404s, boundary values, oversized payloads |
| CI pipeline | GitHub Actions on push + PR; Extent Report uploaded as artifact |

---

## Tech Stack

- **Java 17** — language
- **Maven 3** — build and dependency management
- **Rest Assured 5.4** — HTTP DSL and response assertions
- **TestNG 7.9** — test runner with `@DataProvider`, groups, priorities
- **json-schema-validator** — JSON Schema draft-07 contract enforcement
- **Extent Reports 5** — rich HTML test report
- **GitHub Actions** — CI on every push and PR

---

## Project Structure

```
restassured-api-test-suite/
├── .github/
│   └── workflows/
│       └── api-tests.yml            # CI pipeline
├── src/
│   └── test/
│       ├── java/com/apitest/
│       │   ├── base/
│       │   │   └── BaseTest.java            # RequestSpec, Extent hooks
│       │   ├── tests/
│       │   │   ├── CrudWorkflowTest.java
│       │   │   ├── SchemaValidationTest.java
│       │   │   ├── ContractValidationTest.java
│       │   │   ├── ChainedEndpointTest.java
│       │   │   ├── AuthTests.java
│       │   │   └── NegativeEdgeCaseTest.java
│       │   ├── payloads/
│       │   │   └── UserPayload.java         # Request body builders
│       │   └── utils/
│       │       ├── TokenStore.java          # Thread-local token cache
│       │       └── ExtentReportManager.java # Singleton report instance
│       └── resources/
│           ├── schemas/json/                # JSON Schema files
│           └── config/config.properties    # Base URL, timeouts
├── testng.xml                               # Suite runner
└── pom.xml                                  # Dependencies & Surefire config
```

---

## Framework

`restassured-api-test-suite` follows the SuperClaude Framework project
structure:

- [`PLANNING.md`](PLANNING.md) — architecture, module responsibilities, and design constraints (source of truth, kept in sync with `CLAUDE.md`)
- [`TASK.md`](TASK.md) — priority-ordered task list
- [`plugins/`](plugins/README.md) — reserved extension point for a real-backend CI profile, alternate reporters, and contract-testing integrations
- [`CONTRIBUTING.md`](CONTRIBUTING.md) — development setup and contribution workflow

## Getting Started

```bash
mvn test
```

## Running the Suite

**Prerequisites:** Java 17+, Maven 3.8+

```bash
# Clone
git clone https://github.com/AshraHossain/restassured-api-test-suite.git
cd restassured-api-test-suite

# Run full suite (offline — backed by a local WireMock mock server, no
# network access or API key required; see PLANNING.md)
mvn test

# Run smoke group only
mvn test -Dgroups=smoke

# Run against a different base URL
mvn test -DBASE_URL=https://your-api.example.com/api
```

The Extent HTML report is written to `target/extent-reports/` after each run.

---

## CI Pipeline

Every push to `main` or `develop`, and every pull request targeting `main`, triggers the full regression suite via GitHub Actions.

```
push / PR → checkout → setup JDK 17 → mvn test → upload Extent Report artifact
```

Report artifacts are retained for 30 days and visible under **Actions → workflow run → Artifacts**.

---

## Target API

The suite is **offline-first**: `BaseTest` boots a local WireMock server
(`com.apitest.utils.ReqresMockServer`) on a dynamic port and stubs the
endpoints under test to mimic the classic [reqres.in](https://reqres.in)
user API — no network access or API key is required to run `mvn test`.
`config.properties` documents `BASE_URL`/`API_KEY` as CI-overridable for
pointing the suite at a real backend instead; see `PLANNING.md` for the
mock-server architecture and `TASK.md` for the status of a real-backend CI
path.

---

## Architecture

See [`PLANNING.md`](PLANNING.md) for the full architecture writeup (module
responsibilities, offline-first WireMock design, key design constraints)
and [`CLAUDE.md`](CLAUDE.md) for the same content oriented at Claude Code
sessions.

## Contributing

See [`CONTRIBUTING.md`](CONTRIBUTING.md) for development setup and workflow.

## License

Proprietary. See [LICENSE](LICENSE). All rights reserved.

## Author

**Ashra Hossain** — [github.com/AshraHossain](https://github.com/AshraHossain)
