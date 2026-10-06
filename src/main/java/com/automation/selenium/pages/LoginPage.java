// Package of the page objects (one class per screen of OrangeHRM)
package com.automation.selenium.pages;

// The error banners come as a list
import java.util.List;

// Special keys (Tab, Enter, ...) for keyboard actions
import org.openqa.selenium.Keys;
// Says in which format to return a screenshot (here BYTES)
import org.openqa.selenium.OutputType;
// Thrown when a found element no longer belongs to the page (e.g. after a reload)
import org.openqa.selenium.StaleElementReferenceException;
// The browser the page works on
import org.openqa.selenium.WebDriver;
// One element on the page (here filled in by PageFactory)
import org.openqa.selenium.WebElement;
// Builds chains of low-level mouse and keyboard events
import org.openqa.selenium.interactions.Actions;
// PageFactory annotation: find the element once and keep it
import org.openqa.selenium.support.CacheLookup;
// PageFactory annotation that says how to find a field's element(s)
import org.openqa.selenium.support.FindBy;
// The locator strategies for the long form @FindBy(how = ..., using = ...)
import org.openqa.selenium.support.How;
// Ready-made wait conditions (visible, clickable, ...)
import org.openqa.selenium.support.ui.ExpectedConditions;

// Settings (the admin user name and password)
import com.automation.selenium.config.Config;
// Writes steps into the HTML report
import com.automation.selenium.utils.Steps;

/**
 * The OrangeHRM login page ({@code /web/index.php/auth/login}).
 * <p>
 * Extends {@link BasePage} directly (not {@link AppPage}): before login there
 * is no side menu or user menu.
 * <p>
 * The fields below also show the different ways to write {@code @FindBy}:
 * {@code name = ...}, {@code css = ...}, {@code tagName = ...} and the long
 * form {@code how = How.CSS, using = ...}.
 */
public class LoginPage extends BasePage {

	// Path of the login page, after APP_PATH
	private static final String PATH = "/auth/login";

	// User name and password inputs of the login form (found by their name attribute)
	@FindBy(name = "username")
	private WebElement usernameField;
	@FindBy(name = "password")
	private WebElement passwordField;
	// The same user name input, but @CacheLookup keeps the first element found
	// instead of looking it up again; only used to show how that goes stale
	@FindBy(name = "username")
	@CacheLookup
	private WebElement cachedUsernameField;
	// Login button below the form (the long how/using form of @FindBy)
	@FindBy(how = How.CSS, using = "button[type='submit']")
	private WebElement loginButton;
	// Banner above the form, e.g. "Invalid credentials"; a list, so "no banner" is an empty list
	// (a single WebElement field would throw NoSuchElementException when there is no banner)
	@FindBy(css = ".oxd-alert-content-text")
	private List<WebElement> errorAlerts;
	// The login form (fields and button), for an element screenshot
	@FindBy(tagName = "form")
	private WebElement loginForm;

	/**
	 * Creates the page object; {@link BasePage}'s constructor fills the
	 * {@code @FindBy} fields.
	 *
	 * @param driver the browser this page works on.
	 */
	public LoginPage(WebDriver driver) {
		// Hand the browser to BasePage
		super(driver);
	}

	/**
	 * Opens the login page and waits until the form is shown.
	 *
	 * @return LoginPage this page.
	 */
	public LoginPage open() {
		// Record the step in the report
		Steps.log("Open the login page");
		// Load base URL + /web/index.php + /auth/login
		openPath(PATH);
		// The page is ready once the user name box is visible
		waitVisible(usernameField);
		// Return this page for chaining, e.g. open().submit(...)
		return this;
	}

	/**
	 * Fills in the form and submits it, without checking the outcome.
	 *
	 * @param username  the user name; empty leaves the field empty.
	 * @param password  the password; empty leaves the field empty.
	 * @return LoginPage this page, for checking error messages.
	 */
	public LoginPage submit(String username, String password) {
		// Record the step; the password is shown as stars, never in clear text
		Steps.log("Log in with username \"" + username + "\" and password \"" + mask(password) + "\"");
		// Type the user name (type() clears the box first)
		type(usernameField, username);
		// Type the password
		type(passwordField, password);
		// Press Login
		click(loginButton);
		// Stay on this page object: the caller checks errors or the URL next
		return this;
	}

	/**
	 * Logs in and waits for the dashboard.
	 *
	 * @param username      the user name.
	 * @param password      the password.
	 * @return DashboardPage the page shown after a successful login.
	 * @throws IllegalStateException if the dashboard is not reached, with the login error shown.
	 */
	public DashboardPage loginAs(String username, String password) {
		// Fill in and press Login
		submit(username, password);
		// A rejected login stays on the login page; fail with its error banner
		// instead of a timeout while waiting for the dashboard.
		// (waitForUrl returns false, it does not throw, when the URL never changes)
		if (!waitForUrl(DashboardPage.PATH)) {
			// Without a login error, name the page instead: it shows server errors such as "HTTP ERROR 500".
			// Take the first banner's text if there is one; otherwise describe where the browser is.
			String error = errorAlerts.stream().map(WebElement::getText).findFirst()
					.orElse("no login error shown; the browser is on \"" + driver.getTitle() + "\" at " + driver.getCurrentUrl());
			// e.g. Login as "Admin" failed: Invalid credentials
			throw new IllegalStateException("Login as \"" + username + "\" failed: " + error);
		}
		// The URL is the dashboard's: create its page object
		DashboardPage dashboard = new DashboardPage(driver);
		// Wait for the widgets too, so the test starts on a fully loaded page
		dashboard.waitUntilLoaded();
		// The test continues on the dashboard
		return dashboard;
	}

	/**
	 * Logs in with the keyboard only, using one {@link Actions} chain: click
	 * into the user name, type it, Tab to the password, type it, Enter submits.
	 *
	 * @param username      the user name.
	 * @param password      the password.
	 * @return DashboardPage the page shown after a successful login.
	 */
	public DashboardPage loginWithKeyboard(String username, String password) {
		// Record the step (password masked)
		Steps.log("Log in with the keyboard: type \"" + username + "\", Tab, type \"" + mask(password) + "\", Enter");
		// Wait until the user name box can take input
		WebElement field = wait.until(ExpectedConditions.elementToBeClickable(usernameField));
		// Nothing happens until perform(); the chain is then sent as one sequence
		new Actions(driver)
				// Put the cursor into the user name box
				.click(field)
				// Type into the box that has the focus
				.sendKeys(username)
				// Tab moves the focus to the password box
				.sendKeys(Keys.TAB)
				// Type the password there
				.sendKeys(password)
				// Send the whole chain to the browser
				.perform();
		// Check where the keys landed before submitting. Keyboard input goes wherever the focus is, so if the
		// focus moved while the keys were sent, the text ends up in the wrong field; fail here with exactly
		// what each field holds, instead of with a vague timeout on the dashboard later.
		// (Seen in full parallel runs on 6 Oct 2026: the password landed in the user name field.)
		String typedUsername = usernameField.getDomProperty("value");
		String typedPassword = passwordField.getDomProperty("value");
		if (!username.equals(typedUsername) || !password.equals(typedPassword)) {
			// The password is never shown, only its length
			throw new IllegalStateException("Keyboard login typed into the wrong fields: the user name field holds \""
					+ typedUsername + "\", the password field holds " + typedPassword.length() + " characters");
		}
		// Both fields are right: Enter submits the form, like pressing Login
		new Actions(driver).sendKeys(Keys.ENTER).perform();
		// Login done: create the dashboard page object
		DashboardPage dashboard = new DashboardPage(driver);
		// Wait until it is loaded
		dashboard.waitUntilLoaded();
		// The test continues on the dashboard
		return dashboard;
	}

	/**
	 * Takes a screenshot of the login form only (Selenium 4 element screenshot).
	 *
	 * @return byte[] the PNG image of the form.
	 */
	public byte[] formScreenshot() {
		// Record the step in the report
		Steps.log("Take a screenshot of the login form only");
		// WebElement.getScreenshotAs (Selenium 4) crops to the element; BYTES = the raw PNG
		return waitVisible(loginForm).getScreenshotAs(OutputType.BYTES);
	}

	/**
	 * Reads a field's placeholder (the grey hint in an empty box).
	 *
	 * @param label   the field label, "Username" or "Password".
	 * @return String the hint shown in the empty field.
	 */
	public String placeholder(String label) {
		// getDomAttribute reads the attribute exactly as written in the HTML
		return waitVisible(input(label)).getDomAttribute("placeholder");
	}

	/**
	 * Reads the button text.
	 *
	 * @return String the text on the login button.
	 */
	public String buttonText() {
		// Waits until visible, then returns the trimmed text
		return textOf(loginButton);
	}

	/**
	 * Logs in with the administrator from config.properties.
	 *
	 * @return DashboardPage the page shown after a successful login.
	 */
	public DashboardPage loginAsAdmin() {
		// User name and password come from config.properties (or -Dusername / -Dpassword)
		return loginAs(Config.get().username(), Config.get().password());
	}

	/**
	 * Checks whether the login page is shown.
	 *
	 * @return boolean true if the login form is shown.
	 */
	public boolean isDisplayed() {
		// URL is the login page's AND the Login button is visible (both return false instead of throwing)
		return waitForUrl(PATH) && isVisible(loginButton);
	}

	/**
	 * Reads the error banner.
	 *
	 * @return String the error banner text, e.g. "Invalid credentials".
	 */
	public String errorMessage() {
		// textsOf waits until at least one banner is visible; take the first
		return textsOf(errorAlerts).get(0);
	}

	/**
	 * Reloads the login page, e.g. to show which element references survive
	 * a new page.
	 *
	 * @return LoginPage this page, with the form shown again.
	 */
	public LoginPage reload() {
		// Record the step in the report
		Steps.log("Reload the login page");
		// Same as the browser's reload button: the old page's elements are thrown away
		driver.navigate().refresh();
		// Wait for the new form
		waitVisible(usernameField);
		// Same page object, still usable (its plain @FindBy fields look up again)
		return this;
	}

	/**
	 * Uses the plain {@code @FindBy} user name field, which PageFactory looks
	 * up again on every call.
	 *
	 * @return boolean true if the field could be read, false if it was stale.
	 */
	public boolean usernameFieldIsUsable() {
		// The plain proxy: always finds the element on the current page
		return isUsable(usernameField, "@FindBy");
	}

	/**
	 * Uses the {@code @FindBy @CacheLookup} user name field, which keeps the
	 * element found on first use, even after the page has been reloaded.
	 *
	 * @return boolean true if the field could be read, false if it was stale.
	 */
	public boolean cachedUsernameFieldIsUsable() {
		// The caching proxy: after a reload it still points at the old page's element
		return isUsable(cachedUsernameField, "@FindBy @CacheLookup");
	}

	/**
	 * Hides a password for the report.
	 *
	 * @param password  the password to hide in the report.
	 * @return String   one * per character.
	 */
	private static String mask(String password) {
		// e.g. "admin123" → "********" (an empty password stays empty)
		return "*".repeat(password.length());
	}

	/**
	 * Tries to use an element and reports whether it is still attached to the page.
	 *
	 * @param field     the element to read.
	 * @param kind      how the field is declared, for the report step.
	 * @return boolean  true if the element still belongs to the page, false if it is stale.
	 */
	private static boolean isUsable(WebElement field, String kind) {
		try {
			// Any call on the element will do; getTagName is cheap and changes nothing
			field.getTagName();
			// It worked: the element belongs to the current page
			Steps.log(kind + " field: usable");
			return true;
		} catch (StaleElementReferenceException e) {
			// The element was found on a page that no longer exists
			Steps.log(kind + " field: StaleElementReferenceException");
			return false;
		}
	}
}
