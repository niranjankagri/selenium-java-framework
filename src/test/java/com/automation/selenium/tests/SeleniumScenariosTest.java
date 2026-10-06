// Package of the test classes
package com.automation.selenium.tests;

// TestNG assertions: each fails the test with the given message when the check is false
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

// Thrown when the screenshot file cannot be written
import java.io.IOException;
// File helpers (create folders, write bytes)
import java.nio.file.Files;
// A file or folder path
import java.nio.file.Path;
// Arrays.copyOf: take the first bytes of the PNG
import java.util.Arrays;
// A map that keeps insertion order, so broken links are listed in menu order
import java.util.LinkedHashMap;
// Lists, maps and sets used in the checks
import java.util.List;
import java.util.Map;
import java.util.Set;

// TAB or WINDOW, for driver.switchTo().newWindow(...)
import org.openqa.selenium.WindowType;
// Marks a method (or every public method of a class) as a TestNG test
import org.testng.annotations.Test;

// Base class: starts a browser before each test and closes it afterwards
import com.automation.selenium.base.BaseTest;
// Settings (user name, password)
import com.automation.selenium.config.Config;
// Page objects used by these tests
import com.automation.selenium.pages.DashboardPage;
import com.automation.selenium.pages.LoginPage;
// HTTP status check for links
import com.automation.selenium.utils.Links;
// Writes steps into the HTML report
import com.automation.selenium.utils.Steps;

/**
 * Selenium scenarios that interviews ask you to code, solved on OrangeHRM:
 * a list of elements, broken links, custom drop-down options, keyboard
 * actions, a new tab, JavaScript, PageFactory caching and an element
 * screenshot.
 * <p>
 * The locators and WebDriver calls live in the page objects; each test
 * explains the technique it shows. Window handling is a driver-level task,
 * so that test uses {@code driver()} directly.
 */
// Every test in this class belongs to the "interview" group
@Test(groups = "interview")
public class SeleniumScenariosTest extends BaseTest {

	// Modules the Admin user always has in the side menu (the full list may grow).
	// "Leave" is not in the list: on the shared demo site other users can switch modules off, and it disappeared on 6 Oct 2026.
	private static final List<String> CORE_MODULES = List.of("Admin", "PIM", "Time", "Dashboard");
	// Options of the User Role filter, in display order
	private static final List<String> ROLE_OPTIONS = List.of("-- Select --", "Admin", "ESS");
	// Every PNG file starts with these four bytes (0x89 needs a cast: Java bytes are signed, -128..127)
	private static final byte[] PNG_SIGNATURE = { (byte) 0x89, 'P', 'N', 'G' };
	// Where the element screenshot is saved
	private static final Path FORM_SCREENSHOT = Path.of("target", "screenshots", "login-form.png");

	/**
	 * findElements: reads every side menu entry into a list of names, then
	 * checks the core modules are there and no entry is blank. The check
	 * uses containsAll, not equals, so a new module on the demo site does not
	 * break the test.
	 */
	@Test(description = "Scenario: read a list of elements (the side menu)")
	public void sideMenuListsTheCoreModules() {
		// Log in and read the text of every side menu link
		List<String> items = loginAsAdmin().menuItems();
		// Show the full list in the report
		Steps.log("Side menu has " + items.size() + " entries: " + items);

		// Every core module must be somewhere in the list (order and extras do not matter)
		assertTrue(items.containsAll(CORE_MODULES), "Side menu " + items + " should contain " + CORE_MODULES);
		// No entry may be empty or only spaces (String::isBlank = s -> s.isBlank())
		assertTrue(items.stream().noneMatch(String::isBlank), "Every menu entry should have a name");
	}

	/**
	 * Broken links: collects the URL of every side menu link and requests each
	 * one over HTTP (Selenium cannot read status codes). A status of 400 or
	 * more is broken. The HTTP client has no login cookie, so OrangeHRM
	 * redirects it to the login page; a 200 after that redirect still proves
	 * the link leads somewhere.
	 */
	@Test(description = "Scenario: find broken links (the side menu)")
	public void sideMenuHasNoBrokenLinks() {
		// Log in and read the href of every side menu link
		List<String> links = loginAsAdmin().menuLinks();
		// Guard: an empty list would make the loop below pass without checking anything
		assertFalse(links.isEmpty(), "The side menu should have links");

		// Link → status of every broken link, so the failure lists them all at once
		Map<String, Integer> broken = new LinkedHashMap<>();
		// Check each link in turn
		for (String link : links) {
			// Request the URL outside the browser and get the final HTTP status
			int status = Links.statusCode(link);
			// One report step per link, with its status
			Steps.log("Check \"" + link + "\": HTTP " + status);
			// 400 or more = broken: remember it and keep checking the rest
			if (Links.isBroken(status)) {
				broken.put(link, status);
			}
		}
		// Fail once at the end, naming every broken link
		assertTrue(broken.isEmpty(), "Broken links: " + broken);
	}

	/**
	 * Custom drop-down: OrangeHRM's drop-downs are div elements, so Selenium's
	 * Select class cannot read them. The page opens the list, reads every
	 * option and closes it again.
	 */
	@Test(description = "Scenario: read the options of a custom drop-down")
	public void userRoleDropdownOffersAdminAndEss() {
		// Log in, open Admin, read all options of the User Role drop-down
		List<String> options = loginAsAdmin().openAdmin().roleOptions();
		// Show them in the report
		Steps.log("User Role options: " + options);

		// Exactly these options, in this order
		assertEquals(options, ROLE_OPTIONS, "User Role options");
	}

	/**
	 * Actions class: logs in with keyboard events only (type, Tab, type,
	 * Enter), the way a keyboard user would, instead of clicking the button.
	 */
	@Test(description = "Scenario: keyboard actions with the Actions class (Tab and Enter)")
	public void adminCanLogInWithTheKeyboard() {
		// Open the login page and log in using only key presses
		DashboardPage dashboard = openLoginPage().loginWithKeyboard(Config.get().username(), Config.get().password());

		// Enter must have submitted the form
		assertTrue(dashboard.isDisplayed(), "Enter in the password field should log in");
	}

	/**
	 * Windows and tabs: remembers the first tab's handle, opens a second tab
	 * (Selenium 4 newWindow), works there, closes it and switches back. Tabs
	 * share the session cookie, so the new tab is logged in without a second
	 * login.
	 */
	@Test(description = "Scenario: open a new tab and switch between windows")
	public void dashboardOpensInASecondTab() {
		// Log in in the first tab
		loginAsAdmin();
		// A handle is the ID of a window/tab; keep the first one to come back to it
		String firstTab = driver().getWindowHandle();

		// Record the step in the report
		Steps.log("Open a new tab and switch to it");
		// Selenium 4: opens a new tab AND switches the driver to it
		driver().switchTo().newWindow(WindowType.TAB);
		// Load the dashboard URL in the new tab (no login: the cookie is shared)
		DashboardPage secondTab = new DashboardPage(driver()).open();
		// The new tab shows the dashboard, so it is logged in
		assertTrue(secondTab.isDisplayed(), "The new tab should share the login session");
		// getWindowHandles() = the handles of all open tabs and windows
		assertEquals(driver().getWindowHandles().size(), 2, "Open tabs");

		// Record the step in the report
		Steps.log("Close the new tab and switch back to the first one");
		// close() closes only the current tab (quit() would close the whole browser)
		driver().close();
		// After close() the driver points at no window until we switch
		driver().switchTo().window(firstTab);
		// Only the first tab is left
		assertEquals(driver().getWindowHandles(), Set.of(firstTab), "Only the first tab should be left");
		// And it still shows the dashboard
		assertTrue(new DashboardPage(driver()).isDisplayed(), "The first tab should still show the dashboard");
	}

	/**
	 * JavascriptExecutor: reads values WebDriver has no method for. The
	 * page's load state must be "complete", and the title read by JavaScript
	 * must match driver.getTitle().
	 */
	@Test(description = "Scenario: run JavaScript in the page (JavascriptExecutor)")
	public void javascriptReadsThePageState() {
		// Log in
		DashboardPage dashboard = loginAsAdmin();
		// document.readyState, read by JavaScript
		String readyState = dashboard.readyState();
		// document.title, read by JavaScript
		String title = dashboard.titleFromScript();
		// Show both values in the report
		Steps.log("document.readyState = \"" + readyState + "\", document.title = \"" + title + "\"");

		// The page has finished loading
		assertEquals(readyState, "complete", "document.readyState");
		// JavaScript and WebDriver agree on the title
		assertEquals(title, driver().getTitle(), "Title from JavaScript and from WebDriver");
	}

	/**
	 * PageFactory: a plain {@code @FindBy} field is a proxy that finds the
	 * element again on every call, so it still works after a reload. With
	 * {@code @CacheLookup} the proxy keeps the element it found first, which
	 * belongs to the old page after the reload and throws
	 * StaleElementReferenceException.
	 */
	@Test(description = "Scenario: PageFactory @FindBy vs @CacheLookup after a reload")
	public void findByFieldSurvivesAReloadButCacheLookupGoesStale() {
		// Open the login page
		LoginPage login = openLoginPage();
		// Before the reload, the plain field works...
		assertTrue(login.usernameFieldIsUsable(), "@FindBy field before the reload");
		// The first use makes @CacheLookup remember this page's element
		assertTrue(login.cachedUsernameFieldIsUsable(), "@CacheLookup field before the reload");

		// Reload: the browser builds a new page with new elements
		login.reload();

		// The plain field looks the element up again on the new page: still works
		assertTrue(login.usernameFieldIsUsable(), "@FindBy should find the field on the new page");
		// The cached field still points at the old page's element: stale
		assertFalse(login.cachedUsernameFieldIsUsable(), "@CacheLookup should still hold the old, stale element");
	}

	/**
	 * Element screenshot (Selenium 4): captures only the login form, checks
	 * the bytes are a real PNG, and saves it to target/screenshots.
	 *
	 * @throws IOException if the file cannot be written.
	 */
	@Test(description = "Scenario: take a screenshot of one element")
	public void loginFormScreenshotIsAPng() throws IOException {
		// Open the login page and take a picture of the form only
		byte[] png = openLoginPage().formScreenshot();

		// The first four bytes must be the PNG signature (assertEquals compares arrays element by element)
		assertEquals(Arrays.copyOf(png, PNG_SIGNATURE.length), PNG_SIGNATURE, "PNG file signature");
		// Create target/screenshots if needed
		Files.createDirectories(FORM_SCREENSHOT.getParent());
		// Save the image so it can be looked at after the run
		Files.write(FORM_SCREENSHOT, png);
		// Record the file name and size in the report
		Steps.log("Saved \"" + FORM_SCREENSHOT + "\" (" + png.length + " bytes)");
	}
}
