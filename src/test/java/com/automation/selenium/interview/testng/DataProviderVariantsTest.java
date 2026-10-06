// Interview examples: TestNG
package com.automation.selenium.interview.testng;

// TestNG assertions
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

// The calling test method, passed to a data provider
import java.lang.reflect.Method;
// A lazy data source
import java.util.Iterator;
import java.util.List;
// Thread-safe set for the parallel provider's threads
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

// Marks a method as a data source for tests
import org.testng.annotations.DataProvider;
// Marks a method (or every public method of a class) as a TestNG test
import org.testng.annotations.Test;

// Writes steps into the HTML report
import com.automation.selenium.utils.Steps;

/**
 * Interview question: What forms can a {@code @DataProvider} take?
 * <p>
 * Interview answer: Besides Object[][], a data provider can return a lazy
 * Iterator<Object[]>, take the calling Method to serve several tests, or run
 * its rows in parallel with parallel = true, in which case the test must be
 * thread-safe.
 * <p>
 * Concept: the basic form returns {@code Object[][]}, one inner array per
 * test run (see {@code TestData.invalidCredentials} with {@code LoginTest}).
 * Other forms:
 * <ul>
 * <li>{@code Iterator<Object[]>}: rows are produced one at a time, useful
 * for large or generated data (rows are not all held in memory).</li>
 * <li>A {@code Method} parameter: one provider serves several tests and
 * returns different rows depending on which test asks.</li>
 * <li>{@code parallel = true}: the rows run at the same time on
 * TestNG's data-provider threads (10 by default, set with
 * {@code data-provider-thread-count}); the test must then be thread-safe.</li>
 * </ul>
 * {@code @Parameters} is different: single values from testng.xml, see
 * {@code TestNgFeaturesTest.titleMatchesTheSuiteParameter}.
 * <p>
 * No browser is needed, so this class does not extend {@code BaseTest}.
 */
@Test(groups = { "interview", "testng" })
public class DataProviderVariantsTest {

	// Threads the parallel provider's rows ran on
	private static final Set<String> PARALLEL_THREADS = ConcurrentHashMap.newKeySet();

	/**
	 * Rows produced lazily: each {@code next()} builds one row when TestNG
	 * asks for it.
	 *
	 * @return Iterator rows of { number, its square }.
	 */
	@DataProvider(name = "squares")
	public static Iterator<Object[]> squares() {
		// An anonymous Iterator: hasNext/next are called by TestNG row by row
		return new Iterator<>() {
			// The next number to produce
			private int n = 1;

			@Override
			public boolean hasNext() {
				// Three rows: 1, 2, 3
				return n <= 3;
			}

			@Override
			public Object[] next() {
				// Build the row only now, then move on
				Object[] row = { n, n * n };
				n++;
				return row;
			}
		};
	}

	/**
	 * One run per row of the lazy provider.
	 *
	 * @param number  the input.
	 * @param square  the expected square.
	 */
	@Test(dataProvider = "squares", description = "TestNG: a lazy Iterator data provider")
	public void iteratorProviderFeedsOneRowPerRun(int number, int square) {
		Steps.log(number + "² = " + square);
		assertEquals(number * number, square, "Square of " + number);
	}

	/**
	 * One provider for several tests: TestNG passes the test method that
	 * asks, and the provider picks the matching rows.
	 *
	 * @param test    the test method the rows are for.
	 * @return Object[][] rows for that test.
	 */
	@DataProvider(name = "byTestName")
	public static Object[][] byTestName(Method test) {
		// A switch on the asking test's name
		return switch (test.getName()) {
		case "validUserNamesAreAccepted" -> new Object[][] { { "Admin" }, { "tomsmith" } };
		case "blankUserNamesAreRejected" -> new Object[][] { { "" }, { "   " } };
		default -> throw new IllegalArgumentException("No data for " + test.getName());
		};
	}

	/**
	 * Gets the "valid" rows from the shared provider.
	 *
	 * @param name a user name.
	 */
	@Test(dataProvider = "byTestName", description = "TestNG: a Method-aware data provider (valid rows)")
	public void validUserNamesAreAccepted(String name) {
		assertTrue(!name.isBlank(), "\"" + name + "\" should be a valid user name");
	}

	/**
	 * Gets the "blank" rows from the same provider.
	 *
	 * @param name a user name.
	 */
	@Test(dataProvider = "byTestName", description = "TestNG: a Method-aware data provider (blank rows)")
	public void blankUserNamesAreRejected(String name) {
		assertTrue(name.isBlank(), "\"" + name + "\" should be blank");
	}

	/**
	 * Rows that may run at the same time.
	 *
	 * @return Object[][] rows of { a, b, a + b }.
	 */
	@DataProvider(name = "sums", parallel = true)
	public static Object[][] sums() {
		return new Object[][] { { 1, 2, 3 }, { 2, 3, 5 }, { 10, 20, 30 }, { 7, 8, 15 } };
	}

	/**
	 * One run per row, possibly in parallel; it only uses its own
	 * parameters, so it is thread-safe.
	 *
	 * @param a    first number.
	 * @param b    second number.
	 * @param sum  expected sum.
	 */
	@Test(dataProvider = "sums", description = "TestNG: a parallel data provider")
	public void parallelProviderRowsAreIndependent(int a, int b, int sum) {
		// Remember which thread ran this row (shown in the report)
		PARALLEL_THREADS.add(Thread.currentThread().getName());
		Steps.log(a + " + " + b + " = " + sum + " on " + Thread.currentThread().getName());
		assertEquals(a + b, sum, "Sum of " + a + " and " + b);
	}

	/**
	 * Lists the threads the parallel rows used (at least one).
	 */
	@Test(dependsOnMethods = "parallelProviderRowsAreIndependent",
			description = "TestNG: which threads ran the parallel data provider rows")
	public void parallelRowsRanOnProviderThreads() {
		// Show the thread names in the report
		Steps.log("Parallel rows ran on: " + List.copyOf(PARALLEL_THREADS));
		// How many depends on the machine, so only "some" is checked
		assertTrue(!PARALLEL_THREADS.isEmpty(), "The parallel rows ran");
	}
}
