// Interview examples: Java coding questions
package com.automation.selenium.interview.java;

// TestNG assertion
import static org.testng.Assert.assertEquals;

// Sets: one for what was seen, one (ordered) for the duplicates
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

// Marks a method (or every public method of a class) as a TestNG test
import org.testng.annotations.Test;

/**
 * Interview question: Find the duplicate elements in an integer array.
 * <p>
 * Interview answer: one pass with a {@code HashSet} of numbers seen so far;
 * when {@code seen.add(n)} returns false, the number is a duplicate and goes
 * into an ordered {@code LinkedHashSet}, so each duplicate is reported once.
 * O(n) time, O(n) extra space. Without extra space I would sort first and
 * compare neighbours, O(n log n).
 * <p>
 * Concept: the same "seen before?" set as for characters
 * ({@link DuplicateCharactersTest}), here on numbers.
 * <p>
 * Recommended approach: the set; mention the sort-and-compare alternative
 * and its trade-off (less memory, more time, and it changes the order).
 */
@Test(groups = { "interview", "java" })
public class FindDuplicateElementsTest {

	/**
	 * Lists the numbers that occur more than once.
	 *
	 * @param numbers the array to check.
	 * @return List   each duplicate once, in the order it first repeats.
	 */
	static List<Integer> duplicates(int[] numbers) {
		// Numbers met so far
		Set<Integer> seen = new HashSet<>();
		// Duplicates, each once, in the order they first repeat
		Set<Integer> duplicates = new LinkedHashSet<>();
		for (int n : numbers) {
			// add() returns false for a number already seen
			if (!seen.add(n)) {
				duplicates.add(n);
			}
		}
		return List.copyOf(duplicates);
	}

	/** Duplicates are found once each, in order of repetition. */
	@Test(description = "Java: find duplicate elements in an array")
	public void findsDuplicates() {
		// 2 repeats first (index 3), then 1 (index 5)
		assertEquals(duplicates(new int[] { 1, 2, 3, 2, 4, 1, 5 }), List.of(2, 1));
		// Three 7s are still one duplicate
		assertEquals(duplicates(new int[] { 7, 7, 7 }), List.of(7));
		// Negative numbers work the same way
		assertEquals(duplicates(new int[] { -1, 0, -1 }), List.of(-1));
	}

	/** No duplicates, and an empty array. */
	@Test(description = "Java: no duplicate elements")
	public void reportsNothingWithoutDuplicates() {
		assertEquals(duplicates(new int[] { 1, 2, 3 }), List.of());
		assertEquals(duplicates(new int[0]), List.of());
	}
}
