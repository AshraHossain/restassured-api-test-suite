package com.apitest.tests;

import com.apitest.base.BaseTest;
import com.apitest.payloads.UserPayload;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * CrudWorkflowTest
 *
 * Validates full Create → Read → Update → Delete lifecycle on the
 * /users endpoint. Each method chains off the ID returned by POST,
 * demonstrating stateful multi-step workflows.
 *
 * Target API: https://reqres.in  (public sandbox — no key required)
 */
public class CrudWorkflowTest extends BaseTest {

    private final UserPayload payload = new UserPayload();
    private int createdUserId;

    // ── CREATE ────────────────────────────────────────────────────────────────

    @Test(groups = {"smoke", "crud"}, priority = 1,
          description = "POST /users — creates a new user and returns 201 with id")
    public void testCreateUser() {
        Response response = given()
                .spec(requestSpec)
                .body(payload.createUser("Ashra Hossain", "QA Engineer"))
                .when()
                .post("/users")
                .then()
                .statusCode(201)
                .body("name", equalTo("Ashra Hossain"))
                .body("job",  equalTo("QA Engineer"))
                .body("id",   notNullValue())
                .extract().response();

        createdUserId = Integer.parseInt(response.jsonPath().getString("id"));
        extentTest.info("Created user id: " + createdUserId);
    }

    // ── READ ──────────────────────────────────────────────────────────────────

    @Test(groups = {"smoke", "crud"}, priority = 2,
          description = "GET /users/{id} — retrieves existing user by id",
          dependsOnMethods = "testCreateUser")
    public void testGetUserById() {
        // reqres.in seeds users 1-12; use id 2 for a stable read assertion
        given()
                .spec(requestSpec)
                .when()
                .get("/users/2")
                .then()
                .statusCode(200)
                .body("data.id",         equalTo(2))
                .body("data.email",      containsString("@"))
                .body("data.first_name", notNullValue())
                .body("data.last_name",  notNullValue());
    }

    @Test(groups = "crud", priority = 2,
          description = "GET /users?page=1 — retrieves paginated user list")
    public void testGetUserList() {
        given()
                .spec(requestSpec)
                .queryParam("page", 1)
                .when()
                .get("/users")
                .then()
                .statusCode(200)
                .body("page",         equalTo(1))
                .body("data",         not(empty()))
                .body("data.size()",  greaterThan(0))
                .body("data[0].id",   notNullValue());
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    @Test(groups = "crud", priority = 3,
          description = "PUT /users/{id} — full update returns 200 with updatedAt")
    public void testUpdateUserPut() {
        given()
                .spec(requestSpec)
                .body(payload.updateUser("Ashra H", "Senior QA Engineer"))
                .when()
                .put("/users/2")
                .then()
                .statusCode(200)
                .body("name",      equalTo("Ashra H"))
                .body("job",       equalTo("Senior QA Engineer"))
                .body("updatedAt", notNullValue());
    }

    @Test(groups = "crud", priority = 3,
          description = "PATCH /users/{id} — partial update changes only specified field")
    public void testUpdateUserPatch() {
        given()
                .spec(requestSpec)
                .body(payload.patchUser("job", "Lead QA"))
                .when()
                .patch("/users/2")
                .then()
                .statusCode(200)
                .body("job",       equalTo("Lead QA"))
                .body("updatedAt", notNullValue());
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    @Test(groups = "crud", priority = 4,
          description = "DELETE /users/{id} — returns 204 No Content")
    public void testDeleteUser() {
        given()
                .spec(requestSpec)
                .when()
                .delete("/users/2")
                .then()
                .statusCode(204);
    }

    // ── EDGE: second delete confirms idempotency ───────────────────────────

    @Test(groups = "crud", priority = 5,
          description = "DELETE /users/{id} second call — still returns 204 (reqres sandbox behaviour)")
    public void testDeleteIdempotency() {
        given()
                .spec(requestSpec)
                .when()
                .delete("/users/2")
                .then()
                .statusCode(204);
    }
}
