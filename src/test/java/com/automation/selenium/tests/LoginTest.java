package com.automation.selenium.tests;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.automation.selenium.base.BaseTest;
import com.automation.selenium.data.TestData;
import com.automation.selenium.pages.DashboardPage;
import com.automation.selenium.pages.LoginPage;

/**
 * Logging in and out of OrangeHRM.
 */
@Test(groups = "login")
public class LoginTest extends BaseTest {

	/**
	 * Logs in as the administrator and checks the dashboard: page title,
	 * the user name in the top bar and at least one widget.
	 */
	@Test(groups = "smoke", description = "Admin can log in and sees the dashboard")
	public void adminCanLogIn() {
		DashboardPage dashboard = loginAsAdmin();

		assertTrue(dashboard.isDisplayed(), "The dashboard should be shown after login");
		assertFalse(dashboard.loggedInUser().isEmpty(), "The logged-in user's name should be shown");
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
	@Test(dataProvider = "invalidCredentials", dataProviderClass = TestData.class,
			description = "Login is rejected for invalid credentials")
	public void loginIsRejectedForInvalidCredentials(String caseName, String username, String password) {
		LoginPage login = openLoginPage().submit(username, password);

		assertEquals(login.errorMessage(), "Invalid credentials", "Error for " + caseName);
		assertTrue(login.isDisplayed(), "The login page should still be shown for " + caseName);
	}

	/**
	 * Submits the empty form and checks "Required" under both fields.
	 */
	@Test(description = "Username and password are both required")
	public void usernameAndPasswordAreRequired() {
		LoginPage login = openLoginPage().submit("", "");

		assertEquals(login.fieldError("Username"), "Required", "Message under Username");
		assertEquals(login.fieldError("Password"), "Required", "Message under Password");
	}

	/**
	 * Submits only a user name and checks "Required" under the Password field.
	 */
	@Test(description = "Password is required when only the username is entered")
	public void passwordIsRequired() {
		LoginPage login = openLoginPage().submit("Admin", "");

		assertEquals(login.fieldError("Password"), "Required", "Message under Password");
		assertTrue(login.isDisplayed(), "The login page should still be shown");
	}

	/**
	 * Logs in, logs out through the user menu and checks the login page is shown.
	 */
	@Test(groups = "smoke", description = "Admin can log out and returns to the login page")
	public void adminCanLogOut() {
		LoginPage login = loginAsAdmin().logout();

		assertTrue(login.isDisplayed(), "The login page should be shown after logout");
	}
}
