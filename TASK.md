# TASK.md — restassured-api-test-suite

Priority task list. See `PLANNING.md` for architecture context.

## High priority

- [ ] **Fix stale MIT license badge in README** — `README.md` displays
  `![License](https://img.shields.io/badge/License-MIT-blue)` but no
  `LICENSE` file exists in the repo (and the project is proprietary per
  the portfolio-wide licensing convention). Update the badge to match the
  new `LICENSE` file.
- [ ] **Add a real-backend CI path** — `config.properties` documents
  `BASE_URL`/`API_KEY` as CI-overridable, but the suite as committed always
  targets the local WireMock mock server (`ReqresMockServer`); there is no
  job/profile that actually exercises a live backend.

## Medium priority

- [ ] **Parallelize `testng.xml`** — `<suite ... parallel="none">`; the
  suite runs fully sequential today.
- [ ] **Decide on `.github/modernize/java-upgrade/`** — contains hook
  scripts (`recordToolUse.ps1`/`.sh`) not referenced by
  `.github/workflows/api-tests.yml`; either wire it into CI or remove it as
  unused scaffolding.
- [ ] **`.venv/` is untracked but not gitignored** — `git status` shows an
  untracked `.venv/` directory; `.gitignore` has no Python-specific entries.
  Confirm whether this project needs a Python venv at all, and if not,
  remove the directory; if it does, add `.venv/` to `.gitignore`.

## Low priority / infra

- [x] Rest Assured 5.4 + TestNG 7.9 on Java 17, Maven 3 build.
- [x] Offline-first: WireMock (`ReqresMockServer`) stubs every endpoint
  under test — no live network dependency.
- [x] JSON Schema (draft-07) and XML/XSD contract validation.
- [x] Extent Reports 5 HTML output (`target/extent-reports/`).
- [x] GitHub Actions CI (`.github/workflows/api-tests.yml`) — `mvn test`
  on push/PR, Extent Report uploaded as artifact.
- [x] Thread-local bearer token cache (`TokenStore`) for chained/auth tests.
