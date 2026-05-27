package com.apitest.utils;

import io.restassured.http.ContentType;

import static io.restassured.RestAssured.given;

/**
 * TokenStore
 *
 * Thread-local Bearer token cache so chained and auth tests can share
 * a valid token without re-authenticating on every request.
 */
public class TokenStore {

    private static final ThreadLocal<String> tokenHolder = new ThreadLocal<>();

    private TokenStore() {}

    /**
     * Returns a cached token, or fetches a fresh one if none exists
     * for the current thread.
     */
    public static String getValidToken() {
        if (tokenHolder.get() == null) {
            String token = fetchToken();
            tokenHolder.set(token);
        }
        return tokenHolder.get();
    }

    /** Force-refresh (call after a 401 or at suite teardown). */
    public static void invalidate() {
        tokenHolder.remove();
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private static String fetchToken() {
        return given()
                .contentType(ContentType.JSON)
                .body("{\"email\":\"eve.holt@reqres.in\",\"password\":\"cityslicka\"}")
                .when()
                .post(ReqresMockServer.getBaseUrl() + "/login")
                .then()
                .statusCode(200)
                .extract()
                .path("token");
    }
}
