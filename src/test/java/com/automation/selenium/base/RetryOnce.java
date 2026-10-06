// Package of the shared test base classes
package com.automation.selenium.base;

// TestNG interface for deciding whether a failed test runs again
import org.testng.IRetryAnalyzer;
// The result of one test attempt (name, status, error)
import org.testng.ITestResult;

/**
 * Runs a failed test once more before reporting it as failed. Meant for
 * failures outside the test's control, such as a short outage of the public
 * demo site; it does not fix a test that fails for a real reason.
 * <p>
 * Attach it to one test with {@code @Test(retryAnalyzer = RetryOnce.class)}.
 * TestNG reports the failed attempt as skipped, so the report shows that a
 * retry happened.
 * <p>
 * To retry every test without touching each annotation, an
 * {@code IAnnotationTransformer} could set this analyzer on all tests; that
 * is left out on purpose, so real failures are not hidden.
 */
public class RetryOnce implements IRetryAnalyzer {

	// Extra attempts after the first failure
	private static final int MAX_RETRIES = 1;

	// Retries used so far; TestNG keeps one analyzer per test method (and data row)
	private int retries;

	/**
	 * Called by TestNG after each failed attempt.
	 *
	 * @param result   the failed attempt.
	 * @return boolean true to run the test again, false to report the failure.
	 */
	@Override
	public boolean retry(ITestResult result) {
		// Any retries left for this test?
		if (retries < MAX_RETRIES) {
			// Count this retry before running again
			retries++;
			// Print why the test is retried, e.g. "Retrying adminCanLogIn (1 of 1): java.lang.AssertionError: ..."
			System.out.println("Retrying " + result.getName() + " (" + retries + " of " + MAX_RETRIES + "): "
					+ result.getThrowable());
			// true = TestNG runs the test again (the failed attempt is reported as skipped)
			return true;
		}
		// No retries left: TestNG reports the failure as it is
		return false;
	}
}
