// Package of the test classes
package com.automation.selenium.tests;

// assertEquals(actual, expected, message): fails the test when the two differ
import static org.testng.Assert.assertEquals;

// Throwing this exception marks a test as skipped (not failed)
import org.testng.SkipException;
// Marks a method (or every public method of a class) as a TestNG test
import org.testng.annotations.Test;

// Base class: starts a browser before each test and closes it afterwards
import com.automation.selenium.base.BaseTest;
// Page object of the dashboard
import com.automation.selenium.pages.DashboardPage;
// Writes steps into the HTML report
import com.automation.selenium.utils.Steps;

/**
 * Tests that fail or skip <b>on purpose</b>, so the HTML report shows every
 * outcome: an assertion failure, an error thrown by a page object (both with
 * a screenshot), a skip with a reason and a skip caused by a failed
 * dependency.
 * <p>
 * They are in the {@code demo} group, which a normal run leaves out (see
 * {@code excludedGroups} in pom.xml and testng.xml). Include them with
 * {@code mvn clean test -Pdemo}; that build then ends as failed.
 */
// A class-level @Test puts every test method of the class into the "demo" group
@Test(groups = "demo")
public class ReportDemoTest extends BaseTest {

	// Title the dashboard does not have, so the check below fails
	private static final String WRONG_TITLE = "Home";

	/**
	 * Logs in and expects the dashboard title "Home". The title is
	 * "Dashboard", so the assertion fails and the report shows the expected
	 * and actual values and the screenshot.
	 */
	@Test(description = "Demo failure: the dashboard title is checked against a wrong value")
	public void dashboardTitleCheckFails() {
		// Log in as Admin (helper from BaseTest); returns the loaded dashboard
		DashboardPage dashboard = loginAsAdmin();

		// Actual "Dashboard" vs expected "Home" → AssertionError: Dashboard title expected [Home] but found [Dashboard]
		assertEquals(dashboard.moduleTitle(), WRONG_TITLE, "Dashboard title");
	}

	/**
	 * Logs in with a wrong password through {@code loginAs}, which expects to
	 * reach the dashboard. It stops with an {@link IllegalStateException}
	 * that names the login error, so the report shows an error that is not an
	 * assertion, with the login page in the screenshot.
	 */
	@Test(description = "Demo failure: login with a wrong password stops with the login error")
	public void loginWithWrongPasswordFails() {
		// Open the login page and try to log in; loginAs throws "Login as "Admin" failed: Invalid credentials"
		openLoginPage().loginAs("Admin", "wrong-password");
	}

	/**
	 * Skips itself because a precondition is missing. The steps run before the
	 * skip and the reason are both shown in the report.
	 */
	@Test(description = "Demo skip: the check needs something the demo site does not offer")
	public void skippedWhenPreconditionIsMissing() {
		// Do some real work first, so the report shows steps before the skip
		openLoginPage();
		// Record the step that leads to the skip
		Steps.log("Check whether the site can send e-mails");
		// SkipException → TestNG reports the test as SKIPPED with this message as the reason
		throw new SkipException("The demo site sends no e-mails, so the password reset mail cannot be checked");
	}

	/**
	 * Depends on {@link #dashboardTitleCheckFails()}. That test fails, so
	 * TestNG skips this one without running it.
	 */
	// dependsOnMethods: run only after that test, and only if it passed; otherwise skip
	@Test(dependsOnMethods = "dashboardTitleCheckFails",
			description = "Demo skip: depends on a test that failed")
	public void skippedBecauseDependencyFailed() {
		// Never runs in practice: the test it depends on always fails
		loginAsAdmin().openAdmin();
	}
}
