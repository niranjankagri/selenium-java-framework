package com.automation.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.automation.selenium.utils.Steps;

/**
 * Parent of every page shown after login. Holds the parts they share: the
 * side menu, the page title in the top bar and the user menu.
 */
public abstract class AppPage extends BasePage {

	// Module name at the top left, e.g. "Dashboard", "Admin", "PIM"
	private static final By MODULE_TITLE = By.cssSelector(".oxd-topbar-header-breadcrumb-module");
	// Name of the logged-in user at the top right; opens the user menu
	private static final By USER_MENU = By.cssSelector(".oxd-userdropdown-tab");
	private static final By USER_NAME = By.cssSelector(".oxd-userdropdown-name");
	private static final By LOGOUT = By.xpath("//ul[contains(@class,'oxd-dropdown-menu')]//a[normalize-space()='Logout']");

	/**
	 * @param driver the browser this page works on.
	 */
	protected AppPage(WebDriver driver) {
		super(driver);
	}

	/**
	 * @return String the module title in the top bar, e.g. "Admin".
	 */
	public String moduleTitle() {
		return textOf(MODULE_TITLE);
	}

	/**
	 * @return String the name of the logged-in user, as shown at the top right.
	 */
	public String loggedInUser() {
		return textOf(USER_NAME);
	}

	/**
	 * Opens the System Users page from the side menu.
	 *
	 * @return SystemUsersPage the opened page.
	 */
	public SystemUsersPage openAdmin() {
		openMenu("Admin");
		SystemUsersPage page = new SystemUsersPage(driver);
		page.waitUntilLoaded();
		return page;
	}

	/**
	 * Opens the Employee List from the side menu.
	 *
	 * @return EmployeeListPage the opened page.
	 */
	public EmployeeListPage openPim() {
		openMenu("PIM");
		EmployeeListPage page = new EmployeeListPage(driver);
		page.waitUntilLoaded();
		return page;
	}

	/**
	 * Logs out through the user menu.
	 *
	 * @return LoginPage the login page shown after logging out.
	 */
	public LoginPage logout() {
		Steps.log("Log out from the user menu");
		click(USER_MENU);
		click(LOGOUT);
		return new LoginPage(driver);
	}

	/**
	 * @param item the side menu entry to click, e.g. "Admin".
	 */
	private void openMenu(String item) {
		Steps.log("Open \"" + item + "\" from the side menu");
		click(By.xpath("//a[contains(@class,'oxd-main-menu-item')][normalize-space()='" + item + "']"));
	}
}
