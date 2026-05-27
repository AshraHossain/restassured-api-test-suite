# REST Assured API Automation Suite

![Build Status](https://github.com/AshraHossain/restassured-api-test-suite/actions/workflows/api-tests.yml/badge.svg)
![Java](https://img.shields.io/badge/Java-17-007396?logo=java)
![Rest Assured](https://img.shields.io/badge/Rest%20Assured-5.4-49A55E)
![TestNG](https://img.shields.io/badge/TestNG-7.9-FF6C37)
![License](https://img.shields.io/badge/License-MIT-blue)

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

## Running the Suite

**Prerequisites:** Java 17+, Maven 3.8+

```bash
# Clone
git clone https://github.com/AshraHossain/restassured-api-test-suite.git
cd restassured-api-test-suite

# Run full suite (uses reqres.in public sandbox — no key required)
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

Tests run against [reqres.in](https://reqres.in) — a free, public REST sandbox that requires no authentication setup. To point the suite at your own API, set the `BASE_URL` environment variable or update `src/test/resources/config/config.properties`.

---

## Author

**Ashra Hossain** — [github.com/AshraHossain](https://github.com/AshraHossain)
