package com.automation.selenium.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.Properties;

/**
 * Test settings read from {@code config.properties} on the classpath.
 * A system property with the same key wins, so any setting can be changed
 * from the command line, e.g. {@code -Dbrowser=edge -Dheadless=true}.
 */
public final class Config {

	// Name of the settings file on the test classpath
	private static final String FILE = "config.properties";
	// Loaded once; the settings do not change during a run
	private static final Config INSTANCE = new Config();

	// Values from the settings file
	private final Properties properties = new Properties();

	// Use get()
	private Config() {
		try (InputStream in = Config.class.getClassLoader().getResourceAsStream(FILE)) {
			if (in == null) {
				throw new IllegalStateException(FILE + " not found on the classpath");
			}
			properties.load(in);
		} catch (IOException e) {
			throw new UncheckedIOException("Could not read " + FILE, e);
		}
	}

	/**
	 * @return Config the shared settings.
	 */
	public static Config get() {
		return INSTANCE;
	}

	/**
	 * @param key       the setting name.
	 * @return String   the system property with that name, else the file value.
	 * @throws IllegalStateException if the setting is in neither place.
	 */
	public String value(String key) {
		String value = System.getProperty(key, properties.getProperty(key));
		if (value == null || value.isBlank()) {
			throw new IllegalStateException("Missing setting '" + key + "' in " + FILE);
		}
		return value.trim();
	}

	/**
	 * @return String the application URL without a trailing slash.
	 */
	public String baseUrl() {
		return value("baseUrl").replaceAll("/+$", "");
	}

	/**
	 * @return String chrome, firefox or edge.
	 */
	public String browser() {
		return value("browser");
	}

	/**
	 * @return boolean true to run the browser without a window.
	 */
	public boolean headless() {
		return Boolean.parseBoolean(value("headless"));
	}

	/**
	 * @return Duration the explicit wait timeout for page actions.
	 */
	public Duration timeout() {
		return Duration.ofSeconds(Long.parseLong(value("timeoutSeconds")));
	}

	/**
	 * @return String the administrator user name.
	 */
	public String username() {
		return value("username");
	}

	/**
	 * @return String the administrator password.
	 */
	public String password() {
		return value("password");
	}
}
