package com.apitest.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ExtentReportManager
 *
 * Singleton that initialises one ExtentReports instance per suite run.
 * The HTML report lands in target/extent-reports/ and is uploaded as a
 * GitHub Actions artifact on every CI run.
 */
public class ExtentReportManager {

    private static ExtentReports extent;

    private ExtentReportManager() {}

    public static ExtentReports getInstance() {
        if (extent == null) {
            String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            String reportPath = "target/extent-reports/report-" + timestamp + ".html";

            ExtentSparkReporter spark = new ExtentSparkReporter(reportPath);
            spark.config().setTheme(Theme.DARK);
            spark.config().setDocumentTitle("REST Assured API Suite");
            spark.config().setReportName("API Regression Report");
            spark.config().setEncoding("utf-8");

            extent = new ExtentReports();
            extent.attachReporter(spark);
            extent.setSystemInfo("Framework",    "Rest Assured 5 + TestNG 7");
            extent.setSystemInfo("Author",       "Ashra Hossain");
            extent.setSystemInfo("Java Version", System.getProperty("java.version"));
            extent.setSystemInfo("Environment",
                    System.getenv("BASE_URL") != null ? "CI (GitHub Actions)" : "Local");
        }
        return extent;
    }
}
