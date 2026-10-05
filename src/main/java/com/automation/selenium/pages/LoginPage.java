package com.automation.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
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
	private static final By USERNAME = By.name("username");
	private static final By PASSWORD = By.name("password");
	// Login button below the form
	private static final By LOGIN_BUTTON = By.cssSelector("button[type='submit']");
	// Banner above the form, e.g. "Invalid credentials"
	private static final By ERROR_ALERT = By.cssSelector(".oxd-alert-content-text");
	// The login form (fields and button), for an element screenshot
	private static final By LOGIN_FORM = By.tagName("form");

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
		waitVisible(USERNAME);
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
		type(USERNAME, username);
		type(PASSWORD, password);
		click(LOGIN_BUTTON);
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
			String error = driver.findElements(ERROR_ALERT).stream().map(WebElement::getText).findFirst().orElse("no error shown");
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
		WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(USERNAME));
		// Nothing happens until perform(); the chain is then sent as one sequence
		new Actions(driver)
				.click(usernameField)
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
		return waitVisible(LOGIN_FORM).getScreenshotAs(OutputType.BYTES);
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
		return textOf(LOGIN_BUTTON);
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
		return waitForUrl(PATH) && isVisible(LOGIN_BUTTON);
	}

	/**
	 * @return String the error banner text, e.g. "Invalid credentials".
	 */
	public String errorMessage() {
		return textOf(ERROR_ALERT);
	}

	/**
	 * @param password  the password to hide in the report.
	 * @return String   one * per character.
	 */
	private static String mask(String password) {
		return "*".repeat(password.length());
	}
}
