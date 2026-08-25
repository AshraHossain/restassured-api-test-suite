# plugins/

Reserved for SuperClaude Framework plugin extensions for this project (e.g.
real-backend CI profiles, alternate reporters, or contract-testing
integrations packaged as plugins).

No plugins are defined yet. `restassured-api-test-suite` currently runs
entirely offline against a local WireMock server
(`com.apitest.utils.ReqresMockServer`) rather than a live API — see
`PLANNING.md` for the mock-server architecture and stub layout. The
intended extension point is packaging a real-backend execution profile
(pointing at an actual API via the `BASE_URL`/`API_KEY` env vars
`config.properties` already documents), an alternate report format (Allure),
and CI notification integrations as installable plugins under this
directory.
