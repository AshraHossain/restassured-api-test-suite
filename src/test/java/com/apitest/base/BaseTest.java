package com.apitest.base;

import com.apitest.utils.ExtentReportManager;
import com.aventstack.extentreports.ExtentTest;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * BaseTest
 *
 * Provides shared RequestSpecification, Extent test lifecycle hooks,
 * and config.properties loading for every test class.
 */
public class BaseTest {

    protected static RequestSpecification requestSpec;
    protected static Properties           config = new Properties();
    protected        ExtentTest           extentTest;

    // ── Load config and build shared RequestSpec once per suite ──────────────
    @BeforeSuite(alwaysRun = true)
    public void globalSetup() throws IOException {
        loadConfig();

        // Start local WireMock sandbox and inject its dynamically assigned base URL
        com.apitest.utils.ReqresMockServer.start();
        String baseUrl = com.apitest.utils.ReqresMockServer.getBaseUrl();

        requestSpec = new RequestSpecBuilder()
                .setBaseUri(baseUrl)
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addFilter(new RequestLoggingFilter())
                .addFilter(new ResponseLoggingFilter())
                .build();

        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @BeforeMethod(alwaysRun = true)
    public void startTest(java.lang.reflect.Method method) {
        // Create an Extent node named after the test method
        extentTest = ExtentReportManager.getInstance()
                .createTest(method.getName());
    }

    @AfterMethod(alwaysRun = true)
    public void logResult(ITestResult result) {
        switch (result.getStatus()) {
            case ITestResult.FAILURE ->
                    extentTest.fail(result.getThrowable());
            case ITestResult.SKIP ->
                    extentTest.skip("Test skipped: " + result.getThrowable());
            default ->
                    extentTest.pass("Test passed");
        }
    }

    @AfterSuite(alwaysRun = true)
    public void flushReport() {
        ExtentReportManager.getInstance().flush();
        com.apitest.utils.ReqresMockServer.stop();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────
    private void loadConfig() throws IOException {
        String path = "src/test/resources/config/config.properties";
        try (FileInputStream fis = new FileInputStream(path)) {
            config.load(fis);
        }
    }

    protected String getBaseUrl() {
        return config.getProperty("base.url", "https://reqres.in/api");
    }
}
