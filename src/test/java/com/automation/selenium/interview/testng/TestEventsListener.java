// Interview examples: TestNG
package com.automation.selenium.interview.testng;

// The events recorded per test
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

// TestNG listener for per-test events
import org.testng.ITestListener;
// The result of one test run
import org.testng.ITestResult;

/**
 * A small {@link ITestListener}: TestNG calls it when each test starts,
 * passes, fails or is skipped, and it writes the event down. Real listeners
 * use the same hooks to log, take screenshots or feed a report.
 * <p>
 * Used by {@link ListenerTest} through {@code @Listeners}. Note: a listener
 * added with {@code @Listeners} on one class is registered for the
 * <b>whole suite</b>, so it also sees the events of every other class.
 */
public class TestEventsListener implements ITestListener {

	// "Class.method" → the events of that test, in order (thread-safe: classes run in parallel)
	private static final Map<String, List<String>> EVENTS = new ConcurrentHashMap<>();

	/**
	 * Returns what the listener saw for one test.
	 *
	 * @param test   "SimpleClassName.methodName".
	 * @return List  the events, e.g. [onTestStart, onTestSuccess]; empty if none.
	 */
	public static List<String> eventsOf(String test) {
		// A copy, so the caller cannot change the record
		return List.copyOf(EVENTS.getOrDefault(test, List.of()));
	}

	/** A test method is about to run. */
	@Override
	public void onTestStart(ITestResult result) {
		record(result, "onTestStart");
	}

	/** A test method passed. */
	@Override
	public void onTestSuccess(ITestResult result) {
		record(result, "onTestSuccess");
	}

	/** A test method failed (a screenshot hook would go here). */
	@Override
	public void onTestFailure(ITestResult result) {
		record(result, "onTestFailure");
	}

	/** A test method was skipped. */
	@Override
	public void onTestSkipped(ITestResult result) {
		record(result, "onTestSkipped");
	}

	/**
	 * Adds one event to a test's list.
	 *
	 * @param result the test.
	 * @param event  the event name.
	 */
	private static void record(ITestResult result, String event) {
		// Key: e.g. "ListenerTest.listenerSawThisTestStart"
		String key = result.getTestClass().getRealClass().getSimpleName() + "." + result.getMethod().getMethodName();
		// Create the list on first use (atomically), then add the event
		EVENTS.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>()).add(event);
	}
}
