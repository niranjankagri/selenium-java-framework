// Interview examples: Java coding questions
package com.automation.selenium.interview.java;

// TestNG assertions
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertThrows;

// The series as a list
import java.util.ArrayList;
import java.util.List;

// Marks a method (or every public method of a class) as a TestNG test
import org.testng.annotations.Test;

/**
 * Interview question: Print the first n Fibonacci numbers, and the n-th one.
 * <p>
 * Interview answer: each number is the sum of the two before it, starting
 * 0, 1. I generate them with a loop that keeps only the last two values,
 * O(n). The textbook recursive version {@code fib(n - 1) + fib(n - 2)}
 * recomputes the same values again and again (O(2ⁿ)), so I mention it but
 * don't use it; with memoisation it becomes O(n).
 * <p>
 * Concept: iteration vs. naive recursion and its cost. The values grow
 * fast: {@code long} overflows after the 92nd number.
 * <p>
 * Recommended approach: the loop; say why the plain recursion is slow.
 */
@Test(groups = { "interview", "java" })
public class FibonacciTest {

	/**
	 * The first {@code count} Fibonacci numbers.
	 *
	 * @param count how many numbers (0 or more).
	 * @return List the series, starting 0, 1, 1, 2, …
	 */
	static List<Long> series(int count) {
		// Negative counts make no sense
		if (count < 0) {
			throw new IllegalArgumentException("count must not be negative: " + count);
		}
		List<Long> series = new ArrayList<>();
		// The two most recent numbers
		long previous = 0;
		long current = 1;
		for (int i = 0; i < count; i++) {
			// Add the next number
			series.add(previous);
			// Move both one step forward: (a, b) → (b, a + b)
			long next = previous + current;
			previous = current;
			current = next;
		}
		return series;
	}

	/**
	 * The n-th Fibonacci number, counting from 0 (fib(0) = 0, fib(1) = 1).
	 *
	 * @param n    the position.
	 * @return long the number at that position.
	 */
	static long nth(int n) {
		// The last element of the first n + 1 numbers
		return series(n + 1).get(n);
	}

	/** The start of the series and a few positions. */
	@Test(description = "Java: Fibonacci series with a loop")
	public void generatesTheSeries() {
		assertEquals(series(10), List.of(0L, 1L, 1L, 2L, 3L, 5L, 8L, 13L, 21L, 34L));
		assertEquals(nth(0), 0L);
		assertEquals(nth(1), 1L);
		assertEquals(nth(10), 55L);
		// Still fast for large positions (the naive recursion would take very long)
		assertEquals(nth(90), 2_880_067_194_370_816_120L);
	}

	/** Zero, one, and a negative count. */
	@Test(description = "Java: Fibonacci series, edge cases")
	public void handlesEdgeCases() {
		assertEquals(series(0), List.of());
		assertEquals(series(1), List.of(0L));
		assertThrows(IllegalArgumentException.class, () -> series(-1));
	}
}
