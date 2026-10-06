// Interview examples: framework design
package com.automation.selenium.interview.framework;

// TestNG assertions
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.expectThrows;

// Two threads, each starting its own browser
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

// The browser
import org.openqa.selenium.WebDriver;
// Every local driver (Chrome, Firefox, Edge) is a RemoteWebDriver underneath, with a session ID
import org.openqa.selenium.remote.RemoteWebDriver;
// Marks a method (or every public method of a class) as a TestNG test
import org.testng.annotations.Test;

// Settings (browser, headless)
import com.automation.selenium.config.Config;
// The class under discussion: one browser per thread
import com.automation.selenium.driver.DriverManager;
// Writes steps into the HTML report
import com.automation.selenium.utils.Steps;

/**
 * Interview question: How do you make WebDriver safe for parallel runs?
 * <p>
 * Concept: a {@code static WebDriver driver} is one variable shared by all
 * threads: with parallel tests, thread B overwrites thread A's browser and
 * both drive the same window. {@link DriverManager} keeps the driver in a
 * {@code ThreadLocal<WebDriver>} instead: each thread has its own slot, so
 * {@code DriverManager.driver()} on thread A always returns A's browser.
 * <pre>
 * Thread 1 ── DriverManager.driver() ── ChromeDriver, session 1
 * Thread 2 ── DriverManager.driver() ── ChromeDriver, session 2
 * </pre>
 * Precisely: WebDriver itself is still not thread-safe; ThreadLocal makes
 * sure no two threads <i>share</i> one. And the slot must be emptied with
 * {@code remove()} after {@code quit()}, because thread pools reuse threads.
 * <p>
 * Implementation: this test starts browsers itself (it does not extend
 * {@code BaseTest}), on two threads of its own.
 */
@Test(groups = { "interview", "framework" })
public class ThreadLocalDriverTest {

	/**
	 * Two threads each start a browser through {@code DriverManager}. Each
	 * thread gets the same driver every time it asks, and the two threads
	 * have two different browser sessions.
	 *
	 * @throws Exception if a thread fails.
	 */
	@Test(description = "Framework: each thread gets its own browser from the ThreadLocal driver")
	public void eachThreadGetsItsOwnBrowser() throws Exception {
		// The work each thread does: start a browser, check it, return its session ID, quit
		Callable<String> startOwnBrowser = () -> {
			// Start a browser for this thread
			WebDriver started = DriverManager.start(Config.get().browser(), Config.get().headless());
			try {
				// Asking again on the same thread gives the very same object
				assertSame(DriverManager.driver(), started, "Same driver on the same thread");
				// The browser's session ID identifies this browser
				return ((RemoteWebDriver) started).getSessionId().toString();
			} finally {
				// Always close it and empty this thread's slot
				DriverManager.quit();
			}
		};

		// Two threads of our own
		ExecutorService pool = Executors.newFixedThreadPool(2);
		try {
			// Run the work on both threads at the same time
			List<Future<String>> sessions = pool.invokeAll(List.of(startOwnBrowser, startOwnBrowser));
			// get() waits for each thread and rethrows its failure, if any
			String first = sessions.get(0).get();
			String second = sessions.get(1).get();
			// Show the two sessions in the report
			Steps.log("Thread 1 → session " + first + ", thread 2 → session " + second);
			// Two threads, two browsers
			assertNotEquals(first, second, "Each thread should have its own browser session");
		} finally {
			// Stop the threads
			pool.shutdown();
		}
	}

	/**
	 * After {@code quit()}, the thread's slot is empty: {@code isStarted()} is
	 * false, and {@code driver()} fails with a clear message instead of
	 * handing out a dead browser.
	 */
	@Test(description = "Framework: quit() empties the thread's slot")
	public void quitEmptiesTheThreadsSlot() {
		// Start and stop a browser on this test's thread
		DriverManager.start(Config.get().browser(), Config.get().headless());
		DriverManager.quit();

		// Nothing left in the slot
		assertFalse(DriverManager.isStarted(), "A browser after quit()");
		// driver() refuses, with a message that says what is wrong
		IllegalStateException e = expectThrows(IllegalStateException.class, DriverManager::driver);
		assertEquals(e.getMessage(), "No browser started on this thread", "Message without a browser");
		Steps.log("After quit(): isStarted() = false, driver() → \"" + e.getMessage() + "\"");
	}
}
