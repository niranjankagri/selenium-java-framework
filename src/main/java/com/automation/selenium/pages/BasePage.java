package com.automation.selenium.pages;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.automation.selenium.config.Config;

/**
 * Parent of all page objects. Wraps the WebDriver actions in explicit waits,
 * so no page needs {@code Thread.sleep} or an implicit wait, and knows the
 * OrangeHRM widgets used on every page (labelled fields, drop-downs, tables,
 * toasts and the loading spinner).
 * <p>
 * Pages declare their fixed elements as {@link FindBy @FindBy} fields, which
 * the constructor fills with Selenium's {@link PageFactory}. Each field holds
 * a proxy that looks the element up again on every call, so an element the
 * page has re-rendered never goes stale. Locators built from a value (a field
 * label, a button text, a table cell) cannot be annotations and stay methods
 * that return a {@link By}.
 */
public abstract class BasePage {

	// Every OrangeHRM page lives under this path; page PATHs are relative to it
	protected static final String APP_PATH = "/web/index.php";

	// How long to wait for the spinner to show up; fast responses may never show it
	private static final Duration LOADER_APPEAR_TIMEOUT = Duration.ofSeconds(2);
	// Cells of one table row, searched inside that row (a relative locator cannot be a field)
	private static final By TABLE_CELLS = By.cssSelector(".oxd-table-cell");

	// Spinner OrangeHRM shows while a form or table is loading (an empty list when there is none)
	@FindBy(css = ".oxd-loading-spinner, .oxd-form-loader")
	private List<WebElement> loaders;
	// Message shown at the bottom right after a save or delete
	@FindBy(css = ".oxd-toast .oxd-text--toast-message")
	private WebElement toastMessage;
	// Column titles and rows of a results table
	@FindBy(css = ".oxd-table-header .oxd-table-th")
	private List<WebElement> tableHeaders;
	@FindBy(css = ".oxd-table-body .oxd-table-card")
	private List<WebElement> tableRowElements;
	// Options of an opened drop-down list
	@FindBy(css = "div[role='listbox'] div[role='option']")
	private List<WebElement> dropdownOptions;
	// Validation messages under form fields, e.g. "Required"
	@FindBy(css = ".oxd-input-field-error-message")
	private List<WebElement> fieldErrors;
	// "(3) Records Found" / "No Records Found" above a results table
	@FindBy(xpath = "//span[contains(normalize-space(),'Record Found') or contains(normalize-space(),'Records Found')]")
	private WebElement recordsFoundLabel;

	// The browser this page works on
	protected final WebDriver driver;
	// Explicit wait with the timeout from config.properties
	protected final WebDriverWait wait;

	/**
	 * @param driver the browser this page works on.
	 */
	protected BasePage(WebDriver driver) {
		this.driver = driver;
		this.wait = new WebDriverWait(driver, Config.get().timeout());
		// OrangeHRM re-renders parts of the page after each load; an element that is
		// replaced while we wait is simply looked up again on the next poll
		this.wait.ignoring(StaleElementReferenceException.class);
		// Fills every @FindBy field of this page and its parent classes with a
		// lazy proxy. It runs before the subclass's own field initializers, so
		// @FindBy fields must never be given a value of their own
		PageFactory.initElements(driver, this);
	}

	/**
	 * Loads an OrangeHRM page by its path, without waiting for its content.
	 *
	 * @param path the page path after {@value #APP_PATH}, e.g. "/auth/login".
	 */
	protected void openPath(String path) {
		driver.get(Config.get().baseUrl() + APP_PATH + path);
	}

	/**
	 * Waits until the element is clickable, then clicks it.
	 *
	 * @param locator the element to click.
	 */
	protected void click(By locator) {
		wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
	}

	/**
	 * Waits until the element is clickable, then clicks it.
	 *
	 * @param element the element to click, usually a {@code @FindBy} field.
	 */
	protected void click(WebElement element) {
		wait.until(ExpectedConditions.elementToBeClickable(element)).click();
	}

	/**
	 * Replaces the text of a field. Select-all + delete is used instead of
	 * {@code clear()}, because the page's form model does not notice a
	 * {@code clear()} and would keep the old value.
	 *
	 * @param locator the input field.
	 * @param text    the text to type; empty leaves the field empty.
	 */
	protected void type(By locator, String text) {
		type(wait.until(ExpectedConditions.elementToBeClickable(locator)), text);
	}

	/**
	 * Replaces the text of a field, like {@link #type(By, String)}.
	 *
	 * @param element the input field, usually a {@code @FindBy} field.
	 * @param text    the text to type; empty leaves the field empty.
	 */
	protected void type(WebElement element, String text) {
		WebElement field = wait.until(ExpectedConditions.elementToBeClickable(element));
		field.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
		if (!text.isEmpty()) {
			field.sendKeys(text);
		}
	}

	/**
	 * @param locator     the element to wait for.
	 * @return WebElement the element, once it is visible.
	 */
	protected WebElement waitVisible(By locator) {
		return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
	}

	/**
	 * @param element     the element to wait for, usually a {@code @FindBy} field.
	 * @return WebElement the element, once it is visible.
	 */
	protected WebElement waitVisible(WebElement element) {
		return wait.until(ExpectedConditions.visibilityOf(element));
	}

	/**
	 * @param locator  the element to read.
	 * @return String  its visible text, trimmed.
	 */
	protected String textOf(By locator) {
		return waitVisible(locator).getText().trim();
	}

	/**
	 * @param element  the element to read, usually a {@code @FindBy} field.
	 * @return String  its visible text, trimmed.
	 */
	protected String textOf(WebElement element) {
		return waitVisible(element).getText().trim();
	}

	/**
	 * Waits until the list has at least one element and all of them are
	 * visible, then reads them.
	 *
	 * @param elements the elements to read, usually a {@code @FindBy} list field.
	 * @return List    the visible text of each element, trimmed, in page order.
	 */
	protected List<String> textsOf(List<WebElement> elements) {
		return wait.until(ExpectedConditions.visibilityOfAllElements(elements))
				.stream().map(WebElement::getText).map(String::trim).toList();
	}

	/**
	 * @param locator  the input field to read.
	 * @return String  the current value of the field.
	 */
	protected String valueOf(By locator) {
		return waitVisible(locator).getDomProperty("value");
	}

	/**
	 * Waits for the element to become visible.
	 *
	 * @param locator   the element to wait for.
	 * @return boolean  true if it appeared within the timeout, false otherwise.
	 */
	protected boolean isVisible(By locator) {
		try {
			waitVisible(locator);
			return true;
		} catch (TimeoutException e) {
			return false;
		}
	}

	/**
	 * Waits for the element to become visible.
	 *
	 * @param element   the element to wait for, usually a {@code @FindBy} field.
	 * @return boolean  true if it appeared within the timeout, false otherwise.
	 */
	protected boolean isVisible(WebElement element) {
		try {
			waitVisible(element);
			return true;
		} catch (TimeoutException e) {
			return false;
		}
	}

	/**
	 * Waits until the browser URL contains the given text.
	 *
	 * @param part      the text to wait for, e.g. "/dashboard".
	 * @return boolean  true if the URL matched within the timeout, false otherwise.
	 */
	protected boolean waitForUrl(String part) {
		try {
			return wait.until(ExpectedConditions.urlContains(part));
		} catch (TimeoutException e) {
			return false;
		}
	}

	/**
	 * Waits for a load started by the previous action to finish. The spinner is
	 * first given a moment to appear, so the wait cannot pass before loading
	 * has even started; then it waits until the spinner is gone.
	 */
	protected void waitForLoader() {
		try {
			// The proxy list is looked up again on every poll, so it fills once the spinner is added
			new WebDriverWait(driver, LOADER_APPEAR_TIMEOUT).until(d -> !loaders.isEmpty());
		} catch (TimeoutException e) {
			// The response came back before the spinner was shown
		}
		wait.until(ExpectedConditions.invisibilityOfAllElements(loaders));
	}

	/**
	 * Locates an element inside the form field with the given label. OrangeHRM
	 * does not link labels to inputs, so the field is found through the
	 * {@code oxd-input-group} that holds both.
	 *
	 * @param label  the visible field label, e.g. "Username".
	 * @param inner  XPath of the element within the field, e.g. "//input".
	 * @return By    locator of that element.
	 */
	protected By inField(String label, String inner) {
		return By.xpath("//label[normalize-space()='" + label + "']/ancestor::div[contains(@class,'oxd-input-group')]" + inner);
	}

	/**
	 * @param label  the visible field label.
	 * @return By    locator of the text input of that field.
	 */
	protected By input(String label) {
		return inField(label, "//input");
	}

	/**
	 * @param label  the visible field label.
	 * @return By    locator of the clickable box of that drop-down.
	 */
	private By selectField(String label) {
		return inField(label, "//div[contains(@class,'oxd-select-text')]");
	}

	/**
	 * @param text  the button's visible text, e.g. "Search".
	 * @return By   locator of that button.
	 */
	protected By button(String text) {
		return By.xpath("//button[normalize-space()='" + text + "']");
	}

	/**
	 * Picks an option in one of OrangeHRM's drop-downs (they are not
	 * {@code <select>} elements, so Selenium's {@code Select} cannot be used).
	 *
	 * @param label   the visible field label, e.g. "User Role".
	 * @param option  the option text, e.g. "Admin".
	 */
	protected void choose(String label, String option) {
		click(selectField(label));
		click(By.xpath("//div[@role='listbox']//div[@role='option'][normalize-space()='" + option + "']"));
	}

	/**
	 * Opens one of OrangeHRM's drop-downs, reads every option and closes it
	 * again, e.g. to check the choices a user is offered.
	 *
	 * @param label  the visible field label, e.g. "User Role".
	 * @return List  the option texts in display order, including "-- Select --".
	 */
	protected List<String> options(String label) {
		By field = selectField(label);
		click(field);
		List<String> options = textsOf(dropdownOptions);
		// A second click on the field closes the list, so the page is left as it was
		click(field);
		return options;
	}

	/**
	 * @param label  the visible field label.
	 * @return String the option shown in that drop-down ("-- Select --" if none).
	 */
	protected String chosen(String label) {
		return textOf(inField(label, "//div[contains(@class,'oxd-select-text-input')]"));
	}

	/**
	 * @param label   the visible field label.
	 * @return String the validation message under that field, e.g. "Required".
	 */
	public String fieldError(String label) {
		return textOf(inField(label, "//span[contains(@class,'oxd-input-field-error-message')]"));
	}

	/**
	 * Waits for validation messages and returns all of them, top to bottom.
	 *
	 * @return List the messages under the form fields, e.g. [Required, Required].
	 */
	public List<String> validationErrors() {
		return textsOf(fieldErrors);
	}

	/**
	 * Waits for the confirmation toast and returns its text.
	 *
	 * @return String the toast message, e.g. "Successfully Saved".
	 */
	protected String toast() {
		return textOf(toastMessage);
	}

	/**
	 * @return String the record count above the results table, e.g. "(3) Records Found".
	 */
	protected String recordsFound() {
		return textOf(recordsFoundLabel);
	}

	/**
	 * Reads the results table, keyed by column title. The whole table is read
	 * inside the wait, so a re-render in the middle of reading just retries.
	 *
	 * @return List one map per row: column title → cell text (empty list if no rows).
	 */
	protected List<Map<String, String>> tableRows() {
		return wait.until(d -> {
			List<String> headers = new ArrayList<>();
			// Every loop over a proxy list finds the elements again, so a retry reads the new table
			for (WebElement header : tableHeaders) {
				headers.add(header.getText().trim());
			}
			List<Map<String, String>> rows = new ArrayList<>();
			for (WebElement row : tableRowElements) {
				List<WebElement> cells = row.findElements(TABLE_CELLS);
				Map<String, String> values = new LinkedHashMap<>();
				// Cells are matched to the column titles by position
				for (int i = 0; i < cells.size() && i < headers.size(); i++) {
					values.put(headers.get(i), cells.get(i).getText().trim());
				}
				rows.add(values);
			}
			return rows;
		});
	}

	/**
	 * Runs JavaScript in the page, for what the WebDriver API cannot do. Use it
	 * sparingly: a JavaScript click, for example, skips the checks a real
	 * click makes (visible, not covered), so it can hide real bugs.
	 *
	 * @param script  the script; {@code return} hands a value back.
	 * @param args    values the script reads as {@code arguments[0]}, {@code arguments[1]} …
	 * @return Object the returned value as String, Long, Double, Boolean, WebElement, List or null.
	 */
	protected Object runScript(String script, Object... args) {
		return ((JavascriptExecutor) driver).executeScript(script, args);
	}

	/**
	 * @param column  a column title, e.g. "Username".
	 * @return List   the text of that column in every row.
	 */
	protected List<String> column(String column) {
		return tableRows().stream().map(row -> row.getOrDefault(column, "")).toList();
	}
}
