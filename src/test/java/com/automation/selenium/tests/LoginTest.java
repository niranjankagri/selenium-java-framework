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

	@Test(groups = "smoke", description = "Admin can log in and sees the dashboard")
	public void adminCanLogIn() {
		DashboardPage dashboard = loginAsAdmin();

		assertTrue(dashboard.isDisplayed(), "The dashboard should be shown after login");
		assertFalse(dashboard.loggedInUser().isEmpty(), "The logged-in user's name should be shown");
		assertFalse(dashboard.widgetTitles().isEmpty(), "The dashboard should show its widgets");
	}

	@Test(dataProvider = "invalidCredentials", dataProviderClass = TestData.class,
			description = "Login is rejected for invalid credentials")
	public void loginIsRejectedForInvalidCredentials(String caseName, String username, String password) {
		LoginPage login = openLoginPage().submit(username, password);

		assertEquals(login.errorMessage(), "Invalid credentials", "Error for " + caseName);
		assertTrue(login.isDisplayed(), "The login page should still be shown for " + caseName);
	}

	@Test(description = "Username and password are both required")
	public void usernameAndPasswordAreRequired() {
		LoginPage login = openLoginPage().submit("", "");

		assertEquals(login.fieldError("Username"), "Required", "Message under Username");
		assertEquals(login.fieldError("Password"), "Required", "Message under Password");
	}

	@Test(description = "Password is required when only the username is entered")
	public void passwordIsRequired() {
		LoginPage login = openLoginPage().submit("Admin", "");

		assertEquals(login.fieldError("Password"), "Required", "Message under Password");
		assertTrue(login.isDisplayed(), "The login page should still be shown");
	}

	@Test(groups = "smoke", description = "Admin can log out and returns to the login page")
	public void adminCanLogOut() {
		LoginPage login = loginAsAdmin().logout();

		assertTrue(login.isDisplayed(), "The login page should be shown after logout");
	}
}
