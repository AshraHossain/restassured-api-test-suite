package com.apitest.utils;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

/**
 * ReqresMockServer
 *
 * Starts a local WireMock server on a dynamic port and sets up stubs
 * to mimic the classic reqres.in API endpoints to support offline-first
 * and fast test suite runs.
 */
public class ReqresMockServer {

    private static WireMockServer server;

    public static void start() {
        if (server == null) {
            server = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
            server.start();
            configureFor("localhost", server.port());
            setupStubs();
        }
    }

    public static void stop() {
        if (server != null) {
            server.stop();
            server = null;
        }
    }

    public static String getBaseUrl() {
        if (server == null) {
            start();
        }
        return "http://localhost:" + server.port() + "/api";
    }

    private static void setupStubs() {
        // ── CATCH-ALL stubs (registered first = lowest priority in WireMock) ──
        // WireMock "last registered wins": catch-alls go first so specific stubs
        // registered below always override them.

        // POST /api/users catch-all
        stubFor(post(urlEqualTo("/api/users"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json; charset=utf-8")
                        .withBody("{\n" +
                                "  \"name\": \"QA User\",\n" +
                                "  \"job\": \"QA\",\n" +
                                "  \"id\": \"999\",\n" +
                                "  \"createdAt\": \"2026-05-27T00:00:00.000Z\"\n" +
                                "}")));

        // GET /api/users catch-all (no page param) — needed for bearer token test
        stubFor(get(urlPathEqualTo("/api/users"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json; charset=utf-8")
                        .withBody("{\n" +
                                "  \"page\": 1,\n" +
                                "  \"per_page\": 6,\n" +
                                "  \"total\": 12,\n" +
                                "  \"total_pages\": 2,\n" +
                                "  \"data\": [\n" +
                                "    {\n" +
                                "      \"id\": 1,\n" +
                                "      \"email\": \"george.bluth@reqres.in\",\n" +
                                "      \"first_name\": \"George\",\n" +
                                "      \"last_name\": \"Bluth\",\n" +
                                "      \"avatar\": \"https://reqres.in/img/faces/1-image.jpg\"\n" +
                                "    },\n" +
                                "    {\n" +
                                "      \"id\": 2,\n" +
                                "      \"email\": \"janet.weaver@reqres.in\",\n" +
                                "      \"first_name\": \"Janet\",\n" +
                                "      \"last_name\": \"Weaver\",\n" +
                                "      \"avatar\": \"https://reqres.in/img/faces/2-image.jpg\"\n" +
                                "    }\n" +
                                "  ],\n" +
                                "  \"support\": {\n" +
                                "    \"url\": \"https://reqres.in/#support-heading\",\n" +
                                "    \"text\": \"To keep ReqRes free, contributions are appreciated!\"\n" +
                                "  }\n" +
                                "}")));

        // POST /api/login catch-all — handles empty/unrecognised bodies (400)
        stubFor(post(urlEqualTo("/api/login"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json; charset=utf-8")
                        .withBody("{\n" +
                                "  \"error\": \"Missing email or username\"\n" +
                                "}")));

        // ── SPECIFIC stubs (registered after = higher priority) ───────────
        // ── 1. GET /api/users/2 (Valid Single User) ───────────────────────
        stubFor(get(urlEqualTo("/api/users/2"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json; charset=utf-8")
                        .withBody("{\n" +
                                "  \"data\": {\n" +
                                "    \"id\": 2,\n" +
                                "    \"email\": \"janet.weaver@reqres.in\",\n" +
                                "    \"first_name\": \"Janet\",\n" +
                                "    \"last_name\": \"Weaver\",\n" +
                                "    \"avatar\": \"https://reqres.in/img/faces/2-image.jpg\"\n" +
                                "  },\n" +
                                "  \"support\": {\n" +
                                "    \"url\": \"https://reqres.in/#support-heading\",\n" +
                                "    \"text\": \"To keep ReqRes free, contributions are appreciated!\"\n" +
                                "  }\n" +
                                "}")));

        // ── 2. GET /api/users (Paginated User List) ──────────────────────
        // Page 1
        stubFor(get(urlPathEqualTo("/api/users"))
                .withQueryParam("page", equalTo("1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json; charset=utf-8")
                        .withBody("{\n" +
                                "  \"page\": 1,\n" +
                                "  \"per_page\": 6,\n" +
                                "  \"total\": 12,\n" +
                                "  \"total_pages\": 2,\n" +
                                "  \"data\": [\n" +
                                "    {\n" +
                                "      \"id\": 1,\n" +
                                "      \"email\": \"george.bluth@reqres.in\",\n" +
                                "      \"first_name\": \"George\",\n" +
                                "      \"last_name\": \"Bluth\",\n" +
                                "      \"avatar\": \"https://reqres.in/img/faces/1-image.jpg\"\n" +
                                "    },\n" +
                                "    {\n" +
                                "      \"id\": 2,\n" +
                                "      \"email\": \"janet.weaver@reqres.in\",\n" +
                                "      \"first_name\": \"Janet\",\n" +
                                "      \"last_name\": \"Weaver\",\n" +
                                "      \"avatar\": \"https://reqres.in/img/faces/2-image.jpg\"\n" +
                                "    }\n" +
                                "  ],\n" +
                                "  \"support\": {\n" +
                                "    \"url\": \"https://reqres.in/#support-heading\",\n" +
                                "    \"text\": \"To keep ReqRes free, contributions are appreciated!\"\n" +
                                "  }\n" +
                                "}")));

        // Page 2
        stubFor(get(urlPathEqualTo("/api/users"))
                .withQueryParam("page", equalTo("2"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json; charset=utf-8")
                        .withBody("{\n" +
                                "  \"page\": 2,\n" +
                                "  \"per_page\": 6,\n" +
                                "  \"total\": 12,\n" +
                                "  \"total_pages\": 2,\n" +
                                "  \"data\": [\n" +
                                "    {\n" +
                                "      \"id\": 7,\n" +
                                "      \"email\": \"michael.lawson@reqres.in\",\n" +
                                "      \"first_name\": \"Michael\",\n" +
                                "      \"last_name\": \"Lawson\",\n" +
                                "      \"avatar\": \"https://reqres.in/img/faces/7-image.jpg\"\n" +
                                "    },\n" +
                                "    {\n" +
                                "      \"id\": 8,\n" +
                                "      \"email\": \"lindsay.ferguson@reqres.in\",\n" +
                                "      \"first_name\": \"Lindsay\",\n" +
                                "      \"last_name\": \"Ferguson\",\n" +
                                "      \"avatar\": \"https://reqres.in/img/faces/8-image.jpg\"\n" +
                                "    }\n" +
                                "  ],\n" +
                                "  \"support\": {\n" +
                                "    \"url\": \"https://reqres.in/#support-heading\",\n" +
                                "    \"text\": \"To keep ReqRes free, contributions are appreciated!\"\n" +
                                "  }\n" +
                                "}")));

        // ── 3. POST /api/users (Create User) ──────────────────────────────
        // CrudWorkflowTest
        stubFor(post(urlEqualTo("/api/users"))
                .withRequestBody(containing("Ashra Hossain"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json; charset=utf-8")
                        .withBody("{\n" +
                                "  \"name\": \"Ashra Hossain\",\n" +
                                "  \"job\": \"QA Engineer\",\n" +
                                "  \"id\": \"123\",\n" +
                                "  \"createdAt\": \"2026-05-27T00:00:00.000Z\"\n" +
                                "}")));

        // ChainedEndpointTest - Create Then Read
        stubFor(post(urlEqualTo("/api/users"))
                .withRequestBody(containing("Chain Test User"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json; charset=utf-8")
                        .withBody("{\n" +
                                "  \"name\": \"Chain Test User\",\n" +
                                "  \"job\": \"Automation Engineer\",\n" +
                                "  \"id\": \"124\",\n" +
                                "  \"createdAt\": \"2026-05-27T00:00:00.000Z\"\n" +
                                "}")));

        // ChainedEndpointTest - Create Then Update
        stubFor(post(urlEqualTo("/api/users"))
                .withRequestBody(containing("Before Update"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json; charset=utf-8")
                        .withBody("{\n" +
                                "  \"name\": \"Before Update\",\n" +
                                "  \"job\": \"Junior QA\",\n" +
                                "  \"id\": \"125\",\n" +
                                "  \"createdAt\": \"2026-05-27T00:00:00.000Z\"\n" +
                                "}")));

        // SchemaValidationTest - Created User
        stubFor(post(urlEqualTo("/api/users"))
                .withRequestBody(containing("Schema Tester"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json; charset=utf-8")
                        .withBody("{\n" +
                                "  \"name\": \"Schema Tester\",\n" +
                                "  \"job\": \"QA\",\n" +
                                "  \"id\": \"126\",\n" +
                                "  \"createdAt\": \"2026-05-27T00:00:00.000Z\"\n" +
                                "}")));

        // ContractValidationTest - Created User response contract
        stubFor(post(urlEqualTo("/api/users"))
                .withRequestBody(containing("Contract Test"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json; charset=utf-8")
                        .withBody("{\n" +
                                "  \"name\": \"Contract Test\",\n" +
                                "  \"job\": \"QA\",\n" +
                                "  \"id\": \"127\",\n" +
                                "  \"createdAt\": \"2026-05-27T00:00:00.000Z\"\n" +
                                "}")));

        // ── 4. PUT /api/users/2 (Update User PUT) ──────────────────────────
        // CrudWorkflowTest PUT
        stubFor(put(urlEqualTo("/api/users/2"))
                .withRequestBody(containing("Ashra H"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json; charset=utf-8")
                        .withBody("{\n" +
                                "  \"name\": \"Ashra H\",\n" +
                                "  \"job\": \"Senior QA Engineer\",\n" +
                                "  \"updatedAt\": \"2026-05-27T00:00:00.000Z\"\n" +
                                "}")));

        // ChainedEndpointTest PUT
        stubFor(put(urlEqualTo("/api/users/2"))
                .withRequestBody(containing("After Update"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json; charset=utf-8")
                        .withBody("{\n" +
                                "  \"name\": \"After Update\",\n" +
                                "  \"job\": \"Senior QA\",\n" +
                                "  \"updatedAt\": \"2026-05-27T00:00:00.000Z\"\n" +
                                "}")));

        // ── 5. PATCH /api/users/2 (Update User PATCH) ──────────────────────
        stubFor(patch(urlEqualTo("/api/users/2"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json; charset=utf-8")
                        .withBody("{\n" +
                                "  \"job\": \"Lead QA\",\n" +
                                "  \"updatedAt\": \"2026-05-27T00:00:00.000Z\"\n" +
                                "}")));

        // ── 6. DELETE /api/users/2 (Delete User) ──────────────────────────
        stubFor(delete(urlEqualTo("/api/users/2"))
                .willReturn(aResponse()
                        .withStatus(204)));

        // ── 7. DELETE /api/users (Edge: Delete without ID) ─────────────────
        stubFor(delete(urlEqualTo("/api/users"))
                .willReturn(aResponse()
                        .withStatus(405)
                        .withHeader("Content-Type", "application/json; charset=utf-8")
                        .withBody("{\"error\": \"Method not allowed\"}")));

        // ── 8. POST /api/register (Successful) ───────────────────────────
        stubFor(post(urlEqualTo("/api/register"))
                .withRequestBody(containing("eve.holt@reqres.in"))
                .withRequestBody(containing("pistol"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json; charset=utf-8")
                        .withBody("{\n" +
                                "  \"id\": 4,\n" +
                                "  \"token\": \"QpwL5tke4Pnpja7X4\"\n" +
                                "}")));

        // Undefined User Register
        stubFor(post(urlEqualTo("/api/register"))
                .withRequestBody(containing("sydney@fife.com"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json; charset=utf-8")
                        .withBody("{\n" +
                                "  \"error\": \"Note: Only defined users succeed registration\"\n" +
                                "}")));

        // Empty body Register
        stubFor(post(urlEqualTo("/api/register"))
                .withRequestBody(equalTo("{}"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json; charset=utf-8")
                        .withBody("{\n" +
                                "  \"error\": \"Missing email or username\"\n" +
                                "}")));

        // ── 9. POST /api/login (Successful) ──────────────────────────────
        stubFor(post(urlEqualTo("/api/login"))
                .withRequestBody(containing("eve.holt@reqres.in"))
                .withRequestBody(containing("cityslicka"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json; charset=utf-8")
                        .withBody("{\n" +
                                "  \"token\": \"QpwL5tke4Pnpja7X4\"\n" +
                                "}")));

        // Missing Password Login
        stubFor(post(urlEqualTo("/api/login"))
                .withRequestBody(containing("peter@klaven.com"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json; charset=utf-8")
                        .withBody("{\n" +
                                "  \"error\": \"Missing password\"\n" +
                                "}")));

        // Missing Email Login
        stubFor(post(urlEqualTo("/api/login"))
                .withRequestBody(containing("hunter2"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json; charset=utf-8")
                        .withBody("{\n" +
                                "  \"error\": \"Missing email or username\"\n" +
                                "}")));

        // ── 10. GET /api/users/{nonExistentId} ─────────────────────────────
        stubFor(get(urlPathMatching("/api/users/(9999|100|99999)"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json; charset=utf-8")
                        .withBody("{}")));

        // ── 11. GET /api/xml/users/2 (Single User XML) ───────────────────────
        stubFor(get(urlEqualTo("/api/xml/users/2"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/xml; charset=utf-8")
                        .withBody("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                "<user>\n" +
                                "  <data>\n" +
                                "    <id>2</id>\n" +
                                "    <email>janet.weaver@reqres.in</email>\n" +
                                "    <first_name>Janet</first_name>\n" +
                                "    <last_name>Weaver</last_name>\n" +
                                "    <avatar>https://reqres.in/img/faces/2-image.jpg</avatar>\n" +
                                "  </data>\n" +
                                "  <support>\n" +
                                "    <url>https://reqres.in/#support-heading</url>\n" +
                                "    <text>To keep ReqRes free, contributions are appreciated!</text>\n" +
                                "  </support>\n" +
                                "</user>")));

        // ── 12. GET /api/xml/users?page=1 (User List XML) ────────────────────
        stubFor(get(urlPathEqualTo("/api/xml/users"))
                .withQueryParam("page", equalTo("1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/xml; charset=utf-8")
                        .withBody("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                "<users>\n" +
                                "  <page>1</page>\n" +
                                "  <per_page>6</per_page>\n" +
                                "  <total>12</total>\n" +
                                "  <total_pages>2</total_pages>\n" +
                                "  <data>\n" +
                                "    <user>\n" +
                                "      <id>1</id>\n" +
                                "      <email>george.bluth@reqres.in</email>\n" +
                                "      <first_name>George</first_name>\n" +
                                "      <last_name>Bluth</last_name>\n" +
                                "      <avatar>https://reqres.in/img/faces/1-image.jpg</avatar>\n" +
                                "    </user>\n" +
                                "    <user>\n" +
                                "      <id>2</id>\n" +
                                "      <email>janet.weaver@reqres.in</email>\n" +
                                "      <first_name>Janet</first_name>\n" +
                                "      <last_name>Weaver</last_name>\n" +
                                "      <avatar>https://reqres.in/img/faces/2-image.jpg</avatar>\n" +
                                "    </user>\n" +
                                "  </data>\n" +
                                "  <support>\n" +
                                "    <url>https://reqres.in/#support-heading</url>\n" +
                                "    <text>To keep ReqRes free, contributions are appreciated!</text>\n" +
                                "  </support>\n" +
                                "</users>")));

        // ── 13. GET /api/users Boundary values & injection stubs ───────────
        // Page 0
        stubFor(get(urlPathEqualTo("/api/users"))
                .withQueryParam("page", equalTo("0"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json; charset=utf-8")
                        .withBody("{\n" +
                                "  \"page\": 1,\n" +
                                "  \"per_page\": 6,\n" +
                                "  \"total\": 12,\n" +
                                "  \"total_pages\": 2,\n" +
                                "  \"data\": [],\n" +
                                "  \"support\": {\n" +
                                "    \"url\": \"https://reqres.in/#support-heading\",\n" +
                                "    \"text\": \"To keep ReqRes free, contributions are appreciated!\"\n" +
                                "  }\n" +
                                "}")));

        // Page 9999
        stubFor(get(urlPathEqualTo("/api/users"))
                .withQueryParam("page", equalTo("9999"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json; charset=utf-8")
                        .withBody("{\n" +
                                "  \"page\": 9999,\n" +
                                "  \"per_page\": 6,\n" +
                                "  \"total\": 12,\n" +
                                "  \"total_pages\": 2,\n" +
                                "  \"data\": [],\n" +
                                "  \"support\": {\n" +
                                "    \"url\": \"https://reqres.in/#support-heading\",\n" +
                                "    \"text\": \"To keep ReqRes free, contributions are appreciated!\"\n" +
                                "  }\n" +
                                "}")));

        // Page SQL Injection
        stubFor(get(urlPathEqualTo("/api/users"))
                .withQueryParam("page", equalTo("1 OR 1=1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json; charset=utf-8")
                        .withBody("{\n" +
                                "  \"page\": 1,\n" +
                                "  \"per_page\": 6,\n" +
                                "  \"total\": 12,\n" +
                                "  \"total_pages\": 2,\n" +
                                "  \"data\": [],\n" +
                                "  \"support\": {\n" +
                                "    \"url\": \"https://reqres.in/#support-heading\",\n" +
                                "    \"text\": \"To keep ReqRes free, contributions are appreciated!\"\n" +
                                "  }\n" +
                                "}")));
    }
}
