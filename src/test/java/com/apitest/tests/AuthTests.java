package com.apitest.tests;

import com.apitest.base.BaseTest;
import com.apitest.utils.TokenStore;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * AuthTests
 *
 * Covers authentication flows and header-based access control.
 *
 * Patterns demonstrated:
 *   - Bearer token injection via TokenStore
 *   - Basic Auth via rest-assured's .auth().basic()
 *   - Negative auth: expired/missing tokens must return 401/400
 *   - Custom request header propagation
 */
public class AuthTests extends BaseTest {

    // ── Successful auth flows ─────────────────────────────────────────────────

    @Test(groups = {"smoke", "auth"},
          description = "POST /login — valid credentials return 200 and a token")
    public void testSuccessfulLogin() {
        given()
                .spec(requestSpec)
                .body("{\"email\":\"eve.holt@reqres.in\",\"password\":\"cityslicka\"}")
                .when()
                .post("/login")
                .then()
                .statusCode(200)
                .body("token", notNullValue())
                .body("token", not(emptyOrNullString()));
    }

    @Test(groups = {"smoke", "auth"},
          description = "GET /users with valid Bearer token — returns 200")
    public void testBearerTokenGrantsAccess() {
        String token = TokenStore.getValidToken();

        given()
                .spec(requestSpec)
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/users")
                .then()
                .statusCode(200)
                .body("data", not(empty()));
    }

    @Test(groups = "auth",
          description = "POST /register — valid email + password returns id and token")
    public void testSuccessfulRegistration() {
        given()
                .spec(requestSpec)
                .body("{\"email\":\"eve.holt@reqres.in\",\"password\":\"pistol\"}")
                .when()
                .post("/register")
                .then()
                .statusCode(200)
                .body("id",    notNullValue())
                .body("token", notNullValue());
    }

    // ── Negative auth flows ───────────────────────────────────────────────────

    @Test(groups = {"smoke", "auth"},
          description = "POST /login — missing password returns 400")
    public void testLoginWithMissingPasswordReturns400() {
        given()
                .spec(requestSpec)
                .body("{\"email\":\"peter@klaven.com\"}")
                .when()
                .post("/login")
                .then()
                .statusCode(400)
                .body("error", equalTo("Missing password"));
    }

    @Test(groups = "auth",
          description = "POST /login — missing email field returns 400")
    public void testLoginWithMissingEmailReturns400() {
        given()
                .spec(requestSpec)
                .body("{\"password\":\"hunter2\"}")
                .when()
                .post("/login")
                .then()
                .statusCode(400)
                .body("error", notNullValue());
    }

    @Test(groups = "auth",
          description = "POST /register — undefined user returns 400")
    public void testRegisterUndefinedUserReturns400() {
        given()
                .spec(requestSpec)
                .body("{\"email\":\"sydney@fife.com\"}")
                .when()
                .post("/register")
                .then()
                .statusCode(400)
                .body("error", equalTo("Note: Only defined users succeed registration"));
    }

    // ── Header tests ──────────────────────────────────────────────────────────

    @Test(groups = "auth",
          description = "GET /users — custom X-Request-ID header is accepted (no 4xx)")
    public void testCustomRequestIdHeader() {
        given()
                .spec(requestSpec)
                .header("X-Request-ID", "test-" + System.currentTimeMillis())
                .header("X-Client",     "restassured-suite")
                .when()
                .get("/users/2")
                .then()
                .statusCode(200);
    }
}
