package com.apitest.tests;

import com.apitest.base.BaseTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * NegativeEdgeCaseTest
 *
 * Validates API behaviour for invalid inputs, boundary values,
 * and unexpected conditions.
 *
 * Patterns demonstrated:
 *   - @DataProvider for parameterised negative tests
 *   - Boundary value analysis on numeric path params
 *   - Malformed / empty / oversized payloads
 *   - Unsupported HTTP method → 405
 */
public class NegativeEdgeCaseTest extends BaseTest {

    // ── 404 — non-existent resources ─────────────────────────────────────────

    @DataProvider(name = "nonExistentUserIds")
    public Object[][] nonExistentUserIds() {
        return new Object[][] {
            { 9999  },    // far out of range
            { 100   },    // just above seeded range
            { 99999 }     // extreme upper bound
        };
    }

    @Test(dataProvider = "nonExistentUserIds",
          groups = {"smoke", "negative"},
          description = "GET /users/{id} — non-existent id returns 404 with empty body")
    public void testNonExistentUserReturns404(int userId) {
        given()
                .spec(requestSpec)
                .when()
                .get("/users/" + userId)
                .then()
                .statusCode(404)
                .body("$", anEmptyMap());     // reqres returns {} for 404
    }

    // ── 400 — malformed payloads ──────────────────────────────────────────────

    @Test(groups = "negative",
          description = "POST /register — empty JSON body returns 400")
    public void testRegisterEmptyBodyReturns400() {
        given()
                .spec(requestSpec)
                .body("{}")
                .when()
                .post("/register")
                .then()
                .statusCode(400)
                .body("error", notNullValue());
    }

    @Test(groups = "negative",
          description = "POST /login — completely empty string body returns 400")
    public void testLoginEmptyStringBodyReturns400() {
        given()
                .spec(requestSpec)
                .contentType("application/json")
                .body("")
                .when()
                .post("/login")
                .then()
                .statusCode(in(java.util.List.of(400, 415)));
                // 400 Bad Request or 415 Unsupported Media Type are both valid
    }

    // ── Boundary values ───────────────────────────────────────────────────────

    @DataProvider(name = "boundaryPageNumbers")
    public Object[][] boundaryPageNumbers() {
        return new Object[][] {
            { 0,    200 },   // page 0 — reqres treats as page 1
            { 1,    200 },   // first valid page
            { 9999, 200 }    // beyond last page — returns empty data list
        };
    }

    @Test(dataProvider = "boundaryPageNumbers",
          groups = "negative",
          description = "GET /users?page={n} — boundary page numbers do not cause 5xx")
    public void testPaginationBoundary(int page, int expectedStatus) {
        given()
                .spec(requestSpec)
                .queryParam("page", page)
                .when()
                .get("/users")
                .then()
                .statusCode(expectedStatus)
                .body("page", notNullValue());
    }

    // ── Query param injection guard ───────────────────────────────────────────

    @Test(groups = "negative",
          description = "GET /users with SQL-like page param — API handles gracefully, no 5xx")
    public void testSqlInjectionInQueryParam() {
        given()
                .spec(requestSpec)
                .queryParam("page", "1 OR 1=1")
                .when()
                .get("/users")
                .then()
                .statusCode(in(java.util.List.of(200, 400)));
                // must NOT be 500 — server should not crash
    }

    // ── Unsupported methods ───────────────────────────────────────────────────

    @Test(groups = "negative",
          description = "DELETE /users without id — method not allowed or bad request")
    public void testDeleteWithoutIdIsRejected() {
        given()
                .spec(requestSpec)
                .when()
                .delete("/users")          // no id segment
                .then()
                .statusCode(in(java.util.List.of(404, 405)));
    }

    // ── Large payload ─────────────────────────────────────────────────────────

    @Test(groups = "negative",
          description = "POST /users — 1 MB name field does not cause 500")
    public void testOversizedPayloadHandledGracefully() {
        String hugeValue = "A".repeat(1_048_576);   // 1 MB
        given()
                .spec(requestSpec)
                .body("{\"name\":\"" + hugeValue + "\",\"job\":\"QA\"}")
                .when()
                .post("/users")
                .then()
                .statusCode(in(java.util.List.of(200, 201, 400, 413)));
                // must NOT be 500
    }
}
