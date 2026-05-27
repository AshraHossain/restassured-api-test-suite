package com.apitest.tests;

import com.apitest.base.BaseTest;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.*;

/**
 * SchemaValidationTest
 *
 * Asserts that API responses conform to pre-defined JSON schemas
 * stored in src/test/resources/schemas/json/.
 *
 * Demonstrates:
 *   - matchesJsonSchemaInClasspath() for structural contract enforcement
 *   - Field-level type and presence assertions as a secondary layer
 */
public class SchemaValidationTest extends BaseTest {

    // ── JSON Schema Validation ────────────────────────────────────────────────

    @Test(groups = {"smoke", "schema"},
          description = "GET /users/{id} — response matches user-schema.json")
    public void testSingleUserMatchesJsonSchema() {
        given()
                .spec(requestSpec)
                .when()
                .get("/users/2")
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/json/user-schema.json"));
    }

    @Test(groups = "schema",
          description = "GET /users?page=1 — list response matches user-list-schema.json")
    public void testUserListMatchesJsonSchema() {
        given()
                .spec(requestSpec)
                .queryParam("page", 1)
                .when()
                .get("/users")
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/json/user-list-schema.json"));
    }

    @Test(groups = "schema",
          description = "POST /users — created resource matches user-created-schema.json")
    public void testCreatedUserMatchesSchema() {
        given()
                .spec(requestSpec)
                .body("{\"name\":\"Schema Tester\",\"job\":\"QA\"}")
                .when()
                .post("/users")
                .then()
                .statusCode(201)
                .body(matchesJsonSchemaInClasspath("schemas/json/user-created-schema.json"));
    }

    // ── Field-level structural assertions (no schema file required) ───────────

    @Test(groups = "schema",
          description = "GET /users/{id} — all required fields present and correctly typed")
    public void testUserFieldTypes() {
        given()
                .spec(requestSpec)
                .when()
                .get("/users/2")
                .then()
                .statusCode(200)
                .body("data.id",         instanceOf(Integer.class))
                .body("data.email",      instanceOf(String.class))
                .body("data.first_name", instanceOf(String.class))
                .body("data.last_name",  instanceOf(String.class))
                .body("data.avatar",     matchesRegex("https?://.+"))
                .body("support.url",     notNullValue())
                .body("support.text",    notNullValue());
    }

    @Test(groups = "schema",
          description = "GET /users — pagination envelope contains all required keys")
    public void testPaginationEnvelopeShape() {
        given()
                .spec(requestSpec)
                .queryParam("page", 1)
                .when()
                .get("/users")
                .then()
                .statusCode(200)
                .body("$",              hasKey("page"))
                .body("$",              hasKey("per_page"))
                .body("$",              hasKey("total"))
                .body("$",              hasKey("total_pages"))
                .body("$",              hasKey("data"))
                .body("per_page",       instanceOf(Integer.class))
                .body("total",          greaterThan(0))
                .body("total_pages",    greaterThan(0));
    }

    @Test(groups = "schema",
          description = "GET /users/2 — sensitive fields absent from public user response")
    public void testNoSensitiveFieldsLeaked() {
        io.restassured.response.Response response = given()
                .spec(requestSpec)
                .when()
                .get("/users/2")
                .then()
                .statusCode(200)
                .extract().response();

        // Assert password, token, secret never appear in response keys
        org.testng.Assert.assertFalse(
                response.asString().contains("password"),
                "password must never appear in user response");
        org.testng.Assert.assertFalse(
                response.asString().contains("secret"),
                "secret must never appear in user response");
    }
}
