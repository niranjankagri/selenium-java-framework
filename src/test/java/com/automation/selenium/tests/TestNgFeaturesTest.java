package com.automation.selenium.tests;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import com.automation.selenium.base.BaseTest;
import com.automation.selenium.base.RetryOnce;
import com.automation.selenium.config.Config;
import com.automation.selenium.pages.LoginPage;
import com.automation.selenium.utils.Steps;

/**
 * TestNG features that interviews ask about, each used on a real OrangeHRM
 * check: soft assertions, expected exceptions, priority with
 * dependsOnMethods, invocationCount, a retry analyzer and suite parameters.
 * Data providers and groups are shown in LoginTest; a skip and a failed
 * dependency in ReportDemoTest.
 * <p>
 * Not shown on purpose: {@code timeOut}. TestNG runs a test with a timeOut
 * on a separate thread, which has no browser in DriverManager's ThreadLocal,
 * so it does not suit thread-bound WebDriver tests.
 */
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
		LoginPage login = openLoginPage();
		SoftAssert soft = new SoftAssert();

		soft.assertEquals(driver().getTitle(), TITLE, "Page title");
		soft.assertEquals(login.placeholder("Username"), "Username", "Username placeholder");
		soft.assertEquals(login.placeholder("Password"), "Password", "Password placeholder");
		soft.assertEquals(login.buttonText(), "Login", "Button text");
		Steps.log("Checked title, both placeholders and the button text");

		soft.assertAll();
	}

	/**
	 * expectedExceptions: the test passes only if it throws an
	 * IllegalStateException whose message matches the pattern. loginAs()
	 * throws exactly that when the site shows "Invalid credentials".
	 */
	@Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = ".*Invalid credentials.*",
			description = "TestNG: expectedExceptions, a wrong password must stop with the login error")
	public void wrongPasswordThrowsTheLoginError() {
		openLoginPage().loginAs(Config.get().username(), "wrong-password");
	}

	/**
	 * priority: the lower number runs first within this class (the default is
	 * 0), so this check runs before the dependent test below.
	 */
	@Test(priority = 1, description = "TestNG: priority, the login page check runs first")
	public void loginPageIsShown() {
		assertTrue(openLoginPage().isDisplayed(), "The login form should be shown");
	}

	/**
	 * dependsOnMethods: runs only if loginPageIsShown passed. Had it failed,
	 * TestNG would skip this test, not fail it (see ReportDemoTest).
	 */
	@Test(priority = 2, dependsOnMethods = "loginPageIsShown",
			description = "TestNG: dependsOnMethods, log in only after the login page check passed")
	public void adminLogsInAfterThePageCheck() {
		assertTrue(loginAsAdmin().isDisplayed(), "The dashboard should be shown");
	}

	/**
	 * invocationCount: TestNG runs this test twice, each run with its own
	 * browser, and reports each run. Useful to catch a check that only
	 * sometimes passes.
	 */
	@Test(invocationCount = 2, description = "TestNG: invocationCount, a wrong password is rejected every time")
	public void wrongPasswordIsRejectedEveryTime() {
		LoginPage login = openLoginPage().submit(Config.get().username(), "wrong-password");

		assertEquals(login.errorMessage(), "Invalid credentials", "Error banner");
	}

	/**
	 * retryAnalyzer: if this test fails, RetryOnce runs it one more time
	 * before it counts as failed, which covers a short outage of the demo
	 * site. A passing run is never retried.
	 */
	@Test(retryAnalyzer = RetryOnce.class, description = "TestNG: retryAnalyzer, retried once if the demo site has a hiccup")
	public void adminLoginIsRetriedOnceOnFailure() {
		assertTrue(loginAsAdmin().isDisplayed(), "The dashboard should be shown");
	}

	/**
	 * Parameters: the value comes from testng.xml
	 * ({@code <parameter name="expectedTitle" .../>}). Maven runs the classes
	 * without that file, so Optional supplies the default.
	 *
	 * @param expectedTitle the page title the suite expects.
	 */
	@Parameters("expectedTitle")
	@Test(description = "TestNG: Parameters from testng.xml, with an Optional default")
	public void titleMatchesTheSuiteParameter(@Optional(TITLE) String expectedTitle) {
		openLoginPage();
		Steps.log("Expected title from the suite: \"" + expectedTitle + "\"");

		assertEquals(driver().getTitle(), expectedTitle, "Page title");
	}
}
