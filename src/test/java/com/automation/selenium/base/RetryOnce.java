package com.automation.selenium.base;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * Runs a failed test once more before reporting it as failed. Meant for
 * failures outside the test's control, such as a short outage of the public
 * demo site; it does not fix a test that fails for a real reason.
 * <p>
 * Attach it to one test with {@code @Test(retryAnalyzer = RetryOnce.class)}.
 * TestNG reports the failed attempt as skipped, so the report shows that a
 * retry happened.
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
		if (retries < MAX_RETRIES) {
			retries++;
			System.out.println("Retrying " + result.getName() + " (" + retries + " of " + MAX_RETRIES + "): "
					+ result.getThrowable());
			return true;
		}
		return false;
	}
}
