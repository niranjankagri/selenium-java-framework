// Package of the test classes
package com.automation.selenium.tests;

// TestNG hard assertions: the first failing one stops the test
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

// Default value for a suite parameter when testng.xml does not set it
import org.testng.annotations.Optional;
// Takes a method parameter's value from a <parameter> in testng.xml
import org.testng.annotations.Parameters;
// Marks a method (or every public method of a class) as a TestNG test
import org.testng.annotations.Test;
// Soft assertions: collect failures and report them all at the end
import org.testng.asserts.SoftAssert;

// Base class: starts a browser before each test and closes it afterwards
import com.automation.selenium.base.BaseTest;
// Retry analyzer that runs a failed test once more
import com.automation.selenium.base.RetryOnce;
// Settings (user name, password, ...)
import com.automation.selenium.config.Config;
// Page object of the login page
import com.automation.selenium.pages.LoginPage;
// Writes steps into the HTML report
import com.automation.selenium.utils.Steps;

/**
 * TestNG features that interviews ask about, each used on a real OrangeHRM
 * check: soft assertions, expected exceptions, priority with
 * dependsOnMethods, invocationCount, timeOut, a retry analyzer and suite
 * parameters. Data providers and groups are shown in LoginTest; a skip and a
 * failed dependency in ReportDemoTest.
 */
// Every test in this class belongs to the "interview" group
@Test(groups = "interview")
public class TestNgFeaturesTest extends BaseTest {

	// Title of every OrangeHRM page
	private static final String TITLE = "OrangeHRM";

	/**
	 * SoftAssert: checks four things on the login page and reports every
	 * mismatch together, instead of stopping at the first one. assertAll()
	 * at the end is what fails the test; without it the failures are lost.
	 */
	@Test(description = "TestNG: SoftAssert checks the whole login page in one run")
	public void softAssertChecksTheLoginPage() {
		// Open the login page
		LoginPage login = openLoginPage();
		// A new SoftAssert per test: it remembers every failed check
		SoftAssert soft = new SoftAssert();

		// Each check records a failure but lets the test go on
		soft.assertEquals(driver().getTitle(), TITLE, "Page title");
		soft.assertEquals(login.placeholder("Username"), "Username", "Username placeholder");
		soft.assertEquals(login.placeholder("Password"), "Password", "Password placeholder");
		soft.assertEquals(login.buttonText(), "Login", "Button text");
		// Record in the report what was checked
		Steps.log("Checked title, both placeholders and the button text");

		// Now fail the test if any check above failed, listing all of them in one error
		soft.assertAll();
	}

	/**
	 * expectedExceptions: the test passes only if it throws an
	 * IllegalStateException whose message matches the pattern. loginAs()
	 * throws exactly that when the site shows "Invalid credentials".
	 */
	// The test passes only if this exception type is thrown and its message matches the regex
	@Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = ".*Invalid credentials.*",
			description = "TestNG: expectedExceptions, a wrong password must stop with the login error")
	public void wrongPasswordThrowsTheLoginError() {
		// Right user, wrong password: loginAs throws "Login as "Admin" failed: Invalid credentials"
		openLoginPage().loginAs(Config.get().username(), "wrong-password");
	}

	/**
	 * priority: the lower number runs first within this class (the default is
	 * 0), so this check runs before the dependent test below.
	 */
	@Test(priority = 1, description = "TestNG: priority, the login page check runs first")
	public void loginPageIsShown() {
		// Open the login page and check its form is there
		assertTrue(openLoginPage().isDisplayed(), "The login form should be shown");
	}

	/**
	 * dependsOnMethods: runs only if loginPageIsShown passed. Had it failed,
	 * TestNG would skip this test, not fail it (see ReportDemoTest).
	 */
	@Test(priority = 2, dependsOnMethods = "loginPageIsShown",
			description = "TestNG: dependsOnMethods, log in only after the login page check passed")
	public void adminLogsInAfterThePageCheck() {
		// Log in and check the dashboard
		assertTrue(loginAsAdmin().isDisplayed(), "The dashboard should be shown");
	}

	/**
	 * invocationCount: TestNG runs this test twice, each run with its own
	 * browser, and reports each run. Useful to catch a check that only
	 * sometimes passes.
	 */
	@Test(invocationCount = 2, description = "TestNG: invocationCount, a wrong password is rejected every time")
	public void wrongPasswordIsRejectedEveryTime() {
		// Submit a wrong password (submit does not expect the login to work)
		LoginPage login = openLoginPage().submit(Config.get().username(), "wrong-password");

		// The red banner must say "Invalid credentials" on every run
		assertEquals(login.errorMessage(), "Invalid credentials", "Error banner");
	}

	/**
	 * timeOut: fails the test if it runs longer than 60 seconds. It is a safety
	 * net against a page that hangs; the explicit waits still give the precise
	 * error for anything slower than expected. The timed test still sees the
	 * browser that BaseTest started for this thread.
	 */
	// 60_000 ms = 60 s (the underscore is only a digit separator for readability)
	@Test(timeOut = 60_000, description = "TestNG: timeOut, the login must finish within 60 seconds")
	public void loginFinishesWithinOneMinute() {
		// Log in and check the dashboard, all within the time limit
		assertTrue(loginAsAdmin().isDisplayed(), "The dashboard should be shown");
	}

	/**
	 * retryAnalyzer: if this test fails, RetryOnce runs it one more time
	 * before it counts as failed, which covers a short outage of the demo
	 * site. A passing run is never retried.
	 */
	@Test(retryAnalyzer = RetryOnce.class, description = "TestNG: retryAnalyzer, retried once if the demo site has a hiccup")
	public void adminLoginIsRetriedOnceOnFailure() {
		// Log in and check the dashboard; on failure TestNG asks RetryOnce whether to run again
		assertTrue(loginAsAdmin().isDisplayed(), "The dashboard should be shown");
	}

	/**
	 * Parameters: the value comes from testng.xml
	 * ({@code <parameter name="expectedTitle" .../>}). Maven runs the classes
	 * without that file, so Optional supplies the default.
	 *
	 * @param expectedTitle the page title the suite expects.
	 */
	// Fill the method parameter from <parameter name="expectedTitle"> in testng.xml
	@Parameters("expectedTitle")
	@Test(description = "TestNG: Parameters from testng.xml, with an Optional default")
	public void titleMatchesTheSuiteParameter(@Optional(TITLE) String expectedTitle) {
		// Open the login page
		openLoginPage();
		// Show in the report which value was used (from testng.xml or the default)
		Steps.log("Expected title from the suite: \"" + expectedTitle + "\"");

		// The browser tab title must match
		assertEquals(driver().getTitle(), expectedTitle, "Page title");
	}
}
