// Package of the page objects (one class per screen of OrangeHRM)
package com.automation.selenium.pages;

// Time spans for the waits
import java.time.Duration;
// Collections used while reading tables
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// A locator (how to find an element: by CSS, XPath, name, ...)
import org.openqa.selenium.By;
// Interface for running JavaScript in the page
import org.openqa.selenium.JavascriptExecutor;
// Special keys (Ctrl, Delete, ...)
import org.openqa.selenium.Keys;
// Thrown when a found element no longer belongs to the page
import org.openqa.selenium.StaleElementReferenceException;
// Thrown by a wait when its condition is not met in time
import org.openqa.selenium.TimeoutException;
// The browser
import org.openqa.selenium.WebDriver;
// One element on the page
import org.openqa.selenium.WebElement;
// PageFactory annotation that says how to find a field's element(s)
import org.openqa.selenium.support.FindBy;
// Selenium's PageFactory: fills @FindBy fields with proxies
import org.openqa.selenium.support.PageFactory;
// Ready-made wait conditions (visible, clickable, URL contains, ...)
import org.openqa.selenium.support.ui.ExpectedConditions;
// Explicit wait: polls a condition until it is met or the timeout passes
import org.openqa.selenium.support.ui.WebDriverWait;

// Settings (base URL, timeout)
import com.automation.selenium.config.Config;

/**
 * Parent of all page objects. Wraps the WebDriver actions in explicit waits,
 * so no page needs {@code Thread.sleep} or an implicit wait, and knows the
 * OrangeHRM widgets used on every page (labelled fields, drop-downs, tables,
 * toasts and the loading spinner).
 * <p>
 * Pages declare their fixed elements as {@link FindBy @FindBy} fields, which
 * the constructor fills with Selenium's {@link PageFactory}. Each field holds
 * a proxy that looks the element up again on every call, so after a re-render
 * the next call finds the new element. This does not rule out
 * {@code StaleElementReferenceException}: a call that lands while the page is
 * replacing the element can still throw it, which is why the waits below
 * ignore it and simply try again. Locators built from a value (a field label,
 * a button text, a table cell) cannot be annotations and stay methods that
 * return a {@link By}.
 * <p>
 * Why explicit waits only: an explicit wait polls one condition (every
 * 500 ms by default) and continues as soon as it is true, so a test waits
 * exactly as long as needed. {@code Thread.sleep} always waits the full time,
 * and an implicit wait applies to every lookup, which makes "is it gone?"
 * checks slow and mixes badly with explicit waits.
 * <p>
 * Most helpers come twice: one taking a {@link By} (for locators built from
 * a value) and one taking a {@link WebElement} (for {@code @FindBy} fields).
 */
// abstract: only its subclasses describe a real page
public abstract class BasePage {

	// Every OrangeHRM page lives under this path; page PATHs are relative to it
	protected static final String APP_PATH = "/web/index.php";

	// How long to wait for the spinner to show up; fast responses may never show it
	private static final Duration LOADER_APPEAR_TIMEOUT = Duration.ofSeconds(2);
	// Cells of one table row, searched inside that row (a relative locator cannot be a field)
	private static final By TABLE_CELLS = By.cssSelector(".oxd-table-cell");

	// Spinner OrangeHRM shows while a form or table is loading (an empty list when there is none)
	// (CSS "a, b" matches either selector: the table spinner or the form overlay)
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
	// (XPath, because CSS cannot match on text; both singular and plural forms are covered)
	@FindBy(xpath = "//span[contains(normalize-space(),'Record Found') or contains(normalize-space(),'Records Found')]")
	private WebElement recordsFoundLabel;

	// The browser this page works on (protected: subclasses use it directly)
	protected final WebDriver driver;
	// Explicit wait with the timeout from config.properties
	protected final WebDriverWait wait;

	/**
	 * Sets up the wait and fills the {@code @FindBy} fields.
	 *
	 * @param driver the browser this page works on.
	 */
	protected BasePage(WebDriver driver) {
		// Keep the browser
		this.driver = driver;
		// One wait object per page, with the configured timeout (e.g. 15 s)
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
		// e.g. https://opensource-demo.orangehrmlive.com + /web/index.php + /auth/login
		// (driver.get waits for the page's load event, not for its JavaScript content)
		driver.get(Config.get().baseUrl() + APP_PATH + path);
	}

	/**
	 * Waits until the element is clickable, then clicks it.
	 *
	 * @param locator the element to click.
	 */
	protected void click(By locator) {
		// Clickable = visible and enabled; until(...) returns the element, which is then clicked
		wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
	}

	/**
	 * Waits until the element is clickable, then clicks it.
	 *
	 * @param element the element to click, usually a {@code @FindBy} field.
	 */
	protected void click(WebElement element) {
		// Same as above, for a @FindBy field (the proxy finds the element on each poll)
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
		// Wait for the field, then use the WebElement version below
		type(wait.until(ExpectedConditions.elementToBeClickable(locator)), text);
	}

	/**
	 * Replaces the text of a field, like {@link #type(By, String)}.
	 *
	 * @param element the input field, usually a {@code @FindBy} field.
	 * @param text    the text to type; empty leaves the field empty.
	 */
	protected void type(WebElement element, String text) {
		// Wait until the field can take input
		WebElement field = wait.until(ExpectedConditions.elementToBeClickable(element));
		// Ctrl+A selects everything in the box, Delete removes it; these are real key events, so Vue sees them
		field.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
		// Type the new text (nothing to type for an empty string)
		if (!text.isEmpty()) {
			field.sendKeys(text);
		}
	}

	/**
	 * Waits until an element is visible.
	 *
	 * @param locator     the element to wait for.
	 * @return WebElement the element, once it is visible.
	 */
	protected WebElement waitVisible(By locator) {
		// Visible = in the page and displayed (not hidden, has a size); throws TimeoutException after the timeout
		return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
	}

	/**
	 * Waits until an element is visible.
	 *
	 * @param element     the element to wait for, usually a {@code @FindBy} field.
	 * @return WebElement the element, once it is visible.
	 */
	protected WebElement waitVisible(WebElement element) {
		// For a proxy: "not found yet" counts as "not visible yet", because WebDriverWait ignores NotFoundException
		return wait.until(ExpectedConditions.visibilityOf(element));
	}

	/**
	 * Reads an element's text.
	 *
	 * @param locator  the element to read.
	 * @return String  its visible text, trimmed.
	 */
	protected String textOf(By locator) {
		// Wait, read the visible text, remove spaces at both ends
		return waitVisible(locator).getText().trim();
	}

	/**
	 * Reads an element's text.
	 *
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
		// visibilityOfAllElements also waits while the list is empty, then returns the list
		return wait.until(ExpectedConditions.visibilityOfAllElements(elements))
				// Element → its text → trimmed; toList() collects them in order
				.stream().map(WebElement::getText).map(String::trim).toList();
	}

	/**
	 * Reads what is typed in an input field.
	 *
	 * @param locator  the input field to read.
	 * @return String  the current value of the field.
	 */
	protected String valueOf(By locator) {
		// getText() is empty for an <input>; the typed text is its "value" property
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
			// Wait up to the timeout
			waitVisible(locator);
			// It appeared
			return true;
		} catch (TimeoutException e) {
			// It did not: answer false instead of throwing, so tests can assert on it
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
			// Wait up to the timeout
			waitVisible(element);
			// It appeared
			return true;
		} catch (TimeoutException e) {
			// It did not
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
			// urlContains returns true as soon as the current URL contains the text
			return wait.until(ExpectedConditions.urlContains(part));
		} catch (TimeoutException e) {
			// The URL never matched: false, so the caller can decide what to do
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
			// (a separate, short wait: up to 2 s for the spinner to show up)
			new WebDriverWait(driver, LOADER_APPEAR_TIMEOUT).until(d -> !loaders.isEmpty());
		} catch (TimeoutException e) {
			// The response came back before the spinner was shown
		}
		// Now wait (full timeout) until every spinner is gone; passes at once if there is none.
		// Not ExpectedConditions.invisibilityOfAllElements(loaders): it calls size() and then get(i) on the
		// list, and a PageFactory list proxy searches the page again on every call, so a spinner that
		// vanishes between the two calls gives an IndexOutOfBoundsException. stream() searches once and
		// checks that one snapshot; a spinner removed meanwhile throws Stale…, which this wait ignores.
		wait.until(d -> loaders.stream().noneMatch(WebElement::isDisplayed));
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
		// Read as: the <label> with this text → up to the group div around label and input → down to "inner"
		return By.xpath("//label[normalize-space()='" + label + "']/ancestor::div[contains(@class,'oxd-input-group')]" + inner);
	}

	/**
	 * Locates the text box of a labelled field.
	 *
	 * @param label  the visible field label.
	 * @return By    locator of the text input of that field.
	 */
	protected By input(String label) {
		// The <input> inside that field's group
		return inField(label, "//input");
	}

	/**
	 * Locates the clickable box of a labelled drop-down.
	 *
	 * @param label  the visible field label.
	 * @return By    locator of the clickable box of that drop-down.
	 */
	private By selectField(String label) {
		// The div that shows the chosen option and opens the list when clicked
		return inField(label, "//div[contains(@class,'oxd-select-text')]");
	}

	/**
	 * Locates a button by its text.
	 *
	 * @param text  the button's visible text, e.g. "Search".
	 * @return By   locator of that button.
	 */
	protected By button(String text) {
		// normalize-space() trims and collapses the spaces around the button text
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
		// Click the box to open the list
		click(selectField(label));
		// Click the option with that exact text in the opened list
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
		// The drop-down's box, used twice below
		By field = selectField(label);
		// Open the list
		click(field);
		// Wait for the options and read their texts
		List<String> options = textsOf(dropdownOptions);
		// A second click on the field closes the list, so the page is left as it was
		click(field);
		// Hand back the option texts
		return options;
	}

	/**
	 * Reads the option a drop-down shows.
	 *
	 * @param label  the visible field label.
	 * @return String the option shown in that drop-down ("-- Select --" if none).
	 */
	protected String chosen(String label) {
		// The text part inside the drop-down's box
		return textOf(inField(label, "//div[contains(@class,'oxd-select-text-input')]"));
	}

	/**
	 * Reads the validation message of one field.
	 *
	 * @param label   the visible field label.
	 * @return String the validation message under that field, e.g. "Required".
	 */
	public String fieldError(String label) {
		// The red message inside that field's group
		return textOf(inField(label, "//span[contains(@class,'oxd-input-field-error-message')]"));
	}

	/**
	 * Waits for validation messages and returns all of them, top to bottom.
	 *
	 * @return List the messages under the form fields, e.g. [Required, Required].
	 */
	public List<String> validationErrors() {
		// Waits until at least one message is visible
		return textsOf(fieldErrors);
	}

	/**
	 * Waits for the confirmation toast and returns its text.
	 *
	 * @return String the toast message, e.g. "Successfully Saved".
	 */
	protected String toast() {
		// The toast disappears after a few seconds, so callers read it right after the action
		return textOf(toastMessage);
	}

	/**
	 * Reads the record count above a results table.
	 *
	 * @return String the record count above the results table, e.g. "(3) Records Found".
	 */
	protected String recordsFound() {
		// Waits until the label is visible
		return textOf(recordsFoundLabel);
	}

	/**
	 * Reads the results table, keyed by column title. The whole table is read
	 * inside the wait, so a re-render in the middle of reading just retries.
	 *
	 * @return List one map per row: column title → cell text (empty list if no rows).
	 */
	protected List<Map<String, String>> tableRows() {
		// A custom wait condition: if it throws StaleElementReferenceException (ignored above), it is polled again
		return wait.until(d -> {
			// The column titles, left to right
			List<String> headers = new ArrayList<>();
			// Every loop over a proxy list finds the elements again, so a retry reads the new table
			for (WebElement header : tableHeaders) {
				headers.add(header.getText().trim());
			}
			// One map per row
			List<Map<String, String>> rows = new ArrayList<>();
			// Each row of the table body
			for (WebElement row : tableRowElements) {
				// The cells of this row only (findElements on the row searches inside it)
				List<WebElement> cells = row.findElements(TABLE_CELLS);
				// LinkedHashMap keeps the column order
				Map<String, String> values = new LinkedHashMap<>();
				// Cells are matched to the column titles by position
				for (int i = 0; i < cells.size() && i < headers.size(); i++) {
					values.put(headers.get(i), cells.get(i).getText().trim());
				}
				// Add this row
				rows.add(values);
			}
			// A non-null value (even an empty list) ends the wait
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
		// Every real browser driver implements JavascriptExecutor; the cast gives access to executeScript
		return ((JavascriptExecutor) driver).executeScript(script, args);
	}

	/**
	 * Reads one column of the results table.
	 *
	 * @param column  a column title, e.g. "Username".
	 * @return List   the text of that column in every row.
	 */
	protected List<String> column(String column) {
		// Take that column from every row ("" if a row has no such cell)
		return tableRows().stream().map(row -> row.getOrDefault(column, "")).toList();
	}
}
