package com.apitest.tests;

import com.apitest.base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.InputStream;

import static io.restassured.RestAssured.given;
import static io.restassured.matcher.RestAssuredMatchers.matchesXsd;
import static org.hamcrest.Matchers.*;

/**
 * XmlSchemaValidationTest
 *
 * Validates that XML-producing endpoints return well-formed responses
 * that conform to pre-defined XSD schemas stored in
 * src/test/resources/schemas/xml/.
 *
 * Demonstrates:
 *   - matchesXsd() for structural contract enforcement on XML responses
 *   - GPath assertions on XML field values and list envelopes
 *   - Content-Type header validation for XML endpoints
 */
public class XmlSchemaValidationTest extends BaseTest {

    // ── XSD Schema Validation ─────────────────────────────────────────────────

    @Test(groups = {"smoke", "xml"},
          description = "GET /xml/users/2 — response validates against user-schema.xsd")
    public void testSingleUserXmlMatchesXsd() {
        InputStream xsd = getClass().getResourceAsStream("/schemas/xml/user-schema.xsd");
        given()
                .spec(requestSpec)
                .accept("application/xml")
                .when()
                .get("/xml/users/2")
                .then()
                .statusCode(200)
                .body(matchesXsd(xsd));
    }

    @Test(groups = "xml",
          description = "GET /xml/users?page=1 — list response validates against user-list-schema.xsd")
    public void testUserListXmlMatchesXsd() {
        InputStream xsd = getClass().getResourceAsStream("/schemas/xml/user-list-schema.xsd");
        given()
                .spec(requestSpec)
                .accept("application/xml")
                .queryParam("page", 1)
                .when()
                .get("/xml/users")
                .then()
                .statusCode(200)
                .body(matchesXsd(xsd));
    }

    // ── GPath Field Assertions ────────────────────────────────────────────────

    @Test(groups = {"smoke", "xml"},
          description = "GET /xml/users/2 — GPath field values match expected data")
    public void testXmlUserFieldAssertions() {
        given()
                .spec(requestSpec)
                .accept("application/xml")
                .when()
                .get("/xml/users/2")
                .then()
                .statusCode(200)
                .body("user.data.id",         equalTo("2"))
                .body("user.data.email",      equalTo("janet.weaver@reqres.in"))
                .body("user.data.first_name", equalTo("Janet"))
                .body("user.data.last_name",  equalTo("Weaver"))
                .body("user.data.avatar",     startsWith("https://"));
    }

    @Test(groups = "xml",
          description = "GET /xml/users?page=1 — GPath assertions on XML list envelope")
    public void testXmlUserListGPathAssertions() {
        given()
                .spec(requestSpec)
                .accept("application/xml")
                .queryParam("page", 1)
                .when()
                .get("/xml/users")
                .then()
                .statusCode(200)
                .body("users.page",               equalTo("1"))
                .body("users.total",              equalTo("12"))
                .body("users.data.user[0].email", containsString("@"))
                .body("users.data.user.size()",   greaterThan(0));
    }

    // ── Content-Type Contract ─────────────────────────────────────────────────

    @Test(groups = "xml",
          description = "GET /xml/users/2 — response Content-Type is application/xml")
    public void testXmlContentTypeHeader() {
        given()
                .spec(requestSpec)
                .accept("application/xml")
                .when()
                .get("/xml/users/2")
                .then()
                .statusCode(200)
                .contentType(containsString("application/xml"));
    }

    // ── Sensitive Field Absence ───────────────────────────────────────────────

    @Test(groups = "xml",
          description = "GET /xml/users/2 — password and secret never appear in XML response")
    public void testNoSensitiveFieldsInXmlResponse() {
        String body = given()
                .spec(requestSpec)
                .accept("application/xml")
                .when()
                .get("/xml/users/2")
                .then()
                .statusCode(200)
                .extract().asString();

        Assert.assertFalse(body.contains("password"),
                "XML response must not leak password field");
        Assert.assertFalse(body.contains("secret"),
                "XML response must not leak secret field");
    }
}
