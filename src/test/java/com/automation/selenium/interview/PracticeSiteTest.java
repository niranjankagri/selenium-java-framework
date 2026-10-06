// Package of the interview examples, one sub-package per topic
package com.automation.selenium.interview;

// Time spans for the waits
import java.time.Duration;

// A locator (how to find an element)
import org.openqa.selenium.By;
// Runs JavaScript in the page (to check that the page's scripts loaded)
import org.openqa.selenium.JavascriptExecutor;
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
	// How many times a page is loaded at most before it is left to the test
	private static final int MAX_LOADS = 3;
	// True when the page includes jQuery but it did not load (the page's buttons then do nothing)
	private static final String JQUERY_MISSING = "return !!document.querySelector(\"script[src*='jquery']\") && !window.jQuery;";

	/**
	 * Opens a page of the practice site. The free Heroku app sometimes answers
	 * with an error page or an empty page, or fails to send one of the page's
	 * scripts (then the page looks normal, but its buttons do nothing); in
	 * those cases the page is loaded again, up to {@value #MAX_LOADS} times in
	 * all. After that the page is left to the test, which then fails with a
	 * screenshot of what the site showed.
	 * <p>
	 * This is about the site, not about Selenium: a retry like this belongs
	 * only where the environment is known to be unreliable, and every reload
	 * is written to the report so it stays visible.
	 *
	 * @param path the page path, e.g. "/javascript_alerts".
	 */
	protected void open(String path) {
		// Record the step in the report
		Steps.log("Open the practice page \"" + path + "\"");
		// Load the page (get waits for the page's load event, so its scripts have run)
		driver().get(Config.get().practiceUrl() + path);
		// Reload while the page is broken, but at most MAX_LOADS loads in all
		for (int load = 2; load <= MAX_LOADS && pageIsBroken(); load++) {
			// Say so in the report, so a slow site is not mistaken for a test problem
			Steps.log("The practice site sent a broken page; loading it again (load " + load + " of " + MAX_LOADS + ")");
			// Ask the site again
			driver().navigate().refresh();
		}
	}

	/**
	 * Checks whether the page the site sent is usable.
	 *
	 * @return boolean true if it is Heroku's error page, empty, or missing jQuery.
	 */
	private boolean pageIsBroken() {
		// Heroku's error page, or a page with nothing in it
		if (!driver().findElements(HEROKU_ERROR_PAGE).isEmpty() || driver().findElements(ANY_PAGE_CONTENT).isEmpty()) {
			return true;
		}
		// The page's jQuery did not arrive, so none of its click handlers were attached
		return Boolean.TRUE.equals(((JavascriptExecutor) driver()).executeScript(JQUERY_MISSING));
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
