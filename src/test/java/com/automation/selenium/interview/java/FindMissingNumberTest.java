// Interview examples: Java coding questions
package com.automation.selenium.interview.java;

// TestNG assertion
import static org.testng.Assert.assertEquals;

// Builds a large test array
import java.util.stream.IntStream;

// Marks a method (or every public method of a class) as a TestNG test
import org.testng.annotations.Test;

/**
 * Interview question: An array holds the numbers 1 to n with exactly one
 * missing, in any order. Find the missing number.
 * <p>
 * Interview answer: the sum of 1..n is {@code n * (n + 1) / 2}; subtracting
 * the sum of the array leaves the missing number. n is the array length plus
 * one. O(n) time, O(1) space. I add up in a {@code long}, because for large n
 * the sum overflows an {@code int}.
 * <p>
 * Concept: a formula beats searching. XOR-ing 1..n with all elements also
 * works and can never overflow.
 * <p>
 * Recommended approach: the sum formula with {@code long}; mention XOR as the
 * overflow-free alternative.
 */
@Test(groups = { "interview", "java" })
public class FindMissingNumberTest {

	/**
	 * Finds the one number missing from 1..n.
	 *
	 * @param numbers the numbers 1..n with one missing (any order).
	 * @return int    the missing number.
	 */
	static int missingNumber(int[] numbers) {
		// The full range has one more number than the array
		long n = numbers.length + 1L;
		// Sum of 1..n (long: n * (n + 1) overflows an int from n ≈ 46,341)
		long expected = n * (n + 1) / 2;
		// Sum of what is there
		long actual = 0;
		for (int value : numbers) {
			actual += value;
		}
		// The difference is the missing number
		return (int) (expected - actual);
	}

	/** Missing in the middle, at the start, at the end, unsorted. */
	@Test(description = "Java: find the missing number in 1..n")
	public void findsTheMissingNumber() {
		assertEquals(missingNumber(new int[] { 1, 2, 4, 5, 6 }), 3);
		// The first number missing
		assertEquals(missingNumber(new int[] { 2, 3, 4 }), 1);
		// The last number missing
		assertEquals(missingNumber(new int[] { 1, 2, 3 }), 4);
		// Order does not matter
		assertEquals(missingNumber(new int[] { 5, 3, 1, 2 }), 4);
		// Only "1" missing from 1..1
		assertEquals(missingNumber(new int[0]), 1);
	}

	/** A large range, where an int sum would overflow. */
	@Test(description = "Java: find the missing number without overflow")
	public void handlesLargeRanges() {
		// 1..100,000 without 77,777 (the int sum of 1..100,000 would overflow)
		int[] numbers = IntStream.rangeClosed(1, 100_000).filter(i -> i != 77_777).toArray();
		assertEquals(missingNumber(numbers), 77_777);
	}
}
