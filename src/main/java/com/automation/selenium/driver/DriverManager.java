// Package of the browser start-up code
package com.automation.selenium.driver;

// Locale.ROOT: lower-case the browser name the same way on every machine
import java.util.Locale;

// The common browser interface all drivers implement
import org.openqa.selenium.WebDriver;
// Chrome driver and its start-up options
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
// Edge driver and its start-up options
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
// Firefox driver and its start-up options
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

/**
 * Starts and holds one {@link WebDriver} per thread, so test classes can run
 * in parallel, each with its own browser.
 * <p>
 * The driver binaries are resolved by Selenium Manager, so none are kept in
 * the project.
 * <p>
 * Why a {@link ThreadLocal}: a static {@code WebDriver} field would be shared
 * by all threads, so parallel tests would drive each other's browser. A
 * ThreadLocal gives every thread its own slot: {@code DRIVER.get()} on thread
 * A never sees the browser of thread B.
 */
// final + private constructor: only static members, never instantiated
public final class DriverManager {

	// Window size; set explicitly so headless runs get the same layout as visible ones
	private static final int WIDTH = 1920;
	private static final int HEIGHT = 1080;

	// The browser of each thread (one slot per thread, see the class comment)
	private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();

	// Static helper only
	private DriverManager() {
	}

	/**
	 * Starts a browser for the current thread. A browser this thread still
	 * had open is closed first, so it is not leaked.
	 *
	 * @param browser     chrome, firefox or edge (case-insensitive).
	 * @param headless    true to run without a window.
	 * @return WebDriver  the started browser.
	 */
	public static WebDriver start(String browser, boolean headless) {
		// Close a leftover browser of this thread (does nothing if there is none)
		quit();
		// Each browser's options add the headless flag and the window size.
		// A switch expression (Java 14+): each "case ->" returns the value for that browser.
		// Creating the driver starts the browser; Selenium Manager downloads a matching driver if needed.
		WebDriver driver = switch (browser.toLowerCase(Locale.ROOT)) {
		case "chrome" -> new ChromeDriver(chromeOptions(headless));
		case "firefox" -> new FirefoxDriver(firefoxOptions(headless));
		case "edge" -> new EdgeDriver(edgeOptions(headless));
		// Any other name is a configuration mistake: stop with the allowed values
		default -> throw new IllegalArgumentException("Unsupported browser '" + browser + "' (use chrome, firefox or edge)");
		};
		// Store it in this thread's slot
		DRIVER.set(driver);
		// Also return it, for callers that want it right away
		return driver;
	}

	/**
	 * Returns this thread's browser.
	 *
	 * @return WebDriver the browser of the current thread.
	 * @throws IllegalStateException if no browser was started on this thread.
	 */
	public static WebDriver driver() {
		// Read this thread's slot
		WebDriver driver = DRIVER.get();
		// Empty slot: start() was not called on this thread (or quit() already ran)
		if (driver == null) {
			// A clear message instead of a NullPointerException somewhere in a page object
			throw new IllegalStateException("No browser started on this thread");
		}
		return driver;
	}

	/**
	 * Checks whether this thread has a browser.
	 *
	 * @return boolean true if the current thread has a browser open.
	 */
	public static boolean isStarted() {
		// Unlike driver(), this never throws
		return DRIVER.get() != null;
	}

	/**
	 * Closes the browser of the current thread, if any.
	 */
	public static void quit() {
		// Read this thread's slot
		WebDriver driver = DRIVER.get();
		// Only if a browser is open
		if (driver != null) {
			try {
				// quit() closes every window and ends the driver process (close() would only close one window)
				driver.quit();
			} finally {
				// Always empty the slot, even if quit() threw: the thread pool reuses threads,
				// and a stale driver left in the slot would be handed to the next test
				DRIVER.remove();
			}
		}
	}

	/**
	 * Builds the Chrome start-up options.
	 *
	 * @param headless       true to run without a window.
	 * @return ChromeOptions the Chrome settings.
	 */
	private static ChromeOptions chromeOptions(boolean headless) {
		// Start with Chrome's defaults
		ChromeOptions options = new ChromeOptions();
		// No visible window
		if (headless) {
			// "new" headless mode = the real Chrome without a window (the old mode was a separate, different browser)
			options.addArguments("--headless=new");
		}
		// Chrome's "change your password" warning for the well-known demo password hides the page.
		// The search-engine choice screen (shown on new profiles in some regions) would also block the page.
		options.addArguments("--window-size=" + WIDTH + "," + HEIGHT, "--disable-search-engine-choice-screen",
				"--disable-features=PasswordLeakDetection");
		return options;
	}

	/**
	 * Builds the Firefox start-up options.
	 *
	 * @param headless        true to run without a window.
	 * @return FirefoxOptions the Firefox settings.
	 */
	private static FirefoxOptions firefoxOptions(boolean headless) {
		// Start with Firefox's defaults
		FirefoxOptions options = new FirefoxOptions();
		// No visible window (Firefox uses a single dash)
		if (headless) {
			options.addArguments("-headless");
		}
		// Firefox takes the window size as two separate flags
		options.addArguments("-width=" + WIDTH, "-height=" + HEIGHT);
		return options;
	}

	/**
	 * Builds the Edge start-up options. Edge is based on Chromium, so the
	 * flags are the same as Chrome's.
	 *
	 * @param headless     true to run without a window.
	 * @return EdgeOptions the Edge settings.
	 */
	private static EdgeOptions edgeOptions(boolean headless) {
		// Start with Edge's defaults
		EdgeOptions options = new EdgeOptions();
		// No visible window
		if (headless) {
			options.addArguments("--headless=new");
		}
		// Same window size as the other browsers
		options.addArguments("--window-size=" + WIDTH + "," + HEIGHT);
		return options;
	}
}
