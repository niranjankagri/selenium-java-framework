// Package of the shared test base classes
package com.automation.selenium.base;

// Thrown when the screenshot file cannot be written
import java.io.IOException;
// File helpers (create folders, write bytes)
import java.nio.file.Files;
// A file or folder path
import java.nio.file.Path;
// Turns the Base64 screenshot text back into PNG bytes
import java.util.Base64;

// Says in which format to return a screenshot (BASE64, BYTES or FILE)
import org.openqa.selenium.OutputType;
// Interface of drivers that can take screenshots (all real browsers)
import org.openqa.selenium.TakesScreenshot;
// The browser
import org.openqa.selenium.WebDriver;
// Base class of Selenium's errors (e.g. the browser already crashed)
import org.openqa.selenium.WebDriverException;
// The result of the test that just ran (status, name, attributes)
import org.testng.ITestResult;
// TestNG's per-test log; used to attach cleanup steps to the right test
import org.testng.Reporter;
// Runs a method after every test method
import org.testng.annotations.AfterMethod;
// Runs a method before every test method
import org.testng.annotations.BeforeMethod;

// Settings (browser, headless, ...)
import com.automation.selenium.config.Config;
// Starts and holds the browser of each thread
import com.automation.selenium.driver.DriverManager;
// Page objects returned by the helpers below
import com.automation.selenium.pages.DashboardPage;
import com.automation.selenium.pages.LoginPage;
// Owns the name of the screenshot attribute the report reads
import com.automation.selenium.report.HtmlReportListener;

/**
 * Parent of all test classes. Every test method gets a fresh browser, and a
 * failed test gets a screenshot, which is saved to {@code target/screenshots}
 * and embedded in the HTML report. A test class can override
 * {@link #cleanUp()} to remove data it created, with the browser still open.
 * <p>
 * Order around each test: {@code @BeforeMethod startBrowser} → the test →
 * {@code @AfterMethod stopBrowser} (screenshot if failed → cleanUp → quit).
 */
// abstract: never run on its own, only through its test subclasses
public abstract class BaseTest {

	// Folder for the PNG screenshots of failed tests
	private static final Path SCREENSHOT_DIR = Path.of("target", "screenshots");

	/**
	 * Starts the browser configured in config.properties for this test.
	 */
	// alwaysRun = true: also runs when a group filter (e.g. -Dgroups=smoke) is used,
	// otherwise TestNG would skip this setup for grouped tests and they would have no browser
	@BeforeMethod(alwaysRun = true)
	public void startBrowser() {
		// One fresh browser per test, on this thread (-Dbrowser / -Dheadless override the file)
		DriverManager.start(Config.get().browser(), Config.get().headless());
	}

	/**
	 * Takes a screenshot if the test failed, runs {@link #cleanUp()}, then
	 * closes the browser. The screenshot comes first, so it shows the page at
	 * the moment of failure, not after the cleanup.
	 *
	 * @param result the result of the test that just ran.
	 */
	// TestNG passes the ITestResult automatically because the method declares it as a parameter
	@AfterMethod(alwaysRun = true)
	public void stopBrowser(ITestResult result) {
		try {
			// Failed and the browser is still there (it may not have started at all)
			if (result.getStatus() == ITestResult.FAILURE && DriverManager.isStarted()) {
				// Capture the page as it is right now
				attachScreenshot(result);
			}
			// The cleanup needs the browser
			if (DriverManager.isStarted()) {
				// Let the test class remove the data it created
				runCleanUp(result);
			}
		} finally {
			// Always close the browser, even if anything above threw
			DriverManager.quit();
		}
	}

	/**
	 * Removes test data the test created, while its browser is still open.
	 * Runs after every test, passed or failed; does nothing unless a test
	 * class overrides it.
	 */
	// protected + empty: a "hook" that subclasses may override (see EmployeeTest)
	protected void cleanUp() {
	}

	/**
	 * Gives tests the browser of their thread.
	 *
	 * @return WebDriver the browser of the running test.
	 */
	protected WebDriver driver() {
		return DriverManager.driver();
	}

	/**
	 * Opens the login page.
	 *
	 * @return LoginPage the login page, opened.
	 */
	protected LoginPage openLoginPage() {
		// Create the page object and load its URL
		return new LoginPage(driver()).open();
	}

	/**
	 * Opens the login page and logs in with the administrator from config.properties.
	 *
	 * @return DashboardPage the page shown after login.
	 */
	protected DashboardPage loginAsAdmin() {
		// Open the login page, then log in with the configured user name and password
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
			// Run the test class's cleanup (empty by default)
			cleanUp();
		} catch (RuntimeException e) {
			// Print it, but do not throw: a throwing @AfterMethod would make TestNG skip the following tests
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
			// Cast: WebDriver itself has no screenshot method, the TakesScreenshot interface does.
			// BASE64 = the PNG as text, which the HTML report can embed directly (data: URL)
			String base64 = ((TakesScreenshot) driver()).getScreenshotAs(OutputType.BASE64);
			// Store it on the test result; HtmlReportListener reads it from there
			result.setAttribute(HtmlReportListener.SCREENSHOT_ATTRIBUTE, base64);
			// Create target/screenshots if it does not exist yet (no error if it does)
			Files.createDirectories(SCREENSHOT_DIR);
			// Class_method_timestamp.png, so data-provider rows and reruns never overwrite each other
			String name = result.getTestClass().getRealClass().getSimpleName() + "_" + result.getMethod().getMethodName()
					+ "_" + System.currentTimeMillis() + ".png";
			// Decode the text back to PNG bytes and write the file
			Files.write(SCREENSHOT_DIR.resolve(name), Base64.getDecoder().decode(base64));
		} catch (WebDriverException | IOException e) {
			// Browser gone or disk problem: print it and carry on, the test result is what matters
			System.err.println("Could not take a screenshot of " + result.getName() + ": " + e.getMessage());
		}
	}
}
