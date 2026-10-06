// Interview examples: TestNG
package com.automation.selenium.interview.testng;

// TestNG assertions
import static org.testng.Assert.assertTrue;

// Marks a method (or every public method of a class) as a TestNG test
import org.testng.annotations.Test;

// Settings, checked by the "setup" group
import com.automation.selenium.config.Config;
// Writes steps into the HTML report
import com.automation.selenium.utils.Steps;

/**
 * Interview question: What are dependsOnGroups and enabled = false? What
 * happens to a test whose dependency fails?
 * <p>
 * Interview answer: dependsOnMethods and dependsOnGroups run a test only
 * after the others passed; if one fails, the dependent test is skipped, not
 * failed, unless it has alwaysRun = true. enabled = false switches a test
 * off completely: it is neither run nor reported.
 * <p>
 * Concept:
 * <ul>
 * <li>{@code dependsOnMethods} / {@code dependsOnGroups}: the test runs
 * after those tests, and only if they all passed. If one fails, the
 * dependent test is <b>skipped</b>, not failed (shown in
 * {@code ReportDemoTest.skippedBecauseDependencyFailed}).</li>
 * <li>{@code alwaysRun = true} on a dependent test makes it a "soft"
 * dependency: it waits for the others but runs even if they failed.</li>
 * <li>{@code enabled = false} switches a test off: it is not run and not
 * reported at all (unlike a skip).</li>
 * </ul>
 * Recommended approach: keep tests independent where you can; use
 * dependencies for a real "no point in going on" relationship, such as
 * an environment check before the tests that need it.
 * <p>
 * No browser is needed, so this class does not extend {@code BaseTest}.
 */
@Test(groups = { "interview", "testng" })
public class DependencyTest {

	/**
	 * The "setup" group: checks the settings the other tests need.
	 */
	@Test(groups = "lab-setup", description = "TestNG: a check in the lab-setup group")
	public void settingsAreUsable() {
		// Both sites must be https addresses
		assertTrue(Config.get().baseUrl().startsWith("https://"), "Application URL");
		assertTrue(Config.get().practiceUrl().startsWith("https://"), "Practice site URL");
		Steps.log("Settings checked: " + Config.get().baseUrl() + ", " + Config.get().practiceUrl());
	}

	/**
	 * Runs only after every test of the "lab-setup" group has passed.
	 */
	@Test(dependsOnGroups = "lab-setup", description = "TestNG: dependsOnGroups waits for the whole group")
	public void runsAfterTheSetupGroupPassed() {
		Steps.log("Ran after the lab-setup group passed");
		// The group's checks passed, so the settings can be used
		assertTrue(Config.get().timeout().toSeconds() > 0, "A positive wait timeout");
	}

	/**
	 * Switched off: TestNG neither runs nor reports it, so it never fails the
	 * build even though its body would.
	 */
	@Test(enabled = false, description = "TestNG: enabled = false, never runs and is not reported")
	public void disabledTestNeverRuns() {
		// Would fail, but is never called
		throw new IllegalStateException("A disabled test must not run");
	}
}
