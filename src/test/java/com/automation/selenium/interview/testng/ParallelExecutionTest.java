// Interview examples: TestNG
package com.automation.selenium.interview.testng;

// TestNG assertion
import static org.testng.Assert.assertEquals;

// Thread-safe set of the thread names
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
// Makes three runs wait for each other, so they really overlap
import java.util.concurrent.CyclicBarrier;
// Time unit for the barrier's timeout
import java.util.concurrent.TimeUnit;

// Marks a method (or every public method of a class) as a TestNG test
import org.testng.annotations.Test;

// Writes steps into the HTML report
import com.automation.selenium.utils.Steps;

/**
 * Interview question: How does TestNG run tests in parallel, and why does a
 * Selenium framework need {@code ThreadLocal}?
 * <p>
 * Interview answer: TestNG runs methods, classes or tests in parallel with
 * the parallel attribute and thread-count, or one method's invocations with
 * threadPoolSize. Anything shared must be thread-safe, so the WebDriver
 * lives in a ThreadLocal and each thread works with its own browser.
 * <p>
 * Concept: {@code parallel} in testng.xml or Surefire runs
 * {@code methods}, {@code classes}, {@code tests} or {@code instances} on
 * {@code thread-count} threads; this project uses {@code classes}. On one
 * method, {@code @Test(invocationCount = N, threadPoolSize = T)} runs N
 * invocations on T threads. Anything shared between threads must be
 * thread-safe. A {@link ThreadLocal} gives each thread its own copy of a
 * value; {@code DriverManager} keeps the browser that way, so parallel tests
 * never drive each other's browser.
 * <p>
 * Implementation: six invocations on three threads. A {@link CyclicBarrier}
 * makes three of them wait for each other, so they are guaranteed to run
 * at the same time; each writes its own name into the ThreadLocal and must
 * read back its own name, not another thread's. A plain static field would
 * fail that check. No browser is needed.
 */
@Test(groups = { "interview", "testng" })
public class ParallelExecutionTest {

	// Number of threads TestNG runs the invocations on
	private static final int THREADS = 3;
	// Each thread's own value: set and read by the same thread only
	private static final ThreadLocal<String> OWNER = new ThreadLocal<>();
	// Every thread that ran an invocation
	private static final Set<String> USED_THREADS = ConcurrentHashMap.newKeySet();
	// Three invocations at a time wait here until all three have arrived
	private static final CyclicBarrier ALL_RUNNING = new CyclicBarrier(THREADS);

	/**
	 * Runs six times on three threads. Every invocation stores its thread's
	 * name, waits until the other two of its round are running too, and then
	 * reads back its own value.
	 *
	 * @throws Exception if the barrier is broken or times out.
	 */
	@Test(invocationCount = 6, threadPoolSize = THREADS,
			description = "TestNG: threadPoolSize runs invocations in parallel; ThreadLocal keeps them apart")
	public void eachThreadKeepsItsOwnValue() throws Exception {
		// This invocation's thread
		String me = Thread.currentThread().getName();
		// Write our own value
		OWNER.set(me);
		USED_THREADS.add(me);
		// Wait until three invocations are running at the same time (each wrote its own value by now)
		ALL_RUNNING.await(30, TimeUnit.SECONDS);
		// The other threads wrote to OWNER as well, but we still read our own value
		assertEquals(OWNER.get(), me, "Value seen by " + me);
		Steps.log("Thread " + me + " read back its own value while others ran in parallel");
		// Clean up: thread pools reuse threads, so a forgotten value would leak to the next task
		OWNER.remove();
	}

	/**
	 * After all invocations: exactly three threads were used.
	 */
	@Test(dependsOnMethods = "eachThreadKeepsItsOwnValue", description = "TestNG: the invocations used threadPoolSize threads")
	public void invocationsUsedThePoolsThreads() {
		Steps.log("Threads used: " + USED_THREADS);
		assertEquals(USED_THREADS.size(), THREADS, "Threads used by the invocations");
	}
}
