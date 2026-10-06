// Interview examples: framework design
package com.automation.selenium.interview.framework;

// TestNG assertions
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

// Marks a method (or every public method of a class) as a TestNG test
import org.testng.annotations.Test;

// The class under discussion
import com.automation.selenium.config.Config;
// Writes steps into the HTML report
import com.automation.selenium.utils.Steps;

/**
 * Interview question: How do you manage configuration such as URLs, the
 * browser and timeouts?
 * <p>
 * Concept: settings live outside the code in one file
 * ({@code config.properties} on the test classpath), are read through one
 * class ({@link Config}), and can be overridden per run with
 * {@code -Dkey=value}, so CI can run another browser or environment
 * without a code change ({@code mvn test -Dbrowser=edge -Dheadless=true}).
 * A missing setting fails at once with its name, not later with a
 * {@code NullPointerException}.
 * <p>
 * Implementation: the key {@code labSetting} exists only for this test
 * (other tests never read it), so changing it does not disturb tests
 * running in parallel. No browser is needed.
 */
@Test(groups = { "interview", "framework" })
public class ConfigOverrideTest {

	// A setting only this test uses: "from-file" in config.properties
	private static final String KEY = "labSetting";

	/**
	 * The file's value is used until a system property with the same name is
	 * set; then the system property wins (that is what {@code -D} does).
	 */
	@Test(description = "Framework: a -D system property overrides config.properties")
	public void systemPropertyOverridesTheFile() {
		// No -DlabSetting: the value from config.properties
		assertEquals(Config.get().value(KEY), "from-file", "Value from the file");
		try {
			// Same as running with -DlabSetting=" from-command-line " (spaces are trimmed)
			System.setProperty(KEY, " from-command-line ");
			assertEquals(Config.get().value(KEY), "from-command-line", "Value from the system property");
			Steps.log("labSetting: \"from-file\" from config.properties, \"from-command-line\" with -DlabSetting");
		} finally {
			// Remove it again, so the JVM is left as it was
			System.clearProperty(KEY);
		}
		// Back to the file's value
		assertEquals(Config.get().value(KEY), "from-file", "Value after removing the system property");
	}

	/**
	 * A setting that is in neither place stops with its name in the message.
	 */
	@Test(description = "Framework: a missing setting fails fast with its name")
	public void missingSettingFailsWithItsName() {
		// Ask for a key that does not exist anywhere
		IllegalStateException e = expectThrows(IllegalStateException.class, () -> Config.get().value("noSuchSetting"));
		// The message names the key and the file
		assertTrue(e.getMessage().contains("noSuchSetting") && e.getMessage().contains("config.properties"),
				"Message: " + e.getMessage());
		Steps.log("Missing setting → \"" + e.getMessage() + "\"");
	}
}
