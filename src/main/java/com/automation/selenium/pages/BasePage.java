package com.automation.selenium.pages;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.automation.selenium.config.Config;

/**
 * Parent of all page objects. Wraps the WebDriver actions in explicit waits,
 * so no page needs {@code Thread.sleep} or an implicit wait, and knows the
 * OrangeHRM widgets used on every page (labelled fields, drop-downs, tables,
 * toasts and the loading spinner).
 */
public abstract class BasePage {

	// Spinner OrangeHRM shows while a form or table is loading
	private static final By LOADER = By.cssSelector(".oxd-loading-spinner, .oxd-form-loader");
	// How long to wait for the spinner to show up; fast responses may never show it
	private static final Duration LOADER_APPEAR_TIMEOUT = Duration.ofSeconds(2);
	// Message shown at the bottom right after a save or delete
	private static final By TOAST_MESSAGE = By.cssSelector(".oxd-toast .oxd-text--toast-message");
	// Column titles and rows of a results table
	private static final By TABLE_HEADERS = By.cssSelector(".oxd-table-header .oxd-table-th");
	private static final By TABLE_ROWS = By.cssSelector(".oxd-table-body .oxd-table-card");
	private static final By TABLE_CELLS = By.cssSelector(".oxd-table-cell");
	// "(3) Records Found" / "No Records Found" above a results table
	private static final By RECORDS_FOUND = By.xpath("//span[contains(normalize-space(),'Record Found') or contains(normalize-space(),'Records Found')]");

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
	 * Replaces the text of a field. Select-all + delete is used instead of
	 * {@code clear()}, because the page's form model does not notice a
	 * {@code clear()} and would keep the old value.
	 *
	 * @param locator the input field.
	 * @param text    the text to type; empty leaves the field empty.
	 */
	protected void type(By locator, String text) {
		WebElement field = wait.until(ExpectedConditions.elementToBeClickable(locator));
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
	 * @param locator  the element to read.
	 * @return String  its visible text, trimmed.
	 */
	protected String textOf(By locator) {
		return waitVisible(locator).getText().trim();
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
			new WebDriverWait(driver, LOADER_APPEAR_TIMEOUT).until(ExpectedConditions.presenceOfElementLocated(LOADER));
		} catch (TimeoutException e) {
			// The response came back before the spinner was shown
		}
		wait.until(ExpectedConditions.invisibilityOfElementLocated(LOADER));
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
	 * Picks an option in one of OrangeHRM's drop-downs (they are not
	 * {@code <select>} elements, so Selenium's {@code Select} cannot be used).
	 *
	 * @param label   the visible field label, e.g. "User Role".
	 * @param option  the option text, e.g. "Admin".
	 */
	protected void choose(String label, String option) {
		click(inField(label, "//div[contains(@class,'oxd-select-text')]"));
		click(By.xpath("//div[@role='listbox']//div[@role='option'][normalize-space()='" + option + "']"));
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
	 * Waits for the confirmation toast and returns its text.
	 *
	 * @return String the toast message, e.g. "Successfully Saved".
	 */
	protected String toast() {
		return textOf(TOAST_MESSAGE);
	}

	/**
	 * @return String the record count above the results table, e.g. "(3) Records Found".
	 */
	protected String recordsFound() {
		return textOf(RECORDS_FOUND);
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
			for (WebElement header : d.findElements(TABLE_HEADERS)) {
				headers.add(header.getText().trim());
			}
			List<Map<String, String>> rows = new ArrayList<>();
			for (WebElement row : d.findElements(TABLE_ROWS)) {
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
	 * @param column  a column title, e.g. "Username".
	 * @return List   the text of that column in every row.
	 */
	protected List<String> column(String column) {
		return tableRows().stream().map(row -> row.getOrDefault(column, "")).toList();
	}
}
