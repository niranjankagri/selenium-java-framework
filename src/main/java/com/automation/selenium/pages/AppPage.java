// Package of the page objects (one class per screen of OrangeHRM)
package com.automation.selenium.pages;

// Menu entries and their texts come as lists
import java.util.List;

// A locator; used for the menu item, which depends on its name
import org.openqa.selenium.By;
// The browser the page works on
import org.openqa.selenium.WebDriver;
// One element on the page (here filled in by PageFactory)
import org.openqa.selenium.WebElement;
// PageFactory annotation that says how to find a field's element(s)
import org.openqa.selenium.support.FindBy;
// Ready-made wait conditions (visible, clickable, ...)
import org.openqa.selenium.support.ui.ExpectedConditions;

// Writes steps into the HTML report
import com.automation.selenium.utils.Steps;

/**
 * Parent of every page shown after login. Holds the parts they share: the
 * side menu, the page title in the top bar and the user menu.
 * <p>
 * Inheritance chain: {@link BasePage} (waits and element helpers) →
 * {@code AppPage} (the frame around every logged-in page) → the concrete
 * page, e.g. {@link DashboardPage}.
 */
// abstract: only its subclasses describe a real page
public abstract class AppPage extends BasePage {

	// Module name at the top left, e.g. "Dashboard", "Admin", "PIM"
	@FindBy(css = ".oxd-topbar-header-breadcrumb-module")
	private WebElement moduleTitle;
	// User menu at the top right (picture and name of the logged-in user); a click opens it
	@FindBy(css = ".oxd-userdropdown-tab")
	private WebElement userMenu;
	// Name of the logged-in user inside the user menu tab
	@FindBy(css = ".oxd-userdropdown-name")
	private WebElement userName;
	// "Logout" entry of the opened user menu (XPath: found by its visible text inside the drop-down list)
	@FindBy(xpath = "//ul[contains(@class,'oxd-dropdown-menu')]//a[normalize-space()='Logout']")
	private WebElement logoutLink;
	// Every entry of the side menu (a link with the module name)
	@FindBy(css = "a.oxd-main-menu-item")
	private List<WebElement> menuEntries;

	/**
	 * Called by the subclass constructors; {@link BasePage}'s constructor
	 * fills the {@code @FindBy} fields of this class and the subclass.
	 *
	 * @param driver the browser this page works on.
	 */
	protected AppPage(WebDriver driver) {
		// Hand the browser to BasePage
		super(driver);
	}

	/**
	 * Reads the module name in the top bar.
	 *
	 * @return String the module title in the top bar, e.g. "Admin".
	 */
	public String moduleTitle() {
		// textOf waits until the element is visible, then returns its text
		return textOf(moduleTitle);
	}

	/**
	 * Reads the user name at the top right.
	 *
	 * @return String the name of the logged-in user, as shown at the top right.
	 */
	public String loggedInUser() {
		return textOf(userName);
	}

	/**
	 * Reads a list of elements: every side menu entry, top to bottom.
	 *
	 * @return List the module names, e.g. [Admin, PIM, Leave, …].
	 */
	public List<String> menuItems() {
		// The List<WebElement> field is the "findElements" form of @FindBy
		return textsOf(menuEntries);
	}

	/**
	 * Reads the link target of every side menu entry.
	 *
	 * @return List the absolute URL of every side menu entry, top to bottom.
	 */
	public List<String> menuLinks() {
		// The href property is always absolute; the href attribute may be relative
		return visibleMenuEntries().stream().map(link -> link.getDomProperty("href")).toList();
	}

	/**
	 * Reads the load state through JavaScript; WebDriver has no method for it.
	 *
	 * @return String document.readyState: "loading", "interactive" or "complete".
	 */
	public String readyState() {
		// "return" makes executeScript hand the value back to Java; the cast is needed because it returns Object
		return (String) runScript("return document.readyState;");
	}

	/**
	 * Reads the title through JavaScript, as a JavascriptExecutor example.
	 *
	 * @return String the page title, read through JavaScript instead of {@code driver.getTitle()}.
	 */
	public String titleFromScript() {
		// Same value as driver.getTitle(), read in the page instead
		return (String) runScript("return document.title;");
	}

	/**
	 * Opens the System Users page from the side menu.
	 *
	 * @return SystemUsersPage the opened page.
	 */
	public SystemUsersPage openAdmin() {
		// Click "Admin" in the side menu
		openMenu("Admin");
		// The browser now shows System Users: create its page object
		SystemUsersPage page = new SystemUsersPage(driver);
		// Wait until it can be used
		page.waitUntilLoaded();
		// The test continues on that page
		return page;
	}

	/**
	 * Opens the Employee List from the side menu.
	 *
	 * @return EmployeeListPage the opened page.
	 */
	public EmployeeListPage openPim() {
		// Click "PIM" in the side menu
		openMenu("PIM");
		// The browser now shows the Employee List: create its page object
		EmployeeListPage page = new EmployeeListPage(driver);
		// Wait until it can be used
		page.waitUntilLoaded();
		// The test continues on that page
		return page;
	}

	/**
	 * Logs out through the user menu.
	 *
	 * @return LoginPage the login page shown after logging out.
	 */
	public LoginPage logout() {
		// Record the step in the report
		Steps.log("Log out from the user menu");
		// Open the user menu (top right)...
		click(userMenu);
		// ...and click Logout in it (click waits until the entry is clickable)
		click(logoutLink);
		// The browser now shows the login page; the caller checks it with isDisplayed()
		return new LoginPage(driver);
	}

	/**
	 * Waits for the side menu links.
	 *
	 * @return List the side menu links, once they are all visible.
	 */
	private List<WebElement> visibleMenuEntries() {
		// Waits until every element of the list is visible, then returns the list
		return wait.until(ExpectedConditions.visibilityOfAllElements(menuEntries));
	}

	/**
	 * Clicks one entry of the side menu.
	 *
	 * @param item the side menu entry to click, e.g. "Admin".
	 */
	private void openMenu(String item) {
		// Record the step in the report
		Steps.log("Open \"" + item + "\" from the side menu");
		// Side menu links are matched by their visible text.
		// The name is part of the locator, so it is a By built here, not a @FindBy field.
		click(By.xpath("//a[contains(@class,'oxd-main-menu-item')][normalize-space()='" + item + "']"));
	}
}
