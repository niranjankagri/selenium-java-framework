package com.automation.selenium.tests;

import static org.testng.Assert.assertEquals;

import org.testng.SkipException;
import org.testng.annotations.Test;

import com.automation.selenium.base.BaseTest;
import com.automation.selenium.pages.DashboardPage;
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
		DashboardPage dashboard = loginAsAdmin();

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
		openLoginPage().loginAs("Admin", "wrong-password");
	}

	/**
	 * Skips itself because a precondition is missing. The steps run before the
	 * skip and the reason are both shown in the report.
	 */
	@Test(description = "Demo skip: the check needs something the demo site does not offer")
	public void skippedWhenPreconditionIsMissing() {
		openLoginPage();
		Steps.log("Check whether the site can send e-mails");
		throw new SkipException("The demo site sends no e-mails, so the password reset mail cannot be checked");
	}

	/**
	 * Depends on {@link #dashboardTitleCheckFails()}. That test fails, so
	 * TestNG skips this one without running it.
	 */
	@Test(dependsOnMethods = "dashboardTitleCheckFails",
			description = "Demo skip: depends on a test that failed")
	public void skippedBecauseDependencyFailed() {
		loginAsAdmin().openAdmin();
	}
}
