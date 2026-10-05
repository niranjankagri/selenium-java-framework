package com.automation.selenium.pages;

import java.util.List;

import org.openqa.selenium.Keys;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.CacheLookup;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.automation.selenium.config.Config;
import com.automation.selenium.utils.Steps;

/**
 * The OrangeHRM login page ({@code /web/index.php/auth/login}).
 */
public class LoginPage extends BasePage {

	// Path of the login page, after APP_PATH
	private static final String PATH = "/auth/login";

	// User name and password inputs of the login form
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
	@FindBy(css = ".oxd-alert-content-text")
	private List<WebElement> errorAlerts;
	// The login form (fields and button), for an element screenshot
	@FindBy(tagName = "form")
	private WebElement loginForm;

	/**
	 * @param driver the browser this page works on.
	 */
	public LoginPage(WebDriver driver) {
		super(driver);
	}

	/**
	 * Opens the login page and waits until the form is shown.
	 *
	 * @return LoginPage this page.
	 */
	public LoginPage open() {
		Steps.log("Open the login page");
		openPath(PATH);
		waitVisible(usernameField);
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
		Steps.log("Log in with username \"" + username + "\" and password \"" + mask(password) + "\"");
		type(usernameField, username);
		type(passwordField, password);
		click(loginButton);
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
		submit(username, password);
		// A rejected login stays on the login page; fail with its error banner
		// instead of a timeout while waiting for the dashboard
		if (!waitForUrl(DashboardPage.PATH)) {
			// Without a login error, name the page instead: it shows server errors such as "HTTP ERROR 500"
			String error = errorAlerts.stream().map(WebElement::getText).findFirst()
					.orElse("no login error shown; the browser is on \"" + driver.getTitle() + "\" at " + driver.getCurrentUrl());
			throw new IllegalStateException("Login as \"" + username + "\" failed: " + error);
		}
		DashboardPage dashboard = new DashboardPage(driver);
		dashboard.waitUntilLoaded();
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
		Steps.log("Log in with the keyboard: type \"" + username + "\", Tab, type \"" + mask(password) + "\", Enter");
		WebElement field = wait.until(ExpectedConditions.elementToBeClickable(usernameField));
		// Nothing happens until perform(); the chain is then sent as one sequence
		new Actions(driver)
				.click(field)
				.sendKeys(username)
				.sendKeys(Keys.TAB)
				.sendKeys(password)
				.sendKeys(Keys.ENTER)
				.perform();
		DashboardPage dashboard = new DashboardPage(driver);
		dashboard.waitUntilLoaded();
		return dashboard;
	}

	/**
	 * Takes a screenshot of the login form only (Selenium 4 element screenshot).
	 *
	 * @return byte[] the PNG image of the form.
	 */
	public byte[] formScreenshot() {
		Steps.log("Take a screenshot of the login form only");
		return waitVisible(loginForm).getScreenshotAs(OutputType.BYTES);
	}

	/**
	 * @param label   the field label, "Username" or "Password".
	 * @return String the hint shown in the empty field.
	 */
	public String placeholder(String label) {
		return waitVisible(input(label)).getDomAttribute("placeholder");
	}

	/**
	 * @return String the text on the login button.
	 */
	public String buttonText() {
		return textOf(loginButton);
	}

	/**
	 * Logs in with the administrator from config.properties.
	 *
	 * @return DashboardPage the page shown after a successful login.
	 */
	public DashboardPage loginAsAdmin() {
		return loginAs(Config.get().username(), Config.get().password());
	}

	/**
	 * @return boolean true if the login form is shown.
	 */
	public boolean isDisplayed() {
		return waitForUrl(PATH) && isVisible(loginButton);
	}

	/**
	 * @return String the error banner text, e.g. "Invalid credentials".
	 */
	public String errorMessage() {
		return textsOf(errorAlerts).get(0);
	}

	/**
	 * Reloads the login page, e.g. to show which element references survive
	 * a new page.
	 *
	 * @return LoginPage this page, with the form shown again.
	 */
	public LoginPage reload() {
		Steps.log("Reload the login page");
		driver.navigate().refresh();
		waitVisible(usernameField);
		return this;
	}

	/**
	 * Uses the plain {@code @FindBy} user name field, which PageFactory looks
	 * up again on every call.
	 *
	 * @return boolean true if the field could be read, false if it was stale.
	 */
	public boolean usernameFieldIsUsable() {
		return isUsable(usernameField, "@FindBy");
	}

	/**
	 * Uses the {@code @FindBy @CacheLookup} user name field, which keeps the
	 * element found on first use, even after the page has been reloaded.
	 *
	 * @return boolean true if the field could be read, false if it was stale.
	 */
	public boolean cachedUsernameFieldIsUsable() {
		return isUsable(cachedUsernameField, "@FindBy @CacheLookup");
	}

	/**
	 * @param password  the password to hide in the report.
	 * @return String   one * per character.
	 */
	private static String mask(String password) {
		return "*".repeat(password.length());
	}

	/**
	 * @param field     the element to read.
	 * @param kind      how the field is declared, for the report step.
	 * @return boolean  true if the element still belongs to the page, false if it is stale.
	 */
	private static boolean isUsable(WebElement field, String kind) {
		try {
			field.getTagName();
			Steps.log(kind + " field: usable");
			return true;
		} catch (StaleElementReferenceException e) {
			Steps.log(kind + " field: StaleElementReferenceException");
			return false;
		}
	}
}
