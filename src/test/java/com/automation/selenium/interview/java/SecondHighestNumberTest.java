// Interview examples: Java coding questions
package com.automation.selenium.interview.java;

// TestNG assertion
import static org.testng.Assert.assertEquals;

// "Maybe an int": empty when there is no second-highest value
import java.util.OptionalInt;
// Streams over int arrays
import java.util.stream.IntStream;

// Marks a method (or every public method of a class) as a TestNG test
import org.testng.annotations.Test;

/**
 * Interview question: Find the second-highest number in an array, without
 * sorting.
 * <p>
 * Interview answer: one pass keeping two values, {@code highest} and
 * {@code second}. A bigger number pushes {@code highest} down into
 * {@code second}; a number between them only replaces {@code second}. Equal
 * values are skipped, so the answer is the second highest <i>distinct</i>
 * value. O(n) time, O(1) space. If there is no such value (one element, or
 * all equal), I return {@code OptionalInt.empty()}.
 * <p>
 * Concept: sorting gives the answer too, but costs O(n log n) and needs
 * extra care with duplicates ({@code {20, 20, 10}} → 10, not 20).
 * <p>
 * Recommended approach: the single pass; ask whether duplicates count and
 * what to return when there is no answer.
 */
@Test(groups = { "interview", "java" })
public class SecondHighestNumberTest {

	/**
	 * Finds the second-highest distinct value in one pass.
	 *
	 * @param numbers     the array.
	 * @return OptionalInt the value, or empty if there are fewer than two distinct values.
	 */
	static OptionalInt secondHighest(int[] numbers) {
		// Long, so "not set yet" can be a value no int can have
		long highest = Long.MIN_VALUE;
		long second = Long.MIN_VALUE;
		for (int n : numbers) {
			if (n > highest) {
				// New maximum: the old maximum becomes the second highest
				second = highest;
				highest = n;
			} else if (n > second && n < highest) {
				// Between the two: a better second highest (equal values are skipped)
				second = n;
			}
		}
		// Still unset: fewer than two distinct values
		return second == Long.MIN_VALUE ? OptionalInt.empty() : OptionalInt.of((int) second);
	}

	/** Typical arrays, including duplicates of the maximum and negatives. */
	@Test(description = "Java: second-highest number without sorting")
	public void findsTheSecondHighest() {
		assertEquals(secondHighest(new int[] { 10, 5, 20, 8 }), OptionalInt.of(10));
		// Duplicates of the maximum don't count as "second"
		assertEquals(secondHighest(new int[] { 20, 20, 10 }), OptionalInt.of(10));
		// Works with negative numbers
		assertEquals(secondHighest(new int[] { -3, -1, -2 }), OptionalInt.of(-2));
		// The same answer as the stream version: distinct, sort descending, skip one
		int[] sample = { 4, 9, 9, 7, 1 };
		assertEquals(secondHighest(sample),
				IntStream.of(sample).boxed().distinct().sorted((a, b) -> b - a).skip(1).mapToInt(Integer::intValue).findFirst());
	}

	/** No second-highest value exists. */
	@Test(description = "Java: second-highest number when there is none")
	public void returnsEmptyWithoutASecondValue() {
		assertEquals(secondHighest(new int[] { 5 }), OptionalInt.empty());
		assertEquals(secondHighest(new int[] { 5, 5, 5 }), OptionalInt.empty());
		assertEquals(secondHighest(new int[0]), OptionalInt.empty());
	}
}
