// Package of the interview examples, one sub-package per topic
package com.automation.selenium.interview;

// Time spans for the waits
import java.time.Duration;

// A locator (how to find an element)
import org.openqa.selenium.By;
// Explicit wait: polls a condition until it is met or the timeout passes
import org.openqa.selenium.support.ui.WebDriverWait;

// Base class: starts a browser before each test, screenshot on failure, closes it afterwards
import com.automation.selenium.base.BaseTest;
// Settings (practice site URL, timeout)
import com.automation.selenium.config.Config;
// Writes steps into the HTML report
import com.automation.selenium.utils.Steps;

/**
 * Parent of the interview examples that run on the practice site
 * <a href="https://the-internet.herokuapp.com">the-internet.herokuapp.com</a>,
 * which has one small page per Selenium topic: alerts, frames, a native
 * {@code <select>}, checkboxes, dynamic loading, tables, uploads and more.
 * OrangeHRM has none of these, so these examples need a second site.
 * <p>
 * <b>Why the examples call the Selenium API directly:</b> in the interview
 * lab the API call <i>is</i> the topic (how do you switch to a frame? how do
 * you accept an alert?), so each test shows it in place, with its locators
 * as named constants at the top of the class. The application tests in
 * {@code tests/} use page objects instead, which is how a real suite should
 * be built (see the Page Object Model questions in the index).
 */
// abstract: never run on its own, only through its subclasses
public abstract class PracticeSiteTest extends BaseTest {

	// Heroku shows its error page inside an iframe while the app wakes up or is overloaded
	private static final By HEROKU_ERROR_PAGE = By.cssSelector("iframe[src*='herokucdn.com/error-pages']");
	// Any element of a normal page, or a frameset (the frames pages have no <body>)
	private static final By ANY_PAGE_CONTENT = By.cssSelector("body *, frameset");

	/**
	 * Opens a page of the practice site. The free Heroku app sometimes answers
	 * the first request with an error page or an empty page; then it is loaded
	 * once more. A second failure is left to the test, which then fails with
	 * a screenshot of what the site showed.
	 *
	 * @param path the page path, e.g. "/javascript_alerts".
	 */
	protected void open(String path) {
		// Record the step in the report
		Steps.log("Open the practice page \"" + path + "\"");
		// Load the page (get waits for the page's load event)
		driver().get(Config.get().practiceUrl() + path);
		// The site answered with its error page, or with nothing at all
		if (!driver().findElements(HEROKU_ERROR_PAGE).isEmpty() || driver().findElements(ANY_PAGE_CONTENT).isEmpty()) {
			// Say so in the report, so a slow site is not mistaken for a test problem
			Steps.log("The practice site answered with an error page; loading it again");
			// One more try
			driver().navigate().refresh();
		}
	}

	/**
	 * Creates an explicit wait with the configured timeout.
	 *
	 * @return WebDriverWait a wait for this test's browser.
	 */
	protected WebDriverWait explicitWait() {
		// A new wait object is cheap; the timeout comes from config.properties (timeoutSeconds)
		return new WebDriverWait(driver(), Config.get().timeout());
	}

	/**
	 * Creates an explicit wait with its own timeout, e.g. a short one for a
	 * test that expects the wait to fail.
	 *
	 * @param timeout       how long to wait at most.
	 * @return WebDriverWait a wait for this test's browser.
	 */
	protected WebDriverWait explicitWait(Duration timeout) {
		return new WebDriverWait(driver(), timeout);
	}
}
