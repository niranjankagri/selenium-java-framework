package com.automation.selenium.tests;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openqa.selenium.WindowType;
import org.testng.annotations.Test;

import com.automation.selenium.base.BaseTest;
import com.automation.selenium.config.Config;
import com.automation.selenium.pages.DashboardPage;
import com.automation.selenium.utils.Links;
import com.automation.selenium.utils.Steps;

/**
 * Selenium scenarios that interviews ask you to code, solved on OrangeHRM:
 * a list of elements, broken links, custom drop-down options, keyboard
 * actions, a new tab, JavaScript and an element screenshot.
 * <p>
 * The locators and WebDriver calls live in the page objects; each test
 * explains the technique it shows. Window handling is a driver-level task,
 * so that test uses {@code driver()} directly.
 */
@Test(groups = "interview")
public class SeleniumScenariosTest extends BaseTest {

	// Modules the Admin user always has in the side menu (the full list may grow)
	private static final List<String> CORE_MODULES = List.of("Admin", "PIM", "Leave", "Time", "Dashboard");
	// Options of the User Role filter, in display order
	private static final List<String> ROLE_OPTIONS = List.of("-- Select --", "Admin", "ESS");
	// Every PNG file starts with these four bytes
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
		List<String> items = loginAsAdmin().menuItems();
		Steps.log("Side menu has " + items.size() + " entries: " + items);

		assertTrue(items.containsAll(CORE_MODULES), "Side menu " + items + " should contain " + CORE_MODULES);
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
		List<String> links = loginAsAdmin().menuLinks();
		assertFalse(links.isEmpty(), "The side menu should have links");

		// Link → status of every broken link, so the failure lists them all at once
		Map<String, Integer> broken = new LinkedHashMap<>();
		for (String link : links) {
			int status = Links.statusCode(link);
			Steps.log("Check \"" + link + "\": HTTP " + status);
			if (Links.isBroken(status)) {
				broken.put(link, status);
			}
		}
		assertTrue(broken.isEmpty(), "Broken links: " + broken);
	}

	/**
	 * Custom drop-down: OrangeHRM's drop-downs are div elements, so Selenium's
	 * Select class cannot read them. The page opens the list, reads every
	 * option and closes it again.
	 */
	@Test(description = "Scenario: read the options of a custom drop-down")
	public void userRoleDropdownOffersAdminAndEss() {
		List<String> options = loginAsAdmin().openAdmin().roleOptions();
		Steps.log("User Role options: " + options);

		assertEquals(options, ROLE_OPTIONS, "User Role options");
	}

	/**
	 * Actions class: logs in with keyboard events only (type, Tab, type,
	 * Enter), the way a keyboard user would, instead of clicking the button.
	 */
	@Test(description = "Scenario: keyboard actions with the Actions class (Tab and Enter)")
	public void adminCanLogInWithTheKeyboard() {
		DashboardPage dashboard = openLoginPage().loginWithKeyboard(Config.get().username(), Config.get().password());

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
		loginAsAdmin();
		String firstTab = driver().getWindowHandle();

		Steps.log("Open a new tab and switch to it");
		driver().switchTo().newWindow(WindowType.TAB);
		DashboardPage secondTab = new DashboardPage(driver()).open();
		assertTrue(secondTab.isDisplayed(), "The new tab should share the login session");
		assertEquals(driver().getWindowHandles().size(), 2, "Open tabs");

		Steps.log("Close the new tab and switch back to the first one");
		driver().close();
		// After close() the driver points at no window until we switch
		driver().switchTo().window(firstTab);
		assertEquals(driver().getWindowHandles(), Set.of(firstTab), "Only the first tab should be left");
		assertTrue(new DashboardPage(driver()).isDisplayed(), "The first tab should still show the dashboard");
	}

	/**
	 * JavascriptExecutor: reads values WebDriver has no method for. The
	 * page's load state must be "complete", and the title read by JavaScript
	 * must match driver.getTitle().
	 */
	@Test(description = "Scenario: run JavaScript in the page (JavascriptExecutor)")
	public void javascriptReadsThePageState() {
		DashboardPage dashboard = loginAsAdmin();
		String readyState = dashboard.readyState();
		String title = dashboard.titleFromScript();
		Steps.log("document.readyState = \"" + readyState + "\", document.title = \"" + title + "\"");

		assertEquals(readyState, "complete", "document.readyState");
		assertEquals(title, driver().getTitle(), "Title from JavaScript and from WebDriver");
	}

	/**
	 * Element screenshot (Selenium 4): captures only the login form, checks
	 * the bytes are a real PNG, and saves it to target/screenshots.
	 *
	 * @throws IOException if the file cannot be written.
	 */
	@Test(description = "Scenario: take a screenshot of one element")
	public void loginFormScreenshotIsAPng() throws IOException {
		byte[] png = openLoginPage().formScreenshot();

		assertEquals(Arrays.copyOf(png, PNG_SIGNATURE.length), PNG_SIGNATURE, "PNG file signature");
		Files.createDirectories(FORM_SCREENSHOT.getParent());
		Files.write(FORM_SCREENSHOT, png);
		Steps.log("Saved \"" + FORM_SCREENSHOT + "\" (" + png.length + " bytes)");
	}
}
