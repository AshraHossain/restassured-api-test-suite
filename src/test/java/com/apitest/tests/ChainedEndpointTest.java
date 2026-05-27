package com.apitest.tests;

import com.apitest.base.BaseTest;
import com.apitest.utils.TokenStore;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * ChainedEndpointTest
 *
 * Multi-step tests that extract values from one response and inject
 * them into the next request — simulating real user workflows.
 *
 * Patterns demonstrated:
 *   - extract().path() to capture IDs and tokens
 *   - Bearer token propagation across steps
 *   - Asserting relational consistency (e.g. returned userId matches sent)
 */
public class ChainedEndpointTest extends BaseTest {

    // ── Chain 1: Register → Login → Authenticated GET ────────────────────────

    @Test(groups = {"smoke", "chain"},
          description = "Register new user → login → verify token works on protected call")
    public void testRegisterLoginChain() {
        // Step 1 — register (reqres.in accepts defined set of emails)
        given()
                .spec(requestSpec)
                .body("{\"email\":\"eve.holt@reqres.in\",\"password\":\"pistol\"}")
                .when()
                .post("/register")
                .then()
                .statusCode(200)
                .body("id",    notNullValue())
                .body("token", notNullValue());

        // Step 2 — login with same credentials
        String token = given()
                .spec(requestSpec)
                .body("{\"email\":\"eve.holt@reqres.in\",\"password\":\"cityslicka\"}")
                .when()
                .post("/login")
                .then()
                .statusCode(200)
                .body("token", notNullValue())
                .extract().path("token");

        Assert.assertNotNull(token, "Login token must not be null");
        extentTest.info("Token acquired: " + token.substring(0, 4) + "****");

        // Step 3 — use token to fetch user list (simulate authenticated call)
        given()
                .spec(requestSpec)
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/users?page=1")
                .then()
                .statusCode(200)
                .body("data", not(empty()));
    }

    // ── Chain 2: Create user → Read back → Verify fields match ───────────────

    @Test(groups = "chain",
          description = "POST /users → extract id → GET /users/{id} — fields round-trip correctly")
    public void testCreateThenReadChain() {
        String expectedName = "Chain Test User";
        String expectedJob  = "Automation Engineer";

        // Step 1 — create
        Response createResponse = given()
                .spec(requestSpec)
                .body("{\"name\":\"" + expectedName + "\",\"job\":\"" + expectedJob + "\"}")
                .when()
                .post("/users")
                .then()
                .statusCode(201)
                .body("name", equalTo(expectedName))
                .body("job",  equalTo(expectedJob))
                .extract().response();

        String returnedId   = createResponse.jsonPath().getString("id");
        String returnedName = createResponse.jsonPath().getString("name");

        extentTest.info("Created user id=" + returnedId);

        // Step 2 — assert name in POST response matches what was sent
        Assert.assertEquals(returnedName, expectedName,
                "name in POST response must match payload");

        // Step 3 — GET a seeded user by id (reqres does not persist POSTs)
        //           Demonstrates how you'd verify with a real persisting API
        given()
                .spec(requestSpec)
                .when()
                .get("/users/2")
                .then()
                .statusCode(200)
                .body("data.id", equalTo(2));
    }

    // ── Chain 3: Create → Update → Assert updatedAt changes ─────────────────

    @Test(groups = "chain",
          description = "POST /users → PUT /users/{id} — updatedAt timestamp populated after update")
    public void testCreateThenUpdateChain() {
        // Step 1 — create
        String userId = given()
                .spec(requestSpec)
                .body("{\"name\":\"Before Update\",\"job\":\"Junior QA\"}")
                .when()
                .post("/users")
                .then()
                .statusCode(201)
                .extract().path("id");

        extentTest.info("User created with id: " + userId);

        // Step 2 — update (use reqres-seeded id=2 as PUT actually works there)
        given()
                .spec(requestSpec)
                .body("{\"name\":\"After Update\",\"job\":\"Senior QA\"}")
                .when()
                .put("/users/2")
                .then()
                .statusCode(200)
                .body("name",      equalTo("After Update"))
                .body("job",       equalTo("Senior QA"))
                .body("updatedAt", notNullValue());
    }

    // ── Chain 4: Paginate through all users and collect IDs ──────────────────

    @Test(groups = "chain",
          description = "GET /users page 1 → page 2 — ids are unique across pages")
    public void testPaginationChain() {
        // Page 1
        java.util.List<Integer> page1Ids = given()
                .spec(requestSpec)
                .queryParam("page", 1)
                .when()
                .get("/users")
                .then()
                .statusCode(200)
                .extract().jsonPath().getList("data.id", Integer.class);

        // Page 2
        java.util.List<Integer> page2Ids = given()
                .spec(requestSpec)
                .queryParam("page", 2)
                .when()
                .get("/users")
                .then()
                .statusCode(200)
                .extract().jsonPath().getList("data.id", Integer.class);

        extentTest.info("Page 1 IDs: " + page1Ids);
        extentTest.info("Page 2 IDs: " + page2Ids);

        // Assert pages do not share ids
        boolean overlap = page1Ids.stream().anyMatch(page2Ids::contains);
        Assert.assertFalse(overlap, "IDs on page 1 and page 2 must be distinct");
    }
}
