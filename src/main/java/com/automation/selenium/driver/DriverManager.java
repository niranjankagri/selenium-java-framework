package com.automation.selenium.driver;

import java.util.Locale;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

/**
 * Starts and holds one {@link WebDriver} per thread, so test classes can run
 * in parallel, each with its own browser.
 * <p>
 * The driver binaries are resolved by Selenium Manager, so none are kept in
 * the project.
 */
public final class DriverManager {

	// Window size; set explicitly so headless runs get the same layout as visible ones
	private static final int WIDTH = 1920;
	private static final int HEIGHT = 1080;

	// The browser of each thread
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
		quit();
		// Each browser's options add the headless flag and the window size
		WebDriver driver = switch (browser.toLowerCase(Locale.ROOT)) {
		case "chrome" -> new ChromeDriver(chromeOptions(headless));
		case "firefox" -> new FirefoxDriver(firefoxOptions(headless));
		case "edge" -> new EdgeDriver(edgeOptions(headless));
		default -> throw new IllegalArgumentException("Unsupported browser '" + browser + "' (use chrome, firefox or edge)");
		};
		DRIVER.set(driver);
		return driver;
	}

	/**
	 * @return WebDriver the browser of the current thread.
	 * @throws IllegalStateException if no browser was started on this thread.
	 */
	public static WebDriver driver() {
		WebDriver driver = DRIVER.get();
		if (driver == null) {
			throw new IllegalStateException("No browser started on this thread");
		}
		return driver;
	}

	/**
	 * @return boolean true if the current thread has a browser open.
	 */
	public static boolean isStarted() {
		return DRIVER.get() != null;
	}

	/**
	 * Closes the browser of the current thread, if any.
	 */
	public static void quit() {
		WebDriver driver = DRIVER.get();
		if (driver != null) {
			try {
				driver.quit();
			} finally {
				DRIVER.remove();
			}
		}
	}

	/**
	 * @param headless       true to run without a window.
	 * @return ChromeOptions the Chrome settings.
	 */
	private static ChromeOptions chromeOptions(boolean headless) {
		ChromeOptions options = new ChromeOptions();
		if (headless) {
			options.addArguments("--headless=new");
		}
		// Chrome's "change your password" warning for the well-known demo password hides the page
		options.addArguments("--window-size=" + WIDTH + "," + HEIGHT, "--disable-search-engine-choice-screen",
				"--disable-features=PasswordLeakDetection");
		return options;
	}

	/**
	 * @param headless        true to run without a window.
	 * @return FirefoxOptions the Firefox settings.
	 */
	private static FirefoxOptions firefoxOptions(boolean headless) {
		FirefoxOptions options = new FirefoxOptions();
		if (headless) {
			options.addArguments("-headless");
		}
		// Firefox takes the window size as two separate flags
		options.addArguments("-width=" + WIDTH, "-height=" + HEIGHT);
		return options;
	}

	/**
	 * @param headless     true to run without a window.
	 * @return EdgeOptions the Edge settings.
	 */
	private static EdgeOptions edgeOptions(boolean headless) {
		EdgeOptions options = new EdgeOptions();
		if (headless) {
			options.addArguments("--headless=new");
		}
		options.addArguments("--window-size=" + WIDTH + "," + HEIGHT);
		return options;
	}
}
