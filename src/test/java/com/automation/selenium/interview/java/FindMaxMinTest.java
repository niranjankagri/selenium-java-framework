// Interview examples: Java coding questions
package com.automation.selenium.interview.java;

// TestNG assertions
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertThrows;

// The stream version for comparison
import java.util.IntSummaryStatistics;
import java.util.stream.IntStream;

// Marks a method (or every public method of a class) as a TestNG test
import org.testng.annotations.Test;

/**
 * Interview question: Find the largest and smallest number in an array in
 * one pass, without sorting.
 * <p>
 * Interview answer: I start both {@code min} and {@code max} at the first
 * element (not at 0, which is wrong for all-negative arrays) and update them
 * while walking through the rest. O(n), one pass. An empty array has no
 * answer, so it throws. In real code: {@code IntStream.of(a).summaryStatistics()}.
 * <p>
 * Concept: initialise from the data, not from a guessed constant; returning
 * two values is clearest with a small record.
 * <p>
 * Recommended approach: one loop and a record, plus the empty-array case.
 */
@Test(groups = { "interview", "java" })
public class FindMaxMinTest {

	/**
	 * Smallest and largest value of an array.
	 *
	 * @param min the smallest value.
	 * @param max the largest value.
	 */
	record MinMax(int min, int max) {
	}

	/**
	 * Finds the smallest and largest value in one pass.
	 *
	 * @param numbers the array (not empty).
	 * @return MinMax both values.
	 * @throws IllegalArgumentException for an empty array.
	 */
	static MinMax minMax(int[] numbers) {
		// No elements, no answer
		if (numbers.length == 0) {
			throw new IllegalArgumentException("The array is empty");
		}
		// Start from the first element (0 would be wrong for all-negative arrays)
		int min = numbers[0];
		int max = numbers[0];
		// Compare every other element once
		for (int i = 1; i < numbers.length; i++) {
			if (numbers[i] < min) {
				min = numbers[i];
			} else if (numbers[i] > max) {
				max = numbers[i];
			}
		}
		return new MinMax(min, max);
	}

	/** Typical, negative and single-element arrays. */
	@Test(description = "Java: find max and min in one pass")
	public void findsMinAndMax() {
		assertEquals(minMax(new int[] { 7, 2, 9, 4 }), new MinMax(2, 9));
		// All negative: a start value of 0 would give a wrong max
		assertEquals(minMax(new int[] { -5, -2, -9 }), new MinMax(-9, -2));
		// One element is both
		assertEquals(minMax(new int[] { 3 }), new MinMax(3, 3));
		// Same answer as the library's summary statistics
		IntSummaryStatistics stats = IntStream.of(7, 2, 9, 4).summaryStatistics();
		assertEquals(minMax(new int[] { 7, 2, 9, 4 }), new MinMax(stats.getMin(), stats.getMax()));
	}

	/** An empty array is rejected. */
	@Test(description = "Java: max and min of an empty array")
	public void rejectsAnEmptyArray() {
		assertThrows(IllegalArgumentException.class, () -> minMax(new int[0]));
	}
}
