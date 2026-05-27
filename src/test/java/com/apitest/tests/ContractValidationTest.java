package com.apitest.tests;

import com.apitest.base.BaseTest;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * ContractValidationTest
 *
 * Pact-style consumer-driven contract tests that enforce the agreed
 * "shape" of API responses — field names, types, and forbidden keys.
 *
 * These tests catch breaking changes: if the API team renames a field
 * or changes a type, these assertions fail before production.
 */
public class ContractValidationTest extends BaseTest {

    // ── User contract ─────────────────────────────────────────────────────────

    @Test(groups = {"smoke", "contract"},
          description = "GET /users/{id} — all contract fields present with correct types")
    public void testUserResponseContract() {
        given()
                .spec(requestSpec)
                .when()
                .get("/users/2")
                .then()
                .statusCode(200)
                .body("data.id",         instanceOf(Integer.class))
                .body("data.email",      instanceOf(String.class))
                .body("data.email",      containsString("@"))
                .body("data.first_name", instanceOf(String.class))
                .body("data.first_name", not(emptyOrNullString()))
                .body("data.last_name",  instanceOf(String.class))
                .body("data.avatar",     startsWith("https://"));
    }

    @Test(groups = "contract",
          description = "GET /users/{id} — password key must never appear in response")
    public void testPasswordNeverExposedInUserContract() {
        Response response = given()
                .spec(requestSpec)
                .when()
                .get("/users/2")
                .then()
                .statusCode(200)
                .extract().response();

        Assert.assertFalse(
                response.asString().contains("\"password\""),
                "Contract violation: 'password' field leaked into user response");
    }

    @Test(groups = "contract",
          description = "GET /users — list items all conform to minimal user contract")
    public void testUserListItemsMatchContract() {
        List<Map<String, Object>> users = given()
                .spec(requestSpec)
                .queryParam("page", 1)
                .when()
                .get("/users")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList("data");

        Assert.assertFalse(users.isEmpty(), "user list must not be empty");

        for (Map<String, Object> user : users) {
            Assert.assertTrue(user.containsKey("id"),
                    "Contract violation: 'id' missing from list item");
            Assert.assertTrue(user.containsKey("email"),
                    "Contract violation: 'email' missing from list item");
            Assert.assertTrue(user.containsKey("first_name"),
                    "Contract violation: 'first_name' missing from list item");
            Assert.assertTrue(user.containsKey("last_name"),
                    "Contract violation: 'last_name' missing from list item");
            Assert.assertNotNull(user.get("id"),
                    "Contract violation: 'id' is null");
        }
    }

    // ── POST contract ─────────────────────────────────────────────────────────

    @Test(groups = "contract",
          description = "POST /users — response includes id, name, job, createdAt")
    public void testCreateUserResponseContract() {
        given()
                .spec(requestSpec)
                .body("{\"name\":\"Contract Test\",\"job\":\"QA\"}")
                .when()
                .post("/users")
                .then()
                .statusCode(201)
                .body("$", hasKey("id"))
                .body("$", hasKey("name"))
                .body("$", hasKey("job"))
                .body("$", hasKey("createdAt"))
                .body("id",        not(emptyOrNullString()))
                .body("createdAt", not(emptyOrNullString()));
    }

    // ── Status code contract ──────────────────────────────────────────────────

    @Test(groups = "contract",
          description = "DELETE /users/{id} — always returns 204 (no body)")
    public void testDeleteReturnsNoBody() {
        Response response = given()
                .spec(requestSpec)
                .when()
                .delete("/users/2")
                .then()
                .statusCode(204)
                .extract().response();

        Assert.assertTrue(
                response.body().asString().isEmpty(),
                "Contract violation: DELETE response body must be empty");
    }

    // ── Content-Type contract ─────────────────────────────────────────────────

    @Test(groups = "contract",
          description = "GET /users — Content-Type header is application/json")
    public void testContentTypeHeaderContract() {
        given()
                .spec(requestSpec)
                .when()
                .get("/users/2")
                .then()
                .statusCode(200)
                .contentType(containsString("application/json"));
    }
}
