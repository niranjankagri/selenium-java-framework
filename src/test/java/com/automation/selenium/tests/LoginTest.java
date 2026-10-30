// Package of the test classes
package com.automation.selenium.tests;

// TestNG assertions: each fails the test with the given message when the check is false
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

// Marks a method (or every public method of a class) as a TestNG test
import org.testng.annotations.Test;

// Base class: starts a browser before each test and closes it afterwards
import com.automation.selenium.base.BaseTest;
// Holds the data provider with the invalid credentials
import com.automation.selenium.data.TestData;
// Page objects used by these tests
import com.automation.selenium.pages.DashboardPage;
import com.automation.selenium.pages.LoginPage;
// Writes steps into the HTML report
import com.automation.selenium.utils.Steps;

/**
 * Logging in and out of OrangeHRM.
 */
// Every test in this class belongs to the "login" group
@Test(groups = "login")
public class LoginTest extends BaseTest {

	/**
	 * Logs in as the administrator and checks the dashboard: page title,
	 * the user name in the top bar and at least one widget.
	 */
	// Also in the "smoke" group (the quick check: -Dgroups=smoke)
	@Test(groups = "smoke", description = "[TC-101] Admin can log in and sees the dashboard")
	public void tc101_adminCanLogIn() {
		// Open the login page, log in with the configured Admin user, wait for the dashboard
		DashboardPage dashboard = loginAsAdmin();

		// Dashboard URL and header
		assertTrue(dashboard.isDisplayed(), "The dashboard should be shown after login");
		// The user's name in the top-right corner is filled in
		assertFalse(dashboard.loggedInUser().isEmpty(), "The logged-in user's name should be shown");
		// The widgets ("Time at Work", "Quick Launch", ...) are there
		assertFalse(dashboard.widgetTitles().isEmpty(), "The dashboard should show its widgets");
	}

	/**
	 * Submits each row of {@link TestData#invalidCredentials()} and checks the
	 * "Invalid credentials" banner; the user must stay on the login page.
	 *
	 * @param caseName  what is wrong with the credentials, shown in the report.
	 * @param username  the user name to submit.
	 * @param password  the password to submit.
	 */
	// Data-driven: TestNG calls this method once per row of the provider (3 runs).
	// dataProviderClass is needed because the provider lives in another class.
	@Test(dataProvider = "invalidCredentials", dataProviderClass = TestData.class,
			description = "[TC-102] Login is rejected for invalid credentials")
	public void tc102_loginIsRejectedForInvalidCredentials(String caseName, String username, String password) {
		// Type the row's user name and password and press Login (submit does not expect success)
		LoginPage login = openLoginPage().submit(username, password);

		// The red banner shows the standard message
		assertEquals(login.errorMessage(), "Invalid credentials", "Error for " + caseName);
		// And we are still on the login page
		assertTrue(login.isDisplayed(), "The login page should still be shown for " + caseName);
	}

	/**
	 * Submits the empty form and checks "Required" under both fields.
	 */
	@Test(description = "[TC-103] Username and password are both required")
	public void tc103_usernameAndPasswordAreRequired() {
		// Press Login with both fields empty
		LoginPage login = openLoginPage().submit("", "");

		// Each field shows its own "Required" message
		assertEquals(login.fieldError("Username"), "Required", "Message under Username");
		assertEquals(login.fieldError("Password"), "Required", "Message under Password");
	}

	/**
	 * Submits only a user name and checks "Required" under the Password field.
	 */
	@Test(description = "[TC-104] Password is required when only the username is entered")
	public void tc104_passwordIsRequired() {
		// User name filled in, password left empty
		LoginPage login = openLoginPage().submit("Admin", "");

		// Only the password is complained about
		assertEquals(login.fieldError("Password"), "Required", "Message under Password");
		// Still on the login page
		assertTrue(login.isDisplayed(), "The login page should still be shown");
	}

	/**
	 * Logs in, logs out through the user menu and checks the login page is shown.
	 */
	@Test(groups = "smoke", description = "[TC-105] Admin can log out and returns to the login page")
	public void tc105_adminCanLogOut() {
		// Log in, then user menu (top right) → Logout
		LoginPage login = loginAsAdmin().logout();

		// Back on the login page
		assertTrue(login.isDisplayed(), "The login page should be shown after logout");
	}

	/**
	 * Logs out, presses the browser's Back button, then reloads. Back alone
	 * may show the browser's cached copy of the dashboard (back-forward
	 * cache); the reload asks the server again, and with the session ended it
	 * must answer with the login page.
	 */
	@Test(description = "[TC-106] Back and reload after logout end on the login page")
	public void tc106_backAfterLogoutStaysLoggedOut() {
		// Log in and out
		LoginPage login = loginAsAdmin().logout();
		// Make sure the logout itself worked before testing Back
		assertTrue(login.isDisplayed(), "The login page should be shown after logout");

		// Record the browser actions in the report
		Steps.log("Press the browser's Back button, then reload the page");
		// Browser Back: may show the dashboard from the browser's cache (no server request)
		driver().navigate().back();
		// Reload: the server is asked again and, with the session gone, sends the login page
		driver().navigate().refresh();
		// The same page object can check again: its fields are looked up fresh each time
		assertTrue(login.isDisplayed(), "After logout, reloading the previous page should end on the login page");
	}

	/**
	 * Opens the dashboard URL in a browser that never logged in. OrangeHRM
	 * must redirect to the login page instead of showing the dashboard.
	 */
	@Test(description = "[TC-107] The dashboard URL redirects to login when not logged in")
	public void tc107_dashboardRequiresLogin() {
		// Go straight to the dashboard URL (visit() does not wait for the dashboard)
		new DashboardPage(driver()).visit();

		// The server must have redirected to the login page
		assertTrue(new LoginPage(driver()).isDisplayed(), "A logged-out browser should be sent to the login page");
	}
}
