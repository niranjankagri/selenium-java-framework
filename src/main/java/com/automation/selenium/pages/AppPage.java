package com.automation.selenium.pages;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.automation.selenium.utils.Steps;

/**
 * Parent of every page shown after login. Holds the parts they share: the
 * side menu, the page title in the top bar and the user menu.
 */
public abstract class AppPage extends BasePage {

	// Module name at the top left, e.g. "Dashboard", "Admin", "PIM"
	private static final By MODULE_TITLE = By.cssSelector(".oxd-topbar-header-breadcrumb-module");
	// User menu at the top right (picture and name of the logged-in user); a click opens it
	private static final By USER_MENU = By.cssSelector(".oxd-userdropdown-tab");
	// Name of the logged-in user inside the user menu tab
	private static final By USER_NAME = By.cssSelector(".oxd-userdropdown-name");
	// "Logout" entry of the opened user menu
	private static final By LOGOUT = By.xpath("//ul[contains(@class,'oxd-dropdown-menu')]//a[normalize-space()='Logout']");
	// Every entry of the side menu (a link with the module name)
	private static final By MENU_ITEMS = By.cssSelector("a.oxd-main-menu-item");

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
	 * Reads a list of elements: every side menu entry, top to bottom.
	 *
	 * @return List the module names, e.g. [Admin, PIM, Leave, …].
	 */
	public List<String> menuItems() {
		return menuEntries().stream().map(WebElement::getText).map(String::trim).toList();
	}

	/**
	 * @return List the absolute URL of every side menu entry, top to bottom.
	 */
	public List<String> menuLinks() {
		// The href property is always absolute; the href attribute may be relative
		return menuEntries().stream().map(link -> link.getDomProperty("href")).toList();
	}

	/**
	 * Reads the load state through JavaScript; WebDriver has no method for it.
	 *
	 * @return String document.readyState: "loading", "interactive" or "complete".
	 */
	public String readyState() {
		return (String) runScript("return document.readyState;");
	}

	/**
	 * @return String the page title, read through JavaScript instead of {@code driver.getTitle()}.
	 */
	public String titleFromScript() {
		return (String) runScript("return document.title;");
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
	 * @return List the side menu links, once they are all visible.
	 */
	private List<WebElement> menuEntries() {
		return wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(MENU_ITEMS));
	}

	/**
	 * @param item the side menu entry to click, e.g. "Admin".
	 */
	private void openMenu(String item) {
		Steps.log("Open \"" + item + "\" from the side menu");
		// Side menu links are matched by their visible text
		click(By.xpath("//a[contains(@class,'oxd-main-menu-item')][normalize-space()='" + item + "']"));
	}
}
