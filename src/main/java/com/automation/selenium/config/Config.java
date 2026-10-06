// Package of the configuration reader
package com.automation.selenium.config;

// Thrown when the settings file cannot be read
import java.io.IOException;
// The settings file is read as a stream from the classpath
import java.io.InputStream;
// Unchecked wrapper for IOException
import java.io.UncheckedIOException;
// The wait timeout is returned as a Duration
import java.time.Duration;
// Key = value file format (java.util.Properties)
import java.util.Properties;

/**
 * Test settings read from {@code config.properties} on the classpath.
 * A system property with the same key wins, so any setting can be changed
 * from the command line, e.g. {@code -Dbrowser=edge -Dheadless=true}.
 * <p>
 * This is a singleton: one shared instance, created when the class is first
 * used. Java guarantees that static initialisation runs once and is
 * thread-safe, so parallel tests can all call {@link #get()}.
 */
// final: not meant to be extended
public final class Config {

	// Name of the settings file on the test classpath (src/test/resources)
	private static final String FILE = "config.properties";
	// Loaded once; the settings do not change during a run
	private static final Config INSTANCE = new Config();

	// Values from the settings file
	private final Properties properties = new Properties();

	// Use get(): the constructor is private so nobody creates a second instance
	private Config() {
		// Open the file from the classpath; try-with-resources closes the stream automatically
		try (InputStream in = Config.class.getClassLoader().getResourceAsStream(FILE)) {
			// getResourceAsStream returns null (no exception) when the file is missing
			if (in == null) {
				// Fail with a clear message instead of a NullPointerException later
				throw new IllegalStateException(FILE + " not found on the classpath");
			}
			// Read all key=value lines into the Properties object
			properties.load(in);
		} catch (IOException e) {
			// Reading failed: rethrow unchecked, so the constructor needs no throws clause
			throw new UncheckedIOException("Could not read " + FILE, e);
		}
	}

	/**
	 * Returns the shared settings.
	 *
	 * @return Config the shared settings.
	 */
	public static Config get() {
		// Always the same instance
		return INSTANCE;
	}

	/**
	 * Reads one setting; a {@code -Dkey=value} system property overrides the file.
	 *
	 * @param key       the setting name.
	 * @return String   the system property with that name, else the file value.
	 * @throws IllegalStateException if the setting is in neither place.
	 */
	public String value(String key) {
		// System property first; if it is not set, the file value is used as the default
		String value = System.getProperty(key, properties.getProperty(key));
		// Missing in both places, or set to an empty value
		if (value == null || value.isBlank()) {
			// Stop with the name of the missing setting
			throw new IllegalStateException("Missing setting '" + key + "' in " + FILE);
		}
		// Remove spaces around the value (e.g. a trailing space in the file)
		return value.trim();
	}

	/**
	 * The address of the application under test.
	 *
	 * @return String the application URL without a trailing slash.
	 */
	public String baseUrl() {
		// Regex "/+$" = one or more slashes at the end; removed so paths can be appended as "/web/..."
		return value("baseUrl").replaceAll("/+$", "");
	}

	/**
	 * The address of the practice site used by the interview examples that
	 * OrangeHRM cannot show (alerts, frames, native drop-downs, cookies, ...).
	 *
	 * @return String the practice site URL without a trailing slash.
	 */
	public String practiceUrl() {
		// Same trimming as baseUrl(), so paths can be appended as "/login"
		return value("practiceUrl").replaceAll("/+$", "");
	}

	/**
	 * The browser to start.
	 *
	 * @return String chrome, firefox or edge.
	 */
	public String browser() {
		return value("browser");
	}

	/**
	 * Whether the browser runs without a window.
	 *
	 * @return boolean true to run the browser without a window.
	 */
	public boolean headless() {
		// "true" (any case) → true; anything else → false
		return Boolean.parseBoolean(value("headless"));
	}

	/**
	 * The explicit wait timeout.
	 *
	 * @return Duration the explicit wait timeout for page actions.
	 */
	public Duration timeout() {
		// The file stores whole seconds, e.g. timeoutSeconds=15
		return Duration.ofSeconds(Long.parseLong(value("timeoutSeconds")));
	}

	/**
	 * The login user.
	 *
	 * @return String the administrator user name.
	 */
	public String username() {
		return value("username");
	}

	/**
	 * The login password.
	 *
	 * @return String the administrator password.
	 */
	public String password() {
		return value("password");
	}
}
