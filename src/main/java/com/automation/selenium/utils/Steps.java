// Package of the small helpers used by pages and tests
package com.automation.selenium.utils;

// TestNG's Reporter: stores text messages on the test result that is running now
import org.testng.Reporter;

/**
 * Records the steps of the running test. Each step is attached to the
 * current TestNG result and shown, in order, in the HTML report.
 * <p>
 * How it works: {@link Reporter#log(String)} adds the message to the
 * {@code ITestResult} of the test running on this thread. Later,
 * {@code HtmlReportListener} reads those messages back with
 * {@code Reporter.getOutput(result)} and prints them as the test's steps.
 * Because TestNG tracks the current result per thread, steps from tests that
 * run in parallel never mix.
 */
// final: nobody should extend a class that only has static methods
public final class Steps {

	// Static helper only: a private constructor stops anyone creating an instance
	private Steps() {
	}

	/**
	 * Logs one step of the current test.
	 *
	 * @param step a short description of what the test is doing, e.g. "Log in as Admin".
	 */
	public static void log(String step) {
		// Attach the step to the current test result (shown in the report, not on the console)
		Reporter.log(step);
	}
}
