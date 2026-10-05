package com.automation.selenium.base;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import com.automation.selenium.config.Config;
import com.automation.selenium.driver.DriverManager;
import com.automation.selenium.pages.DashboardPage;
import com.automation.selenium.pages.LoginPage;
import com.automation.selenium.report.HtmlReportListener;

/**
 * Parent of all test classes. Every test method gets a fresh browser, and a
 * failed test gets a screenshot, which is saved to {@code target/screenshots}
 * and embedded in the HTML report. A test class can override
 * {@link #cleanUp()} to remove data it created, with the browser still open.
 */
public abstract class BaseTest {

	// Folder for the PNG screenshots of failed tests
	private static final Path SCREENSHOT_DIR = Path.of("target", "screenshots");

	/**
	 * Starts the browser configured in config.properties for this test.
	 */
	@BeforeMethod(alwaysRun = true)
	public void startBrowser() {
		DriverManager.start(Config.get().browser(), Config.get().headless());
	}

	/**
	 * Takes a screenshot if the test failed, runs {@link #cleanUp()}, then
	 * closes the browser. The screenshot comes first, so it shows the page at
	 * the moment of failure, not after the cleanup.
	 *
	 * @param result the result of the test that just ran.
	 */
	@AfterMethod(alwaysRun = true)
	public void stopBrowser(ITestResult result) {
		try {
			if (result.getStatus() == ITestResult.FAILURE && DriverManager.isStarted()) {
				attachScreenshot(result);
			}
			if (DriverManager.isStarted()) {
				runCleanUp(result);
			}
		} finally {
			DriverManager.quit();
		}
	}

	/**
	 * Removes test data the test created, while its browser is still open.
	 * Runs after every test, passed or failed; does nothing unless a test
	 * class overrides it.
	 */
	protected void cleanUp() {
	}

	/**
	 * @return WebDriver the browser of the running test.
	 */
	protected WebDriver driver() {
		return DriverManager.driver();
	}

	/**
	 * @return LoginPage the login page, opened.
	 */
	protected LoginPage openLoginPage() {
		return new LoginPage(driver()).open();
	}

	/**
	 * Opens the login page and logs in with the administrator from config.properties.
	 *
	 * @return DashboardPage the page shown after login.
	 */
	protected DashboardPage loginAsAdmin() {
		return openLoginPage().loginAsAdmin();
	}

	/**
	 * Runs {@link #cleanUp()}. A failing cleanup is only logged, so it never
	 * hides the test's own result.
	 *
	 * @param result the test that just ran.
	 */
	private void runCleanUp(ITestResult result) {
		// Steps logged during cleanup belong to the test, not to this @AfterMethod
		Reporter.setCurrentTestResult(result);
		try {
			cleanUp();
		} catch (RuntimeException e) {
			System.err.println("Cleanup after " + result.getName() + " failed: " + e.getMessage());
		}
	}

	/**
	 * Captures the browser window, stores it as a result attribute for the
	 * report and writes it to {@link #SCREENSHOT_DIR}. A failing capture is
	 * only logged, so it never hides the real test failure.
	 *
	 * @param result the failed test.
	 */
	private void attachScreenshot(ITestResult result) {
		try {
			String base64 = ((TakesScreenshot) driver()).getScreenshotAs(OutputType.BASE64);
			result.setAttribute(HtmlReportListener.SCREENSHOT_ATTRIBUTE, base64);
			Files.createDirectories(SCREENSHOT_DIR);
			// Class_method_timestamp.png, so data-provider rows and reruns never overwrite each other
			String name = result.getTestClass().getRealClass().getSimpleName() + "_" + result.getMethod().getMethodName()
					+ "_" + System.currentTimeMillis() + ".png";
			Files.write(SCREENSHOT_DIR.resolve(name), Base64.getDecoder().decode(base64));
		} catch (WebDriverException | IOException e) {
			System.err.println("Could not take a screenshot of " + result.getName() + ": " + e.getMessage());
		}
	}
}
